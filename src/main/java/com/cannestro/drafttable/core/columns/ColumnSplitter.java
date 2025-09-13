package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.tables.DraftTable;
import lombok.NonNull;

import java.util.function.Function;


/**
 * @author Victor Cannestro
 */
public interface ColumnSplitter {

    <T, R> ColumnSplitter intoColumn(@NonNull String newLabel, Function<T, R> aspect);

    DraftTable gatherIntoNewTable();

}
