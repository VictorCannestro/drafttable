package com.cannestro.drafttable.supporting.csv.implementation;

import com.cannestro.drafttable.supporting.csv.CsvWritingOptions;
import com.cannestro.drafttable.supporting.csv.options.CustomizableWritingOptions;
import com.opencsv.CSVWriter;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ResultSetHelperService;
import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Victor Cannestro
 */
@Slf4j
public class CsvDataWriter {


    private CsvDataWriter() {}

    /**
     * Exports the provided list of line data to the destination filepath using comma delimiters, unless otherwise
     * specified, <b>in the user's defined order</b>. Will create a new file, if necessary, otherwise the existing file
     * will be updated. Note that the order of the headers <b>must match</b> the order of the data within each line to
     * establish data integrity.
     *
     * @param file The destination file containing the filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     * @param lines A nested list of data where each line will be mapped to a row
     * @param headers The column name labels
     * @param writeOptions Any customized CSV export options
     * @return A flag indicating success or failure of the write operation
     */
    public static boolean writeAllLinesToCsv(@NonNull File file,
                                             @NonNull List<String> headers,
                                             @NonNull List<List<String>> lines,
                                             @NonNull CsvWritingOptions writeOptions) {
        log.debug("Attempting CSV export to: {}", file.getName());
        FileUtils.touchFile(file);
        List<List<String>> data = new ArrayList<>(List.of(headers));
        data.addAll(lines);
        ResultSetHelperService resultSetHelper = new ResultSetHelperService();
        resultSetHelper.setNullDefault(writeOptions.fillerValue());
        try (CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter(file))
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
        log.debug("Successfully completed CSV export to: {}", file.getAbsolutePath());
        return true;
    }

    /**
     * Exports the provided list of line data to the destination filepath using a comma delimiter, new line escape
     * character, double quote as the quote character, and an empty string as the filler value <b>in the user's
     * defined order</b>. Will create a new file, if necessary, otherwise the existing file will be updated. Note that
     * the order of the headers <b>must match</b> the order of the data within each line to establish data integrity.
     *
     * @param file The destination file containing the filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     * @param lines A nested list of data where each line will be mapped to a row
     * @param headers The column name labels
     * @return A flag indicating success or failure of the write operation
     */
    public static boolean writeAllLinesToCsv(@NonNull File file,
                                             @NonNull List<String> headers,
                                             @NonNull List<List<String>> lines) {
        return writeAllLinesToCsv(file, headers, lines, CustomizableWritingOptions.allDefaults());
    }

}
