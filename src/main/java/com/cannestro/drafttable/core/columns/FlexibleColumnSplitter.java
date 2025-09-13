package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import lombok.NonNull;

import java.util.function.Function;


/**
 * @author Victor Cannestro
 */
public class FlexibleColumnSplitter implements ColumnSplitter {

    private final Column column;
    private DraftTable outputTable = FlexibleDraftTable.create().emptyDraftTable();


    public FlexibleColumnSplitter(@NonNull Column column) {
        this.column = column;
    }

    @Override
    public <T, R> FlexibleColumnSplitter intoColumn(@NonNull String newLabel, Function<T, R> aspect) {
        outputTable = outputTable.addColumn(column.transform(newLabel, aspect));
        return this;
    }

    @Override
    public DraftTable gatherIntoNewTable() {
        if (outputTable.isCompletelyEmpty()) {
            return outputTable.addColumn(column);
        }
        return outputTable;
    }

}
