package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.core.Column;
import com.cannestro.drafttable.core.Row;
import com.cannestro.drafttable.core.implementations.tables.FlexibleDraftTable;
import com.cannestro.drafttable.core.implementations.rows.HashMapRow;
import com.cannestro.drafttable.core.options.StatisticName;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.cannestro.drafttable.utils.mappers.GsonSupplier;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
public record ColumnOutput(Column column) {

    public ColumnOutput {
        if (isNull(column)) {
            throw new IllegalStateException("Cannot output a null object");
        }
    }

    /**
     * <p> Produces a JSON String representation of the {@code Column} using the column label as the root element.
     * Non-primitive objects will be represented by a nested JSON String. If the {@code Column} is empty, then an empty
     * JSON array will be returned under the root element. </p>
     *
     * @return A valid JSON string
     */
    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        column().getValues().forEach(value -> jsonArray.add(GsonSupplier.DEFAULT_GSON.toJson(value)));
        jsonObject.add(column().getLabel(), jsonArray);
        return jsonObject.toString();
    }

    /**
     * <p> Prints a highly readable table representation of the {@code Column} to the console alongside an index value.
     * <b> Row order is preserved </b> when pretty printing. Non-primitive objects will be represented by their
     * {@code toString()} output. </p>
     */
    public void prettyPrint() {
        FlexibleDraftTable.fromColumns(List.of(column()))
                         .introspect(df -> df.addColumn("index", IntStream.range(0, df.rowCount()).boxed().toList(), null))
                         .write()
                         .prettyPrint();
    }

    /**
     * <p> Prints a highly readable table representation of the descriptive statistics of the {@code Column} to the
     * console. Statistical measures included:
     * <ul>
     *     <li> Maximum </li>
     *     <li> Arithmetic Mean </li>
     *     <li> Minimum </li>
     *     <li> Standard deviation </li>
     *     <li> Variance </li>
     *     <li> 25th, 50th, and 75th percentile estimates </li>
     * </ul> </p>
     */
    public void describe() {
        if (column().descriptiveStats().isEmpty()) {
            System.out.printf("Cannot describe non-numeric Column: '%s'%n", column().getLabel());
        } else {
            List<Map.Entry<StatisticName, Number>> entries = column().descriptiveStats().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();

            record StatisticalDescription(String METRIC, Double VALUE) {}
            FlexibleDraftTable.fromRows(entries.stream()
                            .map(entry -> HashMapRow.from(new StatisticalDescription(entry.getKey().shortHand, entry.getValue().doubleValue())))
                            .map(Row.class::cast)
                            .toList())
                    .write()
                    .prettyPrint();
        }
    }

}
