package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import org.jspecify.annotations.NonNull;

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
    public <T, R> FlexibleColumnSplitter intoColumn(@NonNull String newLabel, @NonNull Function<T, R> aspect) {
        outputTable = outputTable.add(column.transform(newLabel, aspect));
        return this;
    }

    @Override
    public DraftTable gather() {
        if (outputTable.isCompletelyEmpty()) {
            return outputTable.add(column);
        }
        return outputTable;
    }

}
