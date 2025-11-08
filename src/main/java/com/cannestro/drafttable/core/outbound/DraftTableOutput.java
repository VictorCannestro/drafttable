package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.supporting.csv.CsvWritingOptions;
import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.supporting.csv.options.CustomizableWritingOptions;
import com.cannestro.drafttable.supporting.utils.ObjectMapperManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.cannestro.drafttable.supporting.csv.implementation.CsvDataWriter;
import lombok.AccessLevel;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static java.util.Objects.isNull;
import static org.hamcrest.Matchers.nullValue;


/**
 * @author Victor Cannestro
 */
@Accessors(fluent = true)
public class DraftTableOutput {

    public static final String DIVIDER = "=";
    public static final String EMPTY_DELIMITER = "";
    public static final String PRETTY_DELIMITER = " | ";
    public static final String PRETTY_FORMAT_STRING = "| %s |";
    public static final String SEPARATE_BY_NEW_LINE_FORMAT_STRING = "%s%n%s";
    public static final String NULL_STRING = "null";

    @Getter(AccessLevel.PRIVATE) private final DraftTable draftTable;


    public DraftTableOutput(DraftTable draftTable) {
        if (isNull(draftTable)) {
            throw new IllegalStateException("Cannot output a null object");
        }
        this.draftTable = draftTable;
    }

    public void toCSV(@NonNull File file, @NonNull CsvWritingOptions options) {
        CsvDataWriter.writeAllLinesToCsv(
                file,
                draftTable().columnNames(),
                draftTable().rows().stream()
                        .map(row -> draftTable().columnNames().stream()
                                .map(row::valueOf)
                                .map(String.class::cast)
                                .toList())
                        .toList(),
                options
        );
    }

    /**
     * Exports the table to a destination filepath using comma delimiters and String values. Will create a new
     * file, if necessary, otherwise the existing file will be updated.
     *
     * @param file The destination file containing the filepath
     */
    public void toCSV(@NonNull File file) {
        toCSV(file, CustomizableWritingOptions.allDefaults());
    }

    /**
     * <p> Produces a JSON String representation of the {@code DraftTable} using the table "label" and "values" as root
     * elements. If the {@code DraftTable} is empty, then an empty JSON array will be returned under the "values"
     * element. Non-empty example: <pre>{@code
     * {
     *     "label": "tornadoes",
     *     "values": [
     *         {
     *             "Start Lon": "-101.8",
     *             "Length": "7.0",
     *             "State": "KS",
     *             "Fatalities": "0.0",
     *             "Time": "19:32:00",
     *             "Scale": "2.0",
     *             "State No": "12.0",
     *             "Width": "800.0",
     *             "Date": "1994-06-07",
     *             "Injuries": "0.0",
     *             "Start Lat": "39.68"
     *         },
     *         {
     *             "Start Lon": "-82.48",
     *             "Length": "0.2",
     *             "State": "MI",
     *             "Fatalities": "0.0",
     *             "Time": "19:20:00",
     *             "Scale": "1.0",
     *             "State No": "5.0",
     *             "Width": "20.0",
     *             "Date": "1984-05-22",
     *             "Injuries": "1.0",
     *             "Start Lat": "43.08"
     *         }
     *     ]
     * }
     * }</pre>
     * </p>
     *
     * @return A valid JSON string
     */
    public String toJsonString() {
        try {
            return ObjectMapperManager.getInstance()
                    .defaultMapper()
                    .writeValueAsString(new JsonOutputFormat(
                            draftTable().tableName(),
                            draftTable().rows().stream().map(Row::valueMap).toList()
                    ));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void toJson(@NonNull File outputFile) {
        try {
            ObjectMapperManager.getInstance()
                    .defaultMapper()
                    .writeValue(
                            outputFile,
                            new JsonOutputFormat(draftTable().tableName(), draftTable().rows().stream().map(Row::valueMap).toList())
                    );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * <p> Prints a table representation of the type distribution of the {@code DraftTable} to the console. Columns are
     * ordered alphabetically and a count of null values is also provided on a per-column basis. </p>
     */
    public void structure() {
        FlexibleDraftTable.create()
                .fromRows(draftTable().tableName(),
                          draftTable().columns().stream()
                                  .map(column -> new Structure(column.label(),
                                                               column.dataType().getTypeName(),
                                                               column.where(nullValue()).size()))
                                  .sorted(Comparator.comparing(Structure::columnName))
                                  .map(Mappable.class::cast)
                                  .map(HashMapRow::from)
                                  .toList())
                .write()
                .prettyPrint();
    }

    /**
     * <p> Prints a highly readable table representation of the {@code DraftTable} to the console. <b>Row order is
     * preserved</b> when pretty printing, however <b>column order is not guaranteed</b>. Non-primitive objects will
     * be represented by their {@code toString()} output. </p>
     *
     * @return An iterator observable containing the header, divider, and contents (if present)
     */
    public Iterator<String> prettyPrint() {
        return prettyPrint(Integer.MAX_VALUE);
    }

    /**
     * <p> Prints a highly readable table representation of the {@code DraftTable} to the console. <b>Row order is
     * preserved</b> when pretty printing, however <b>column order is not guaranteed</b>. Non-primitive objects will
     * be represented by their {@code toString()} output.
     * <br><br>
     * Printed columns will be <b>at least</b> as wide as their column names, respectively. A
     * {@code characterLimitPerColumn} may be specified to truncate the displayed output of every column. This is useful
     * for columns whose values have large {@code toString()} outputs. </p>
     *
     * @param characterLimitPerColumn A non-negative integer.
     * @return An iterator observable containing the header, divider, and contents (if present)
     */
    public Iterator<String> prettyPrint(int characterLimitPerColumn) {
        if (draftTable().isCompletelyEmpty()) {
            return zeroDimensionalPrettyPrintOf(draftTable());
        }
        if (draftTable().isEmpty()) {
            return oneDimensionalPrettyPrintOf(draftTable());
        }
        if (characterLimitPerColumn < 0) {
            throw new IllegalArgumentException("The character limit per column must be a non-negative integer");
        }
        return fullDimensionalPrettyPrintOf(draftTable(), characterLimitPerColumn);
    }

    Iterator<String> zeroDimensionalPrettyPrintOf(DraftTable draftTable) {
        System.out.println(draftTable.shape());
        return List.of(draftTable.shape()).listIterator();
    }

    Iterator<String> oneDimensionalPrettyPrintOf(DraftTable draftTable) {
        String header = format(PRETTY_FORMAT_STRING, join(PRETTY_DELIMITER, draftTable.columnNames()));
        String divider = join(EMPTY_DELIMITER, nCopies(header.length(), DIVIDER));
        String name = generateNameHeader(divider.length());
        System.out.println(name);
        System.out.println(header);
        System.out.println(divider);
        return List.of(name, header, divider).listIterator();
    }

    Iterator<String> fullDimensionalPrettyPrintOf(DraftTable draftTable, int characterLimitPerColumn) {
        Map<String, Integer> lengthLimitsPerColumn = calculateAppropriateWidthPerColumnGiven(characterLimitPerColumn, draftTable);
        String joinedAndPaddedHeaders = lengthLimitsPerColumn.keySet().stream().toList()
                .stream()
                .map(columnName -> StringUtils.center(columnName, lengthLimitsPerColumn.get(columnName)))
                .collect(Collectors.joining(PRETTY_DELIMITER));
        String header = format(PRETTY_FORMAT_STRING, joinedAndPaddedHeaders);
        String divider = join(EMPTY_DELIMITER, nCopies(header.length(), DIVIDER));
        String name =  generateNameHeader(divider.length());
        List<String> tableWithHeaders = new ArrayList<>(List.of(name, header, divider));
        tableWithHeaders.addAll(formattedRowContentAccordingTo(lengthLimitsPerColumn, draftTable));
        tableWithHeaders.forEach(System.out::println);
        return tableWithHeaders.listIterator();
    }

    Map<String, Integer> calculateAppropriateWidthPerColumnGiven(int characterLimitPerColumn, DraftTable draftTable) {
        Map<String, Integer> columnNameLengths = columnNameLengths(draftTable);
        Map<String, Integer> columnDataMaxLengths = widestEntryLengthPerColumn(draftTable);
        ToIntFunction<String> maxDataLimitRule = (String label) -> characterLimitPerColumn == Integer.MAX_VALUE
                ? columnDataMaxLengths.get(label)
                : Math.min(columnDataMaxLengths.get(label), characterLimitPerColumn);
        Map<String, Integer> overallMaxLengths = new HashMap<>();
        draftTable.columnNames().forEach(label -> overallMaxLengths.put(
                label,
                Math.max(columnNameLengths.get(label), maxDataLimitRule.applyAsInt(label))
        ));
        return overallMaxLengths;
    }

    Map<String, Integer> columnNameLengths(DraftTable draftTable) {
        Map<String, Integer> columnNameLengths = new HashMap<>();
        draftTable.columnNames().forEach(label -> columnNameLengths.put(label, label.length()));
        return columnNameLengths;
    }

    Map<String, Integer> widestEntryLengthPerColumn(DraftTable draftTable) {
        Map<String, Integer> columnDataMaxLengths = new HashMap<>();
        draftTable.columns().forEach(column -> columnDataMaxLengths.put(
                column.label(),
                column.values().stream()
                        .map(Objects::toString)
                        .map(String::length)
                        .max(Comparator.naturalOrder())
                        .orElseThrow())
        );
        return columnDataMaxLengths;
    }

    List<String> formattedRowContentAccordingTo(Map<String, Integer> lengthLimitsPerColumn, DraftTable draftTable) {
        return draftTable.rows().stream()
                .map(Row::valueMap)
                .map(valueMap -> lengthLimitsPerColumn.keySet().stream().toList().stream()
                        .map(Object::toString)
                        .map(columnName -> {
                            int lengthLimit = lengthLimitsPerColumn.get(columnName);
                            if (isNull(valueMap.get(columnName))) {
                                return StringUtils.leftPad(NULL_STRING, lengthLimit);
                            }
                            return StringUtils.leftPad(
                                    truncateIfNecessary(valueMap.get(columnName).toString(), lengthLimit),
                                    lengthLimit
                            );
                        })
                        .toList())
                .map(valueList -> format(PRETTY_FORMAT_STRING, join(PRETTY_DELIMITER, valueList)))
                .toList();
    }

    String truncateIfNecessary(String string, int lengthLimit){
        if (string.length() == Math.min(string.length(), lengthLimit)) {
            return string;
        }
        return string.substring(0, lengthLimit);
    }

    String generateNameHeader(int length) {
        return String.format(
                SEPARATE_BY_NEW_LINE_FORMAT_STRING,
                StringUtils.center(draftTable().tableName(), length),
                StringUtils.repeat(DIVIDER, length)
        );
    }

}
