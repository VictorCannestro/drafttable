package com.cannestro.drafttable.core.implementations.tables;

import com.cannestro.drafttable.core.Column;
import com.cannestro.drafttable.core.DraftTable;
import com.cannestro.drafttable.core.Row;
import com.cannestro.drafttable.core.TableCreator;
import com.cannestro.drafttable.core.implementations.columns.FlexibleColumn;
import com.cannestro.drafttable.core.implementations.rows.HashMapRow;
import com.cannestro.drafttable.csv.beans.CsvBean;
import com.cannestro.drafttable.utils.MapUtils;
import lombok.NonNull;
import org.paumard.streams.StreamsUtils;

import java.util.List;
import java.util.stream.IntStream;

import static com.cannestro.drafttable.core.assumptions.DraftTableAssumptions.assumeColumnsHaveUniformSize;
import static com.cannestro.drafttable.core.assumptions.DraftTableAssumptions.assumeRowsHaveEquivalentKeySet;
import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeUniformityOf;
import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeUnique;
import static com.cannestro.drafttable.csv.CsvDataParser.csvBeanBuilder;
import static com.cannestro.drafttable.csv.CsvDataParser.readAllLines;
import static com.cannestro.drafttable.utils.ListUtils.firstElementOf;
import static java.util.Collections.emptyList;


public class FlexibleDraftTableCreator implements TableCreator {

    @Override
    public DraftTable emptyDraftTable() {
        return new FlexibleDraftTable(emptyList());
    }

    @Override
    public <T> DraftTable fromObjects(@NonNull List<T> objects) {
        return fromRows(
                objects.stream()
                        .map(HashMapRow::from)
                        .map(Row.class::cast)
                        .toList()
        );
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
    public DraftTable fromRows(@NonNull List<Row> listOfRows) {
        if(listOfRows.isEmpty()) {
            return emptyDraftTable();
        }
        assumeRowsHaveEquivalentKeySet(listOfRows);
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
                        .map(Row.class::cast)
                        .toList()
        );
    }

    @Override
    public DraftTable fromCSV(@NonNull String filePath, @NonNull Class<? extends CsvBean> csvSchema) {
        return fromObjects(csvBeanBuilder(filePath, csvSchema));
    }

    @Override
    public DraftTable fromCSV(@NonNull String filePath) {
        List<List<String>> fullTable = readAllLines(filePath);
        List<String> headers = firstElementOf(fullTable);
        List<List<String>> tableData = fullTable.subList(1, fullTable.size());
        return fromRows(
                IntStream.range(1, tableData.size())
                        .mapToObj(rowIndex -> MapUtils.zip(headers, tableData.get(rowIndex)))
                        .map(HashMapRow::new)
                        .map(Row.class::cast)
                        .toList()
        );
    }

}
