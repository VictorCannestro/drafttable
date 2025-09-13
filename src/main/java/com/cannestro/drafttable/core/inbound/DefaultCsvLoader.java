package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import com.cannestro.drafttable.supporting.utils.MapUtils;
import lombok.NonNull;

import java.util.List;
import java.util.stream.IntStream;

import static com.cannestro.drafttable.supporting.csv.CsvDataParser.csvBeanBuilder;
import static com.cannestro.drafttable.supporting.csv.CsvDataParser.readAllLines;
import static com.cannestro.drafttable.supporting.utils.ListUtils.firstElementOf;


public class DefaultCsvLoader implements CsvLoader {

    @Override
    public DraftTable at(@NonNull String filePath) {
        List<List<String>> fullTable = readAllLines(filePath);
        List<String> headers = firstElementOf(fullTable);
        List<List<String>> tableData = fullTable.subList(1, fullTable.size());
        return FlexibleDraftTable.create().fromRows(
                IntStream.range(1, tableData.size())
                        .mapToObj(rowIndex -> MapUtils.zip(headers, tableData.get(rowIndex)))
                        .map(HashMapRow::new)
                        .map(Row.class::cast)
                        .toList()
        );
    }

    @Override
    public DraftTable at(@NonNull String filePath, @NonNull CsvLoadingOptions loadingOptions) {
        return FlexibleDraftTable.create().fromObjects(csvBeanBuilder(filePath,loadingOptions));
    }

    public DraftTable load(@NonNull String filePath, @NonNull Class<? extends CsvBean> csvSchema) {
        return FlexibleDraftTable.create().fromObjects(csvBeanBuilder(filePath, csvSchema));
    }

}
