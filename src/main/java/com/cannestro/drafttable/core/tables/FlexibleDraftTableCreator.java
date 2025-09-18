package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.columns.FlexibleColumn;
import com.cannestro.drafttable.core.rows.ExperimentalRow;
import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.inbound.CsvLoader;
import com.cannestro.drafttable.core.inbound.DefaultCsvLoader;
import com.cannestro.drafttable.supporting.utils.MapUtils;
import lombok.NonNull;
import org.paumard.streams.StreamsUtils;

import java.util.List;

import static com.cannestro.drafttable.core.assumptions.DraftTableAssumptions.assumeColumnsHaveUniformSize;
import static com.cannestro.drafttable.core.assumptions.DraftTableAssumptions.assumeRowsHaveEquivalentKeySets;
import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeUniformityOf;
import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeUnique;
import static com.cannestro.drafttable.supporting.utils.ListUtils.firstElementOf;
import static java.util.Collections.emptyList;


public class FlexibleDraftTableCreator implements TableCreator {

    @Override
    public DraftTable emptyDraftTable() {
        return new FlexibleDraftTable(emptyList());
    }

    @Override
    public <T extends Mappable> DraftTable fromObjects(@NonNull List<T> objects) {
        return fromRows(objects.stream().map(ExperimentalRow::from).toList());
    }

    @Override
    public DraftTable fromColumns(@NonNull List<Column> columns) {
        if(columns.isEmpty()) {
            return emptyDraftTable();
        }
        assumeUnique(columns.stream().map(Column::label).toList());
        assumeColumnsHaveUniformSize(columns);
        return new FlexibleDraftTable(columns);
    }

    @Override
    public <T extends Row> DraftTable fromRows(@NonNull List<T> listOfRows) {
        if(listOfRows.isEmpty()) {
            return emptyDraftTable();
        }
        assumeRowsHaveEquivalentKeySets(listOfRows);
        return new FlexibleDraftTable(
                firstElementOf(listOfRows)
                        .keys().stream()
                        .map(name -> new FlexibleColumn(
                                name,
                                listOfRows.stream().map(row -> row.valueOf(name)).toList()
                        )).map(Column.class::cast)
                        .toList()
        );
    }

    @Override
    public DraftTable fromColumnValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table) {
        assumeUnique(columnNames);
        assumeUniformityOf(table);
        return new FlexibleDraftTable(
                StreamsUtils.zip(columnNames.stream(), table.stream(), FlexibleColumn::new)
                        .map(Column.class::cast)
                        .toList()
        );
    }

    @Override
    public DraftTable fromRowValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table) {
        assumeUnique(columnNames);
        assumeUniformityOf(table);
        return fromRows(
                table.stream()
                        .map(rowValues -> MapUtils.zip(columnNames, rowValues))
                        .map(HashMapRow::new)
                        .toList()
        );
    }

    @Override
    public CsvLoader fromCSV() {
        return new DefaultCsvLoader();
    }

}
