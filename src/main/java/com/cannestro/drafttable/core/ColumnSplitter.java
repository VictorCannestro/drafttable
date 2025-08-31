package com.cannestro.drafttable.core;

import lombok.NonNull;

import java.util.function.Function;


public interface ColumnSplitter {

    <T, R> ColumnSplitter intoColumn(@NonNull String newLabel, Function<T, R> aspect);

    DraftTable asNewTable();

}
