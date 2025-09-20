package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.options.StatisticName;
import com.cannestro.drafttable.supporting.utils.ObjectMapperManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
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
     * <p> Produces a JSON String representation of the {@code Column} using the column "label" and "values" as root
     * elements. If the {@code Column} is empty, then an empty JSON array will be returned under the "values" element.
     * For example: <pre>{@code
     * {
     *     "label": "Date",
     *     "values": [
     *         "2005-03-21",
     *         "1976-06-28",
     *         "1964-05-10",
     *         "1984-06-17",
     *         "2007-03-01",
     *         "1973-06-26",
     *         "1998-11-10"
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
                    .writeValueAsString(new JsonOutputFormat(column().label(), column().values()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public void toJson(@NonNull File outputFile) {
        try {
            ObjectMapperManager.getInstance()
                    .defaultMapper()
                    .writeValue(outputFile, new JsonOutputFormat(column().label(), column().values()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * <p> Prints a highly readable table representation of the {@code Column} to the console alongside an index value.
     * <b> Row order is preserved </b> when pretty printing. Non-primitive objects will be represented by their
     * {@code toString()} output. </p>
     */
    public void prettyPrint() {
        FlexibleDraftTable.create().fromColumns(column().label(), List.of(column()))
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
            System.out.printf("Cannot describe non-numeric Column: '%s'%n", column().label());
        } else {
            List<Map.Entry<StatisticName, Number>> entries = column().descriptiveStats().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();

            FlexibleDraftTable.create().fromRows(
                    column().label(),
                    entries.stream()
                            .map(entry -> HashMapRow.from(new StatisticalDescription(entry.getKey().shortHand, entry.getValue().doubleValue())))
                            .toList())
                    .write()
                    .prettyPrint();
        }
    }

}
