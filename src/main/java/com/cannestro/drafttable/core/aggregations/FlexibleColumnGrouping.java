package com.cannestro.drafttable.core.aggregations;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.columns.FlexibleColumn;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import org.jspecify.annotations.NonNull;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.hamcrest.Matchers.*;


/**
 * @author Victor Cannestro
 */
public record FlexibleColumnGrouping(Column column) implements ColumnGrouping {

    public static final String LABEL_FORMATTER = "Grouping: \"%s\"";


    @Override
    @SuppressWarnings("unchecked")
    public <B, R> DraftTable byCountsOf(@NonNull Function<? super B, ? extends R> mapping) {
        Map<R, Long> counts = new HashMap<>();
        for (B value: (List<B>) this.column().values()) {
            R key = isNull(value) ? null : mapping.apply(value);
            counts.merge(key, 1L, Long::sum);
        }
        List<R> keys = new ArrayList<>(counts.keySet());
        return FlexibleDraftTable.create().fromColumns(outputTableName(), List.of(
                FlexibleColumn.from(VALUE, keys),
                FlexibleColumn.from(COUNT, keys.stream().map(counts::get).toList())
        ));
    }

    @Override
    public <R, A, D> DraftTable byValuesUsing(@NonNull Collector<? super R, A, D> aggregation) {
        DraftTable grouping = by(Function.identity(), aggregation);
        Column valueColumn = grouping.select(VALUE).conditionalAction(
                col -> column().hasNulls(),
                col -> col.append((Object) null),
                UnaryOperator.identity()
        );
        Column aggregationColumn = grouping.select(VALUE_AGGREGATION).conditionalAction(
                col -> column().hasNulls(),
                col -> col.append((Object) null),
                UnaryOperator.identity()
        );
        return FlexibleDraftTable.create().fromColumns(outputTableName(), List.of(valueColumn, aggregationColumn));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <B, R, A, D> DraftTable by(@NonNull Function<? super B, ? extends R> mapping, @NonNull Collector<? super B, A, D> aggregation) {
        List<B> nonNullValues = column()
                .where(notNullValue())
                .where((Function<? super B, R>) mapping, (Matcher<R>) notNullValue())
                .values();
        Map<R, D> valueAggregationMap = nonNullValues.stream().collect(Collectors.groupingBy(mapping, aggregation));
        List<R> values = valueAggregationMap.keySet().stream().toList();
        return FlexibleDraftTable.create().fromColumns(
                outputTableName(),
                List.of(
                        FlexibleColumn.from(VALUE, values),
                        FlexibleColumn.from(VALUE_AGGREGATION, values.stream().map(valueAggregationMap::get).toList())
                )
        );
    }

    String outputTableName() {
        return String.format(LABEL_FORMATTER, column().label());
    }

}