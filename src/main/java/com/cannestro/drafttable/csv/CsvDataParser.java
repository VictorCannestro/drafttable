package com.cannestro.drafttable.csv;

import com.cannestro.drafttable.csv.beans.CsvBean;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import com.cannestro.drafttable.csv.pojo.CsvToListTransferrer;
import com.cannestro.drafttable.utils.FileUtils;
import com.cannestro.drafttable.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;


/**
 * @author Victor Cannestro
 */
@Slf4j
public class CsvDataParser {

    public static final String DEFAULT_IOEXCEPTION_LOG_STRING = "Could not open the file with the provided path: {}";
    public static final String EXCEPTION_MESSAGE = "CSV file could not be loaded";

    
    private CsvDataParser() {}


    public static List<String> mapCsvToJsonStrings(String resourceFilePath, Class<? extends CsvBean> csvBeanClass) {
        return JsonUtils.jsonStringListFrom(csvBeanBuilder(resourceFilePath, csvBeanClass));
    }

    /**
     * Maps the contents of the CSV located at resourceFilePath to its corresponding CSV representation.
     *
     * @param resourceFilePath A valid resourceFilePath to the CSV resource file to be read
     * @param csvBeanClass The {@code CsvBean} type representation of the CSV file located at resourceFilePath
     * @return A List of extracted {@code CsvBean} types where each bean maps to a row in the CSV
     */
    public static List<CsvBean> csvBeanBuilder(String resourceFilePath, Class<? extends CsvBean> csvBeanClass) {
        CsvToListTransferrer csvToListTransferrer = new CsvToListTransferrer();
        try (Reader reader = FileUtils.createReaderFromResource(resourceFilePath)) {
            CsvToBean<CsvBean> csvBean = new CsvToBeanBuilder<CsvBean>(reader)
                    .withType(csvBeanClass)
                    .build();
            csvToListTransferrer.setCsvList(csvBean.parse());
        } catch (IOException e) {
            log.debug(DEFAULT_IOEXCEPTION_LOG_STRING, resourceFilePath);
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }
        return csvToListTransferrer.getCsvList();
    }

    /**
     * @param resourceFilePath A valid resource file path to the CSV file to be read
     * @return A List of arrays, each corresponding to a row in the CSV file
     */
    public static List<List<String>> readAllLines(String resourceFilePath) {
        try (Reader reader = FileUtils.createReaderFromResource(resourceFilePath)) {
            return handleReadAllCallUsing(reader);
        } catch (IOException e) {
            log.debug(DEFAULT_IOEXCEPTION_LOG_STRING, resourceFilePath);
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }
    }

    static List<List<String>> handleReadAllCallUsing(Reader reader) {
        try (CSVReader csvReader = new CSVReader(reader)) {
            return csvReader.readAll().stream()
                    .map(line -> Arrays.stream(line).toList())
                    .toList();
        } catch (CsvException | IOException e) {
            log.debug("Encountered a exception while attempting to parse the csv file: " + e.getCause());
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }
    }

}
