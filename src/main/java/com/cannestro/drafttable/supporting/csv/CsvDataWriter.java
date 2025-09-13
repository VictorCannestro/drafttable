package com.cannestro.drafttable.supporting.csv;

import com.cannestro.drafttable.supporting.csv.strategies.AnnotationStrategy;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.cannestro.drafttable.supporting.utils.ListUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
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
     * Exports the provided list of CSV beans to the destination filepath using comma delimiters <b>in alphabetical
     * order</b>. Will create a new file, if necessary, otherwise the existing file will be updated.
     *
     * @param filePath The destination filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     * @param csvBeanList A list of csv beans to export
     * @return A flag indicating success or failure of the write operation
     */
    public static boolean exportBeansToCsv(String filePath, List<CsvBean> csvBeanList) {
        log.debug("Attempting to export CSV information to: {}", filePath);
        FileUtils.touchFile(filePath);
        try (Writer writer = new FileWriter(Paths.get(filePath).toAbsolutePath().toFile())) {
            StatefulBeanToCsv statefulBeanToCsv = new StatefulBeanToCsvBuilder<CsvBean>(writer)
                    .withMappingStrategy(new AnnotationStrategy(csvBeanList.iterator().next().getClass())) //
                    .withApplyQuotesToAll(false)
                    .withQuotechar('\'')
                    .withSeparator(ICSVWriter.DEFAULT_SEPARATOR)
                    .build();
            statefulBeanToCsv.write(csvBeanList);
            log.debug("Successfully completed CSV export to: {}", filePath);
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("Could not export data to the CSV. Encountered the following: {}", e.toString());
            return false;
        }
        return true;
    }

    /**
     * Exports the provided list of line data to the destination filepath using comma delimiters <b>in the user's
     * defined order</b>. Will create a new file, if necessary, otherwise the existing file will be updated. Note that
     * the order of the headers <b>must match</b> the order of the data within each line to establish data integrity.
     * All missing data in a given line, relative to the number of headers, will be filled with a user defined value.
     *
     * @param filePath The destination filepath, for example {@code ./src/main/resources/csv/export_file.csv}
     * @param lines A nested list of data where each line will be mapped to a row
     * @param headers The column name labels
     * @param fillValue The fill value for any missing data
     * @return A flag indicating success or failure of the write operation
     */
    public static boolean writeAllLinesToCsv(String filePath, List<List<String>> lines, List<String> headers, String fillValue) {
        log.debug("Attempting to export CSV information to: {}", filePath);
        FileUtils.touchFile(filePath);
        lines = new ArrayList<>(lines);
        try (CSVWriter writer = new CSVWriter(new FileWriter(Paths.get(filePath).toAbsolutePath().toFile()))) {
            lines.add(0, headers);
            writer.writeAll(
                    lines.stream()
                         .map(line -> ListUtils.fillToTargetLength(line, headers.size(), fillValue))
                         .map(line -> line.toArray(new String[0]))
                         .toList(),
                    false
            );
        } catch (IOException e) {
            log.error("Could not export data to the CSV. Encountered the following: {}", e.toString());
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
    public static boolean writeAllLinesToCsv(String filePath, List<List<String>> lines, List<String> headers) {
        return writeAllLinesToCsv(filePath, lines, headers, "");
    }

}
