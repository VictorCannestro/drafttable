package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.supporting.csv.CsvWritingOptions;
import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.core.rows.HashMapRow;
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
public class DefaultDraftTableOutput implements DraftTableOutput {

    public static final String DIVIDER = "=";
    public static final String EMPTY_DELIMITER = "";
    public static final String PRETTY_DELIMITER = " | ";
    public static final String PRETTY_FORMAT_STRING = "| %s |";
    public static final String SEPARATE_BY_NEW_LINE_FORMAT_STRING = "%s%n%s";
    public static final String NULL_STRING = "null";

    @Getter(AccessLevel.PRIVATE) private final DraftTable draftTable;


    public DefaultDraftTableOutput(DraftTable draftTable) {
        if (isNull(draftTable)) {
            throw new IllegalStateException("Cannot output a null object");
        }
        this.draftTable = draftTable;
    }

    @Override
    public void toCsv(@NonNull File file, @NonNull CsvWritingOptions options) {
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
     * <p> Produces a JSON String representation of the {@code DraftTable} using the rows as elements of a JSON array.
     * If the {@code DraftTable} is empty, then an empty JSON array will be returned. Non-empty example: <pre>{@code
     * [
     *     {
     *         "Start Lon": "-74.11",
     *         "State": "NY",
     *         "Fatalities": "0.0",
     *         "Scale": "2.0",
     *         "State No": "3.0",
     *         "Injuries": "9.0",
     *         "Start Lat": "40.63",
     *         "DateTime": "2007-08-08T04:22:00"
     *     },
     *     {
     *         "Start Lon": "-73.88",
     *         "State": "NY",
     *         "Fatalities": "0.0",
     *         "Scale": "3.0",
     *         "State No": "2.0",
     *         "Injuries": "9.0",
     *         "Start Lat": "42.8",
     *         "DateTime": "1960-06-24T17:30:00"
     *     }
     * ]
     * }</pre>
     * </p>
     *
     * @return A valid JSON string
     */
    @Override
    public String toJsonString() {
        try {
            return ObjectMapperManager.getInstance()
                    .defaultMapper()
                    .writeValueAsString(draftTable().rows().stream().map(Row::valueMap).toList());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void toJson(@NonNull File outputFile) {
        try {
            ObjectMapperManager.getInstance()
                    .defaultMapper()
                    .writeValue(outputFile, draftTable().rows().stream().map(Row::valueMap).toList());
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
    @Override
    public Iterator<String> structure() {
        return FlexibleDraftTable.create()
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

    @Override
    public Iterator<String> prettyPrint() {
        return prettyPrint(Integer.MAX_VALUE);
    }

    @Override
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
