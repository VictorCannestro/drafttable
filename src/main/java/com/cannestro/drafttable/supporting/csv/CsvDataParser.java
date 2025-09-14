package com.cannestro.drafttable.supporting.csv;

import com.cannestro.drafttable.core.inbound.CsvLoadingOptions;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import com.cannestro.drafttable.supporting.csv.pojo.CsvToListTransferrer;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.cannestro.drafttable.supporting.utils.JsonUtils;
import lombok.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;


/**
 * @author Victor Cannestro
 */
public class CsvDataParser {

    public static final String EXCEPTION_MESSAGE = "CSV file could not be loaded";

    
    private CsvDataParser() {}


    public static List<String> mapCsvToJsonStrings(@NonNull String resourceFilePath, @NonNull Class<? extends CsvBean> csvBeanClass) {
        return JsonUtils.jsonStringListFrom(buildBeansFrom(resourceFilePath, csvBeanClass));
    }

    public static <T extends CsvBean> List<T> buildBeansFrom(@NonNull String resourceFilePath, @NonNull CsvLoadingOptions loadingOptions) {
        CsvToListTransferrer<T> csvToListTransferrer = new CsvToListTransferrer<>();
        try (Reader reader = FileUtils.createReaderFromResource(resourceFilePath)) {
            CsvToBean<T> csvBean = new CsvToBeanBuilder<T>(reader)
                    .withEscapeChar(loadingOptions.escapeCharacter())
                    .withQuoteChar(loadingOptions.quoteCharacter())
                    .withIgnoreEmptyLine(loadingOptions.ignoreEmptyLines())
                    .withIgnoreQuotations(loadingOptions.ignoreQuotations())
                    .withIgnoreLeadingWhiteSpace(loadingOptions.ignoreLeadingWhiteSpace())
                    .withType(loadingOptions.type())
                    .build();
            csvToListTransferrer.setCsvList(csvBean.parse());
        } catch (IOException e) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }
        return csvToListTransferrer.getCsvList();
    }


    /**
     * Maps the contents of the CSV located at resourceFilePath to its corresponding CSV representation.
     *
     * @param resourceFilePath A valid resourceFilePath to the CSV resource file to be read
     * @param csvBeanClass The {@code CsvBean} type representation of the CSV file located at resourceFilePath
     * @return A List of extracted {@code CsvBean} types where each bean maps to a row in the CSV
     */
    public static <T extends CsvBean> List<T> buildBeansFrom(@NonNull String resourceFilePath, @NonNull Class<T> csvBeanClass) {
        CsvToListTransferrer<T> csvToListTransferrer = new CsvToListTransferrer<>();
        try (Reader reader = FileUtils.createReaderFromResource(resourceFilePath)) {
            CsvToBean<T> csvBean = new CsvToBeanBuilder<T>(reader)
                    .withType(csvBeanClass)
                    .build();
            csvToListTransferrer.setCsvList(csvBean.parse());
        } catch (IOException e) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }
        return csvToListTransferrer.getCsvList();
    }

    /**
     * @param resourceFilePath A valid resource file path to the CSV file to be read
     * @return A List of arrays, each corresponding to a row in the CSV file
     */
    public static List<List<String>> readAllLines(@NonNull String resourceFilePath) {
        try (Reader reader = FileUtils.createReaderFromResource(resourceFilePath)) {
            return handleReadAllCallUsing(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }
    }

    static List<List<String>> handleReadAllCallUsing(@NonNull Reader reader) {
        try (CSVReader csvReader = new CSVReader(reader)) {
            return csvReader.readAll().stream()
                    .map(line -> Arrays.stream(line).toList())
                    .toList();
        } catch (CsvException | IOException e) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }
    }

}
