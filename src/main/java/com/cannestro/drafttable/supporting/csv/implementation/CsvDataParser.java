package com.cannestro.drafttable.supporting.csv.implementation;

import com.cannestro.drafttable.supporting.csv.CsvEssentials;
import com.cannestro.drafttable.supporting.csv.CsvParsingOptions;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import com.cannestro.drafttable.supporting.utils.FileHelper;
import com.cannestro.drafttable.supporting.utils.JsonHelper;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;


/**
 * @author Victor Cannestro
 */
public class CsvDataParser {
    
    private CsvDataParser() {}

    /**
     * @param resourceFilePath A valid resource file path to the CSV file to be read
     * @return A List of arrays, each corresponding to a row in the CSV file
     */
    public static List<List<String>> readAllLines(@NonNull String resourceFilePath) {
        try (CSVReader csvReader = new CSVReader(FileHelper.createReaderFromResource(resourceFilePath, CsvEssentials.DEFAULT_CHARSET))) {
            return csvReader.readAll().stream().map(line -> Arrays.stream(line).toList()).toList();
        } catch (IOException | CsvException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static List<List<String>> readAllLines(@NonNull String resourceFilePath, @NonNull CsvParsingOptions loadingOptions) {
        try (CSVReader csvReader = new CSVReaderBuilder(FileHelper.createReaderFromResource(resourceFilePath, loadingOptions.charset()))
                .withSkipLines(loadingOptions.skipLines())
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(loadingOptions.delimiter())
                        .withEscapeChar(loadingOptions.escapeCharacter())
                        .withQuoteChar(loadingOptions.quoteCharacter())
                        .withStrictQuotes(loadingOptions.useStrictQuotes())
                        .withIgnoreQuotations(loadingOptions.ignoreQuotations())
                        .withIgnoreLeadingWhiteSpace(loadingOptions.ignoreLeadingWhiteSpace())
                        .build())
                .build()) {
            return csvReader.readAll().stream().map(line -> Arrays.stream(line).toList()).toList();
        } catch (CsvException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T extends CsvBean> List<T> buildBeansFrom(@NonNull String resourceFilePath, @NonNull CsvParsingOptions loadingOptions) {
        try (Reader reader = FileHelper.createReaderFromResource(resourceFilePath, loadingOptions.charset())) {
            CsvToBean<T> csvBean = new CsvToBeanBuilder<T>(reader)
                    .withIgnoreEmptyLine(true)
                    .withSeparator(loadingOptions.delimiter())
                    .withSkipLines(loadingOptions.skipLines())
                    .withEscapeChar(loadingOptions.escapeCharacter())
                    .withQuoteChar(loadingOptions.quoteCharacter())
                    .withStrictQuotes(loadingOptions.useStrictQuotes())
                    .withIgnoreQuotations(loadingOptions.ignoreQuotations())
                    .withIgnoreLeadingWhiteSpace(loadingOptions.ignoreLeadingWhiteSpace())
                    .withType(loadingOptions.type())
                    .build();
            return csvBean.parse();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Maps the contents of the CSV located at resourceFilePath to its corresponding CSV representation.
     *
     * @param resourceFilePath A valid resourceFilePath to the CSV resource file to be read
     * @param csvBeanClass The {@code CsvBean} type representation of the CSV file located at resourceFilePath
     * @return A List of extracted {@code CsvBean} types where each bean maps to a row in the CSV
     */
    public static <T extends CsvBean> List<T> buildBeansFrom(@NonNull String resourceFilePath, @NonNull Class<T> csvBeanClass) {
        try (Reader reader = FileHelper.createReaderFromResource(resourceFilePath, CsvEssentials.DEFAULT_CHARSET)) {
            return new CsvToBeanBuilder<T>(reader).withType(csvBeanClass).build().parse();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static List<String> mapCsvToJsonStrings(@NonNull String resourceFilePath, @NonNull Class<? extends CsvBean> csvBeanClass) {
        return JsonHelper.jsonStringListFrom(buildBeansFrom(resourceFilePath, csvBeanClass));
    }

}
