package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.columns.FlexibleColumn;
import com.cannestro.drafttable.core.inbound.*;
import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.supporting.utils.MapHelper;
import org.jspecify.annotations.NonNull;
import org.paumard.streams.StreamsUtils;

import java.net.http.HttpClient;
import java.util.List;

import static com.cannestro.drafttable.core.assumptions.DraftTableAssumptions.assumeColumnsHaveUniformSize;
import static com.cannestro.drafttable.core.assumptions.DraftTableAssumptions.assumeRowsHaveEquivalentKeySets;
import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeUniformityOf;
import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeUniquenessOf;
import static com.cannestro.drafttable.core.tables.DraftTable.DEFAULT_TABLE_NAME;
import static com.cannestro.drafttable.supporting.utils.ListUtils.firstElementOf;
import static java.util.Collections.emptyList;


public class FlexibleDraftTableCreator implements TableCreator {

    @Override
    public DraftTable emptyDraftTable() {
        return new FlexibleDraftTable(DEFAULT_TABLE_NAME, emptyList());
    }

    @Override
    public DraftTable fromColumns(@NonNull String tableName, @NonNull List<Column> columns) {
        if(columns.isEmpty()) {
            return emptyDraftTable();
        }
        assumeUniquenessOf(columns.stream().map(Column::label).toList());
        assumeColumnsHaveUniformSize(columns);
        return new FlexibleDraftTable(tableName, columns);
    }

    @Override
    public <R extends Row> DraftTable fromRows(@NonNull String tableName, @NonNull List<R> listOfRows) {
        if(listOfRows.isEmpty()) {
            return emptyDraftTable();
        }
        assumeRowsHaveEquivalentKeySets(listOfRows);
        return new FlexibleDraftTable(
                tableName,
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
    public <M extends Mappable> DraftTable fromObjects(@NonNull String tableName, @NonNull List<M> objects) {
        return fromRows(tableName, objects.stream().map(HashMapRow::from).toList());
    }

    @Override
    public DraftTable fromColumnValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table) {
        assumeUniquenessOf(columnNames);
        assumeUniformityOf(table);
        return new FlexibleDraftTable(
                DEFAULT_TABLE_NAME,
                StreamsUtils.zip(columnNames.stream(), table.stream(), FlexibleColumn::new)
                        .map(Column.class::cast)
                        .toList()
        );
    }

    @Override
    public DraftTable fromRowValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table) {
        assumeUniquenessOf(columnNames);
        assumeUniformityOf(table);
        return fromRows(
                DEFAULT_TABLE_NAME,
                table.stream()
                        .map(rowValues -> MapHelper.zip(columnNames, rowValues))
                        .map(HashMapRow::new)
                        .toList()
        );
    }

    @Override
    public CsvLoader fromCsv() {
        return new DefaultCsvLoader();
    }

    @Override
    public HttpLoader fromHttp(@NonNull HttpClient client) {
        return new DefaultHttpLoader(client);
    }

    @Override
    public JsonLoader fromJsonArray() {
        return new DefaultJsonLoader();
    }

}
