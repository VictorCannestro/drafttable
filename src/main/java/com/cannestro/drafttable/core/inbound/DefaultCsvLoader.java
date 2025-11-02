package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static com.cannestro.drafttable.supporting.csv.CsvDataParser.buildBeansFrom;
import static com.cannestro.drafttable.supporting.csv.CsvDataParser.readAllLines;
import static com.cannestro.drafttable.supporting.utils.FileUtils.copyToTempDirectory;
import static com.cannestro.drafttable.supporting.utils.FileUtils.deleteFileIfPresent;
import static com.cannestro.drafttable.supporting.utils.ListUtils.firstElementOf;
import static com.cannestro.drafttable.supporting.utils.MapUtils.zip;
import static com.google.common.io.Files.getNameWithoutExtension;
import static java.util.Objects.isNull;


public class DefaultCsvLoader implements CsvLoader {

    @Override
    public DraftTable at(@NonNull Path path) {
        String pathName = path.toFile().getPath();
        return createWithoutSchema(pathName, null);
    }

    @Override
    public DraftTable at(@NonNull Path path, @NonNull CsvOptions loadingOptions) {
        String pathName = path.toFile().getPath();
        if (isNull(loadingOptions.type())) {
            return createWithoutSchema(pathName, loadingOptions);
        } else {
            return FlexibleDraftTable.create().fromObjects(
                    getNameWithoutExtension(pathName),
                    buildBeansFrom(pathName, loadingOptions)
            );
        }
    }

    @Override
    public DraftTable at(@NonNull URL url) {
        File file = copyToTempDirectory(url);
        DraftTable draftTable = createWithoutSchema(file.getPath(), null);
        cleanUpTemporaryFiles(file);
        return draftTable;
    }

    @Override
    public DraftTable at(@NonNull URL url, @NonNull CsvOptions loadingOptions) {
        File file = copyToTempDirectory(url);
        DraftTable draftTable = isNull(loadingOptions.type())
                ? createWithoutSchema(file.getPath(), loadingOptions)
                : FlexibleDraftTable.create().fromObjects(getNameWithoutExtension(file.getPath()), buildBeansFrom(file.getPath(), loadingOptions));
        cleanUpTemporaryFiles(file);
        return draftTable;
    }


    public <T extends CsvBean & Mappable> DraftTable load(@NonNull Path path, @NonNull Class<T> csvSchema) {
        String pathName = path.toFile().getPath();
        return FlexibleDraftTable.create().fromObjects(
                getNameWithoutExtension(pathName),
                buildBeansFrom(pathName, csvSchema)
        );
    }

    DraftTable createWithoutSchema(@NonNull String pathName, @Nullable CsvOptions loadingOptions) {
        List<List<String>> fullTable = isNull(loadingOptions)
                ? readAllLines(pathName)
                : readAllLines(pathName, loadingOptions);
        if (fullTable.isEmpty()) {
            return FlexibleDraftTable.create().emptyDraftTable();
        }
        List<String> headers = firstElementOf(fullTable);
        List<List<String>> tableData = fullTable.subList(1, fullTable.size());
        return FlexibleDraftTable.create().fromRows(
                getNameWithoutExtension(pathName),
                IntStream.range(1, tableData.size())
                        .mapToObj(rowIndex -> zip(headers, tableData.get(rowIndex)))
                        .map(HashMapRow::new)
                        .toList()
        );
    }

    void cleanUpTemporaryFiles(@NonNull File file) {
        deleteFileIfPresent(file.getPath());
        deleteFileIfPresent(file.getParentFile().getPath());
    }

}
