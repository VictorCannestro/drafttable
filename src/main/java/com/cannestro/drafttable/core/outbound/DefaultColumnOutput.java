package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.options.StatisticName;
import com.cannestro.drafttable.supporting.json.ObjectMapperManager;
import org.jspecify.annotations.NonNull;
import tools.jackson.core.JacksonException;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
public record DefaultColumnOutput(Column column) implements ColumnOutput {

    public static final String EMPTY_LABEL = "";
    public static final String INDEX_LABEL = "index";
    public static final String DESCRIBE_LABEL_FORMATTER = "Column: \"%s\"";


    public DefaultColumnOutput {
        if (isNull(column)) {
            throw new IllegalStateException("Cannot output a null object");
        }
    }

    /**
     * <p> Produces a JSON String representation of the {@code Column} using the underlying values as the elements of a
     * JSON array. If the {@code Column} is empty, then an empty JSON array will be returned. For example: <pre>{@code
     *     [
     *         "2005-03-21",
     *         "1976-06-28",
     *         "1964-05-10",
     *         "1984-06-17",
     *         "2007-03-01",
     *         "1973-06-26",
     *         "1998-11-10"
     *     ]
     * }</pre>
     * </p>
     *
     * @return A valid JSON string
     */
    @Override
    public String toJsonString() {
        try {
            return ObjectMapperManager.getInstance().defaultMapper().writeValueAsString(column().values());
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void toJson(@NonNull File outputFile) {
        try {
            ObjectMapperManager.getInstance().defaultMapper().writeValue(outputFile, column().values());
        } catch (JacksonException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Iterator<String> prettyPrint() {
        return FlexibleDraftTable.create().fromColumns(EMPTY_LABEL, List.of(column()))
                .introspect(df -> df.add(INDEX_LABEL, IntStream.range(0, df.rowCount()).boxed().toList(), null))
                .write()
                .prettyPrint();
    }

    @Override
    public Iterator<String> describe() {
        if (column().descriptiveStats().isEmpty()) {
            String report = String.format("Cannot describe non-numeric Column: '%s'%n", column().label());
            System.out.print(report);
            return List.of(report).listIterator();
        } else {
            List<Map.Entry<StatisticName, Number>> entries = column().descriptiveStats().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();
            return FlexibleDraftTable.create().fromRows(
                        String.format(DESCRIBE_LABEL_FORMATTER, column().label()),
                        entries.stream()
                            .map(entry -> HashMapRow.from(new StatisticalDescription(entry.getKey().shortHand, entry.getValue().doubleValue())))
                            .toList())
                    .write()
                    .prettyPrint();
        }
    }

}
