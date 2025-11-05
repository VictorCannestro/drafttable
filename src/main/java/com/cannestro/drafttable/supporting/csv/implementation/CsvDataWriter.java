package com.cannestro.drafttable.supporting.csv.implementation;

import com.cannestro.drafttable.supporting.csv.CsvWritingOptions;
import com.cannestro.drafttable.supporting.csv.options.CustomizableWritingOptions;
import com.opencsv.CSVWriter;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ResultSetHelperService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Victor Cannestro
 */
@Slf4j
public class CsvDataWriter {


    private CsvDataWriter() {}

    /**
     * Exports the provided list of line data to the destination filepath using comma delimiters <b>in the user's
     * defined order</b>. Will create a new file, if necessary, otherwise the existing file will be updated. Note that
     * the order of the headers <b>must match</b> the order of the data within each line to establish data integrity.
     * All missing data in a given line, relative to the number of headers, will be filled with a user defined value.
     *
     * @param filePath The destination filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     * @param lines A nested list of data where each line will be mapped to a row
     * @param headers The column name labels
     * @param writeOptions Any customized CSV export options
     * @return A flag indicating success or failure of the write operation
     */
    public static boolean writeAllLinesToCsv(@NonNull String filePath,
                                             @NonNull List<String> headers,
                                             @NonNull List<List<String>> lines,
                                             @NonNull CsvWritingOptions writeOptions) {
        log.debug("Attempting CSV export to: {}", filePath);
        FileUtils.touchFile(filePath);
        List<List<String>> data = new ArrayList<>(List.of(headers));
        data.addAll(lines);
        ResultSetHelperService resultSetHelper = new ResultSetHelperService();
        resultSetHelper.setNullDefault(writeOptions.fillerValue());
        try (CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter(Paths.get(filePath).toAbsolutePath().toFile()))
                .withSeparator(writeOptions.delimiter())
                .withEscapeChar(writeOptions.escapeCharacter())
                .withQuoteChar(writeOptions.quoteCharacter())
                .withLineEnd(writeOptions.lineEnder())
                .withResultSetHelper(resultSetHelper)
                .build()) {
            writer.writeAll(data.stream().map(line -> line.toArray(new String[0])).toList(), false);
        } catch (IOException e) {
            log.error("Could not export data to CSV. Encountered the following: {}", e.toString());
            return false;
        }
        log.debug("Successfully completed CSV export to: {}", filePath);
        return true;
    }

    /**
     * Exports the provided list of line data to the destination filepath using comma delimiters <b>in the user's
     * defined order</b>. Will create a new file, if necessary, otherwise the existing file will be updated. Note that
     * the order of the headers <b>must match</b> the order of the data within each line to establish data integrity.
     * All missing data in a given line, relative to the number of headers, will be filled with an empty string.
     *
     * @param filePath The destination filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     * @param lines A nested list of data where each line will be mapped to a row
     * @param headers The column name labels
     * @return A flag indicating success or failure of the write operation
     */
    public static boolean writeAllLinesToCsv(@NonNull String filePath,
                                             @NonNull List<String> headers,
                                             @NonNull List<List<String>> lines) {
        return writeAllLinesToCsv(filePath, headers, lines, CustomizableWritingOptions.allDefaults());
    }

}
