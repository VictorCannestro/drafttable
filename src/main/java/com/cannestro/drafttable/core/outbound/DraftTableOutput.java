package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.core.DraftTable;
import com.cannestro.drafttable.core.Row;
import com.cannestro.drafttable.core.implementations.FlexibleDraftTable;
import com.cannestro.drafttable.core.implementations.HashMapRow;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.cannestro.drafttable.csv.CsvDataWriter;
import com.cannestro.drafttable.utils.mappers.GsonSupplier;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.hamcrest.Matchers.nullValue;


/**
 * @author Victor Cannestro
 */
public record DraftTableOutput(DraftTable draftTable) {

    public static final String JSON_ROOT_KEY = "data";
    public static final String DIVIDER = "=";
    public static final String PRETTY_DELIMITER = " | ";
    public static final String PRETTY_FORMAT_STRING = "| %s |";


    public DraftTableOutput {
        if (isNull(draftTable)) {
            throw new IllegalStateException("Cannot output a null object");
        }
    }

    /**
     * Exports the data frame to a destination filepath using comma delimiters and String values. Will create a new
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
        FlexibleDraftTable.fromRows(draftTable().columns().stream()
                        .map(column -> new Structure(column.getLabel(),
                                                     column.dataType().getTypeName(),
                                                     column.where(nullValue()).size()))
                        .sorted(Comparator.comparing(Structure::ColumnName))
                        .map(HashMapRow::from)
                        .map(Row.class::cast)
                        .toList())
                .write()
                .prettyPrint();
    }

    /**
     * <p> Prints a highly readable table representation of the {@code DraftTable} to the console. <b>Row order is
     * preserved</b> when pretty printing, however <b>column order is not guaranteed</b>. Non-primitive objects will
     * be represented by their {@code toString()} output. </p>
     */
    public void prettyPrint() {
        if (draftTable().isCompletelyEmpty()) {
            System.out.println(draftTable().shape());
        } else if (draftTable().isEmpty()) {
            String header = String.format(PRETTY_FORMAT_STRING, String.join(PRETTY_DELIMITER, draftTable().columnNames()));
            System.out.println(header);
            System.out.println(String.join("", Collections.nCopies(header.length(), DIVIDER)));
        } else {
            Map<String, Integer> paddingLengthsByColumn = calculatePaddingLengthsByColumn(draftTable());
            String joinedHeaderLabels = paddingLengthsByColumn.keySet().stream().toList()
                    .stream()
                    .map(columnName -> StringUtils.center(columnName, paddingLengthsByColumn.get(columnName)))
                    .collect(Collectors.joining(PRETTY_DELIMITER));
            String header = String.format(PRETTY_FORMAT_STRING, joinedHeaderLabels);
            System.out.println(header);
            System.out.println(String.join("", Collections.nCopies(header.length(), DIVIDER)));
            draftTable().rows().stream()
                    .map(Row::valueMap)
                    .map(valueMap -> valueMap.keySet().stream().toList().stream()
                            .map(Object::toString)
                            .map(columnName -> {
                                if (isNull(valueMap.get(columnName))) {
                                    return StringUtils.leftPad("null", paddingLengthsByColumn.get(columnName));
                                }
                                return StringUtils.leftPad(valueMap.get(columnName).toString(), paddingLengthsByColumn.get(columnName));
                            })
                            .toList())
                    .map(valueList -> String.format(PRETTY_FORMAT_STRING, String.join(PRETTY_DELIMITER, valueList)))
                    .forEach(System.out::println);
        }
    }

    Map<String, Integer> calculatePaddingLengthsByColumn(DraftTable df) {
        Map<String, Integer> columnNameLengths = columnNameLengths(df);
        Map<String, Integer> columnDataMaxLengths = maxDataStringLengthByColumns(df);
        Map<String, Integer> overallMaxLengths = new HashMap<>();
        df.columnNames().forEach(label -> overallMaxLengths.put(
                label,
                Math.max(columnNameLengths.get(label), columnDataMaxLengths.get(label))
        ));
        return overallMaxLengths;
    }

    Map<String, Integer> columnNameLengths(DraftTable df) {
        Map<String, Integer> columnNameLengths = new HashMap<>();
        df.columnNames().forEach(label -> columnNameLengths.put(label, label.length()));
        return columnNameLengths;
    }

    Map<String, Integer> maxDataStringLengthByColumns(DraftTable df) {
        Map<String, Integer> columnDataMaxLengths = new HashMap<>();
        df.columns().forEach(column -> columnDataMaxLengths.put(
                column.getLabel(),
                column.getValues().stream()
                        .map(Objects::toString)
                        .map(String::length)
                        .max(Comparator.naturalOrder())
                        .orElseThrow())
        );
        return columnDataMaxLengths;
    }

}
