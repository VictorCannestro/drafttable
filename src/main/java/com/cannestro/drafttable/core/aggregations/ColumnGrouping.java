package com.cannestro.drafttable.core.aggregations;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.options.SortingOrderType;

import java.util.function.Function;
import java.util.stream.Collector;


/**
 * @author Victor Cannestro
 */
public interface ColumnGrouping {

    String VALUE = "Value";
    String VALUE_AGGREGATION = "ValueAggregation";
    String COUNT = "Count";


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
    <B, R> DraftTable byCountsOf(Function<? super B, ? extends R> mapping);

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
    <R, A, D> DraftTable byValuesUsing(Collector<? super R, A, D> aggregation);

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
    <B, R, A, D> DraftTable by(Function<? super B, ? extends R> mapping, Collector<? super B, A, D> aggregation);


    /**
     * <p> Creates an aggregation of unique values by frequency of occurrence. The resulting object will be a new
     * {@code DraftTable} with column names  {@code "Value"} and {@code "Count"} of the originating type and of type
     * Long, respectively.</p>
     * <br>
     * <p> Null values are handled, and will appear in the null count, if present. </p>
     *
     * @return A new {@code DraftTable}
     */
    default DraftTable byValueCounts() {
        return byCountsOf(Function.identity());
    }

    /**
     * <p> Creates an aggregation of unique values by frequency of occurrence, in ascending or descending order as
     * designated by the user. The resulting object will be a new {@code DraftTable} with column names {@code "Value"}
     * and {@code "Count"} of the originating type and of type Long, respectively. </p>
     * <br>
     * <p> Null values are handled, and will appear in the null count, if present. </p>
     *
     * @return A new {@code DraftTable}
     */
    default DraftTable byValueCounts(SortingOrderType orderType) {
        return byValueCounts().orderBy(COUNT, orderType);
    }

}
