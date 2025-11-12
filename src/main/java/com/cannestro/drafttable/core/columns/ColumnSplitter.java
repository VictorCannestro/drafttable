package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.tables.DraftTable;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;


/**
 * @author Victor Cannestro
 */
public interface ColumnSplitter {

    <T, R> ColumnSplitter intoColumn(@NonNull String newLabel, @NonNull Function<T, R> aspect);

    /**
     * Collects the derived columns, if any, into a {@code DraftTable} instance. Where applicable, it may return the
     * same {@code DraftTable} or a {@code DraftTable} containing the targeted {@code Column}.
     *
     * @return A {@code DraftTable}
     */
    DraftTable thenGather();

}
