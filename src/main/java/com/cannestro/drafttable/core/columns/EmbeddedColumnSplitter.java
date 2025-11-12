package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.cannestro.drafttable.core.options.Items.these;


/**
 * @author Victor Cannestro
 */
public class EmbeddedColumnSplitter implements ColumnSplitter {

    private final Column targetColumn;
    private final DraftTable inputTable;
    private final List<Column> columns = new ArrayList<>();


    public EmbeddedColumnSplitter(@NonNull String columnName, @NonNull DraftTable draftTable) {
        this.inputTable = draftTable;
        this.targetColumn = draftTable.select(columnName);
    }

    @Override
    public <T, R> EmbeddedColumnSplitter intoColumn(@NonNull String newLabel, @NonNull Function<T, R> aspect) {
        this.columns.add(this.targetColumn.transform(newLabel, aspect));
        return this;
    }

    @Override
    public DraftTable thenGather() {
        if (this.columns.isEmpty()) {
            return this.inputTable;
        }
        if (1 == this.inputTable.columnCount()) {
            return FlexibleDraftTable.create().fromColumns(this.inputTable.tableName(), this.columns);
        }
        return this.inputTable.drop(this.targetColumn.label()).add(these(this.columns));
    }

}
