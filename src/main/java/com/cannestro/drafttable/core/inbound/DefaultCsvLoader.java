package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.csv.assumptions.CsvAssumptions;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import com.cannestro.drafttable.supporting.csv.CsvParsingOptions;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static com.cannestro.drafttable.supporting.csv.implementation.CsvDataParser.buildBeansFrom;
import static com.cannestro.drafttable.supporting.csv.implementation.CsvDataParser.readAllLines;
import static com.cannestro.drafttable.supporting.utils.FileUtils.copyToTempDirectory;
import static com.cannestro.drafttable.supporting.utils.ListUtils.firstElementOf;
import static com.cannestro.drafttable.supporting.utils.MapUtils.zip;
import static java.util.Collections.emptyList;
import static java.util.Collections.nCopies;
import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
public class DefaultCsvLoader implements CsvLoader {

    @Override
    public DraftTable at(@NonNull Path path) {
        File file = path.toFile();
        CsvAssumptions.assumeFilenameIsCsvCompatible(file.getName());
        return createWithoutSchema(file.getPath(), null);
    }

    @Override
    public DraftTable at(@NonNull Path path, @NonNull CsvParsingOptions loadingOptions) {
        File file = path.toFile();
        CsvAssumptions.assumeFilenameIsCsvCompatible(file.getName());
        if (isNull(loadingOptions.type())) {
            return createWithoutSchema(file.getPath(), loadingOptions);
        } else {
            return FlexibleDraftTable.create().fromObjects(
                    FilenameUtils.getName(file.getName()),
                    buildBeansFrom(path.toFile().getPath(), loadingOptions)
            );
        }
    }

    @Override
    public DraftTable at(@NonNull URL url) {
        CsvAssumptions.assumeFilenameIsCsvCompatible(url.toString());
        File file = copyToTempDirectory(url);
        DraftTable draftTable = createWithoutSchema(file.getPath(), null);
        FileUtils.cleanUpTemporaryFiles(file);
        return draftTable;
    }

    @Override
    public DraftTable at(@NonNull URL url, @NonNull CsvParsingOptions loadingOptions) {
        CsvAssumptions.assumeFilenameIsCsvCompatible(url.toString());
        File file = copyToTempDirectory(url);
        DraftTable draftTable = isNull(loadingOptions.type())
                ? createWithoutSchema(file.getPath(), loadingOptions)
                : FlexibleDraftTable.create().fromObjects(
                        FilenameUtils.getName(file.getName()),
                        buildBeansFrom(file.getPath(), loadingOptions)
                  );
        FileUtils.cleanUpTemporaryFiles(file);
        return draftTable;
    }


    public <T extends CsvBean & Mappable> DraftTable load(@NonNull File file, @NonNull Class<T> csvSchema) {
        CsvAssumptions.assumeFilenameIsCsvCompatible(file.getName());
        return FlexibleDraftTable.create().fromObjects(
                FilenameUtils.getName(file.getName()),
                buildBeansFrom(file.getPath(), csvSchema)
        );
    }

    DraftTable createWithoutSchema(@NonNull String pathToFile, @Nullable CsvParsingOptions loadingOptions) {
        List<List<String>> fullTable = isNull(loadingOptions)
                ? readAllLines(pathToFile)
                : readAllLines(pathToFile, loadingOptions);
        if (fullTable.isEmpty()) {
            return FlexibleDraftTable.create()
                    .emptyDraftTable()
                    .nameTable(FilenameUtils.getName(pathToFile));
        }
        List<String> headers = firstElementOf(fullTable);
        List<List<String>> tableData = fullTable.subList(1, fullTable.size());
        if (tableData.isEmpty()) {
            return FlexibleDraftTable.create()
                    .fromColumnValues(headers, nCopies(headers.size(), emptyList()))
                    .nameTable(FilenameUtils.getName(pathToFile));
        }
        return FlexibleDraftTable.create().fromRows(
                FilenameUtils.getName(pathToFile),
                IntStream.range(1, tableData.size())
                        .mapToObj(rowIndex -> zip(headers, tableData.get(rowIndex)))
                        .map(HashMapRow::new)
                        .toList()
        );
    }

}
