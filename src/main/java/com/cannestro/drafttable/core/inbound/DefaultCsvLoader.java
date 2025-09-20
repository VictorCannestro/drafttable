package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import lombok.NonNull;

import java.util.List;
import java.util.stream.IntStream;

import static com.cannestro.drafttable.supporting.csv.CsvDataParser.buildBeansFrom;
import static com.cannestro.drafttable.supporting.csv.CsvDataParser.readAllLines;
import static com.cannestro.drafttable.supporting.utils.ListUtils.firstElementOf;
import static com.cannestro.drafttable.supporting.utils.MapUtils.zip;
import static com.google.common.io.Files.getNameWithoutExtension;


public class DefaultCsvLoader implements CsvLoader {

    @Override
    public DraftTable at(@NonNull String filePath) {
        List<List<String>> fullTable = readAllLines(filePath);
        List<String> headers = firstElementOf(fullTable);
        List<List<String>> tableData = fullTable.subList(1, fullTable.size());
        return FlexibleDraftTable.create().fromRows(
                getNameWithoutExtension(filePath),
                IntStream.range(1, tableData.size())
                        .mapToObj(rowIndex -> zip(headers, tableData.get(rowIndex)))
                        .map(HashMapRow::new)
                        .toList()
        );
    }

    @Override
    public DraftTable at(@NonNull String filePath, @NonNull CsvOptions loadingOptions) {
        return FlexibleDraftTable.create().fromObjects(
                getNameWithoutExtension(filePath),
                buildBeansFrom(filePath, loadingOptions)
        );
    }

    public <T extends CsvBean & Mappable> DraftTable load(@NonNull String filePath, @NonNull Class<T> csvSchema) {
        return FlexibleDraftTable.create().fromObjects(
                getNameWithoutExtension(filePath),
                buildBeansFrom(filePath, csvSchema)
        );
    }

}
