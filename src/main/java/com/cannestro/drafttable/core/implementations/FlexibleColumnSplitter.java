package com.cannestro.drafttable.core.implementations;

import com.cannestro.drafttable.core.Column;
import com.cannestro.drafttable.core.ColumnSplitter;
import com.cannestro.drafttable.core.DraftTable;
import lombok.NonNull;

import java.util.function.Function;


public class FlexibleColumnSplitter implements ColumnSplitter {

    private final Column column;
    private DraftTable outputTable = FlexibleDraftTable.emptyDraftTable();


    public FlexibleColumnSplitter(@NonNull Column column) {
        this.column = column;
    }

    @Override
    public <T, R> FlexibleColumnSplitter intoColumn(@NonNull String newLabel, Function<T, R> aspect) {
        outputTable = outputTable.addColumn(column.transform(newLabel, aspect));
        return this;
    }

    @Override
    public DraftTable asNewTable() {
        return outputTable;
    }

}
