package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.cannestro.drafttable.supporting.csv.CsvDataWriter;
import com.cannestro.drafttable.supporting.utils.mappers.GsonSupplier;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

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
public record DraftTableOutput(DraftTable draftTable) {

    public static final String JSON_ROOT_KEY = "data";
    public static final String DIVIDER = "=";
    public static final String EMPTY_DELIMITER = "";
    public static final String PRETTY_DELIMITER = " | ";
    public static final String PRETTY_FORMAT_STRING = "| %s |";
    public static final String NULL_STRING = "null";


    public DraftTableOutput {
        if (isNull(draftTable)) {
            throw new IllegalStateException("Cannot output a null object");
        }
    }

    /**
     * Exports the table to a destination filepath using comma delimiters and String values. Will create a new
     * file, if necessary, otherwise the existing file will be updated.
     *
     * @param filePath The destination filepath
     */
    public void toCSV(@NonNull String filePath, String fillValue) {
        CsvDataWriter.writeAllLinesToCsv(
                filePath,
                draftTable().rows().stream()
                        .map(row -> draftTable().columnNames().stream()
                                .map(row::valueOf)
                                .map(String.class::cast)
                                .toList())
                        .toList(),
                draftTable().columnNames(),
                fillValue
        );
    }

    /**
     * <p> Produces a JSON String representation of the {@code DraftTable}. Non-primitive objects will be represented by
     * a nested JSON String. If the {@code DraftTable} is empty, then an empty JSON array will be returned under the root
     * element. Non-empty example: <pre>{@code
     * {
     *   "data": [
     *     "{\"Measurements\":{\"Scale\":3.0,\"Length\":6.2,\"Width\":150.0},\"DateTime\":\"1950-01-03T11:00:00\"}"
     *   ]
     * }
     * }</pre>
     * </p>
     *
     * @return A valid JSON string
     */
    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        draftTable().rows().stream()
                   .map(Row::valueMap)
                   .forEach(map -> jsonArray.add(GsonSupplier.DEFAULT_GSON.toJson(map)));
        jsonObject.add(JSON_ROOT_KEY, jsonArray);
        return jsonObject.toString();
    }

    /**
     * <p> Prints a table representation of the type distribution of the {@code DraftTable} to the console. Columns are
     * ordered alphabetically and a count of null values is also provided on a per-column basis. </p>
     */
    public void structure() {
        record Structure(String ColumnName, String Type, double NullCount) {}
        FlexibleDraftTable.create().fromRows(draftTable().columns().stream()
                        .map(column -> new Structure(column.label(),
                                                     column.dataType().getTypeName(),
                                                     column.where(nullValue()).size()))
                        .sorted(Comparator.comparing(Structure::ColumnName))
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
        System.out.println(header);
        System.out.println(divider);
        return List.of(header, divider).listIterator();
    }

    Iterator<String> fullDimensionalPrettyPrintOf(DraftTable draftTable, int characterLimitPerColumn) {
        Map<String, Integer> lengthLimitsPerColumn = calculateAppropriateWidthPerColumnGiven(characterLimitPerColumn, draftTable);
        String joinedAndPaddedHeaders = lengthLimitsPerColumn.keySet().stream().toList()
                .stream()
                .map(columnName -> StringUtils.center(columnName, lengthLimitsPerColumn.get(columnName)))
                .collect(Collectors.joining(PRETTY_DELIMITER));
        String header = format(PRETTY_FORMAT_STRING, joinedAndPaddedHeaders);
        String divider = join(EMPTY_DELIMITER, nCopies(header.length(), DIVIDER));
        List<String> tableWithHeaders = new ArrayList<>(List.of(header, divider));
        tableWithHeaders.addAll(formattedRowContentAccordingTo(lengthLimitsPerColumn, draftTable));
        tableWithHeaders.forEach(System.out::println);
        return tableWithHeaders.listIterator();
    }

    List<String> formattedRowContentAccordingTo(Map<String, Integer> lengthLimitsPerColumn, DraftTable draftTable) {
        return draftTable.rows().stream()
                .map(Row::valueMap)
                .map(valueMap -> valueMap.keySet().stream().toList().stream()
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

}
