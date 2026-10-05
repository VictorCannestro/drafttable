package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.tables.DraftTable;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;


/**
 * @author Victor Cannestro
 */
public interface ColumnSplitter {

    /**
     * Splits a {@code Column} into one or more derived columns. Derived columns may be components of the original or
     * enriched. Example chained API usage: <pre>{@code
     *     .intoColumn("lat", Coordinate::lat)
     *     .intoColumn("lon", Coordinate::lon)
     *     .intoColumn("classifier", WatershedClassifier::new)
     * }</pre>
     *
     * @param newLabel The name of the new column
     * @param aspect A function to apply to the column being split
     * @return A {@code ColumnSplitter} instance
     * @param <T> The current type of the column being split
     * @param <R> The target type of the new column via function application
     */
    <T, R> ColumnSplitter intoColumn(@NonNull String newLabel, @NonNull Function<? super T, ? extends R> aspect);

    /**
     * Collects the derived columns, if any, into a {@code DraftTable} instance. Where applicable, it may return the
     * same {@code DraftTable} or a {@code DraftTable} containing the targeted {@code Column}.
     *
     * @return A {@code DraftTable}
     */
    DraftTable thenGather();

}
