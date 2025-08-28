package com.cannestro.drafttable.core.aggregations;

import com.cannestro.drafttable.core.Column;
import com.cannestro.drafttable.core.DraftTable;
import com.cannestro.drafttable.core.implementations.FlexibleColumn;
import com.cannestro.drafttable.core.implementations.FlexibleDraftTable;
import com.cannestro.drafttable.core.options.SortingOrderType;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;


/**
 * @author Victor Cannestro
 */
public record FlexibleColumnGrouping(Column column) {

    public static final String VALUE = "Value";
    public static final String VALUE_AGGREGATION = "ValueAggregation";
    public static final String COUNT = "Count";


    /**
     * <p> Creates an aggregation of unique values by frequency of occurrence, in ascending or descending order as
     * designated by the user. The resulting object will be a new {@code DraftTable} with column names {@code "Value"}
     * and {@code "Count"} of the originating type and of type Long, respectively. </p>
     * <br>
     * <p> Null values are handled, and will appear in the null count, if present. </p>
     *
     * @return A new {@code DraftTable}
     */
    public DraftTable byValueCounts(SortingOrderType orderType) {
        return byValueCounts().orderBy(COUNT, orderType);
    }

    /**
     * <p> Creates an aggregation of unique values by frequency of occurrence. The resulting object will be a new
     * {@code DraftTable} with column names  {@code "Value"} and {@code "Count"} of the originating type and of type
     * Long, respectively.</p>
     * <br>
     * <p> Null values are handled, and will appear in the null count, if present. </p>
     *
     * @return A new {@code DraftTable}
     */
    public DraftTable byValueCounts() {
        return byCountsOf(Function.identity());
    }

    /**
     * <p> Creates an aggregation of unique values by frequency of occurrence. The resulting object will be a new
     * {@code DraftTable} with column names {@code "Value"} and {@code "Count"} of the type designated in the
     * user-provided function and of type Long, respectively. The total count over all counts will equal the row count
     * of the originating {@code DraftTable}. </p>
     * <br>
     * <p> Pre-mapping null values and post-mapping null values are handled, and will appear in the null count, if
     * present. </p>
     *
     * @param mapping A user-defined data mapping
     * @return A new {@code DraftTable}
     * @param <B> Input mapping type
     * @param <R> Output mapping type
     */
    public <B, R> DraftTable byCountsOf(Function<? super B, ? extends R> mapping) {
        DraftTable grouping = by(mapping, Collectors.counting());
        Column valueColumn = grouping.select(VALUE).conditionalAction(
                col -> column().hasNulls(),
                col -> col.append((Object) null),
                UnaryOperator.identity()
        );
        long nullCountByClassifier = Function.identity().equals(mapping)
                ? column().where(nullValue()).size()
                : (long) column().where(nullValue()).size() + column().where(notNullValue()).transform(mapping).where(nullValue()).size();
        Column aggregationColumn = grouping.select(VALUE_AGGREGATION).conditionalAction(
                col -> column().hasNulls(),
                col -> col.append(nullCountByClassifier),
                UnaryOperator.identity()
        ).rename(COUNT);
        return FlexibleDraftTable.fromColumns(List.of(valueColumn, aggregationColumn));
    }

    /**
     * <p> Creates an aggregation of unique values according to a user-defined reduction operation. The resulting object
     * will be a new {@code DraftTable} with column names {@code "Value"} and {@code "ValueAggregation"} of the
     * originating type and type designated by the user-provided reduction operation, respectively.  </p>
     * <br>
     * <p> Null values are handled, and will appear in the aggregation as null, if present. </p>
     *
     * @param aggregation A user-defined reduction operation
     * @return A new {@code DraftTable}
     * @param <R> The type of input elements to the reduction operation
     * @param <A> The mutable accumulation type of the reduction operation (often hidden as an implementation detail)
     * @param <D> The result type of the reduction operation
     */
    public <R, A, D> DraftTable byValuesUsing(Collector<? super R, A, D> aggregation) {
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
        return FlexibleDraftTable.fromColumns(List.of(valueColumn, aggregationColumn));
    }

    /**
     * <p> Creates an aggregation of unique values according to a user-defined aggregation mapping. The resulting object
     * will be a new {@code DraftTable} with column names {@code "Value"} and {@code "ValueAggregation"} of the type
     * designated in the user-provided function and reduction operation, respectively. </p>
     * <br>
     * <p> Null values are filtered out, if present. </p>
     *
     * @param mapping  A user-defined data mapping
     * @param aggregation A user-defined reduction operation
     * @return A new {@code DraftTable}
     * @param <B> The data type of the input column
     * @param <R> The type of input elements to the reduction operation
     * @param <A> The mutable accumulation type of the reduction operation (often hidden as an implementation detail)
     * @param <D> The result type of the reduction operation
     */
    public <B, R, A, D> DraftTable by(Function<? super B, ? extends R> mapping, Collector<? super B, A, D> aggregation) {
        List<B> nonNullValues = column().where(notNullValue())
                .where((Function<? super B, R>) mapping, (Matcher<R>) notNullValue())
                .getValues();
        Map<R, D> valueAggregationMap = nonNullValues.stream()
                .collect(Collectors.groupingBy(mapping, aggregation));
        List<R> values = valueAggregationMap.keySet().stream().toList();
        return FlexibleDraftTable.fromColumns(List.of(
                FlexibleColumn.from(VALUE, values),
                FlexibleColumn.from(VALUE_AGGREGATION, values.stream().map(valueAggregationMap::get).toList())
        ));
    }

}