package com.cannestro.drafttable.supporting.csv;

import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.cannestro.drafttable.supporting.utils.helper.Pay;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static com.cannestro.drafttable.supporting.utils.Constants.TEST_CSV_DIRECTORY;
import static com.cannestro.drafttable.supporting.utils.ListUtils.*;


@Test(groups = {"component"})
public class CsvDataReadWriteTest {

    @Test
    public void endToEndWriteAllLinesToCsvTest() {
        List<String> headers = List.of("type", "rate", "period", "workHours");
        List<List<String>> lines = List.of(
                List.of("Hourly", "25.00", "Bi-Weekly", "80"),
                List.of("Hourly", "18.50", "", "80"),
                List.of("Salary", "50000.00", "Bi-Weekly", "80")
        );
        CsvDataWriter.writeAllLinesToCsv(TEST_CSV_DIRECTORY.concat("temp_1.csv"), lines, headers, "NULL");

        Assert.assertEquals(lines.size(), 3);
        Assert.assertEquals(
                CsvDataParser.mapCsvToJsonStrings("csv/temp_1.csv", Pay.class).size(),
                3
        );

        FileUtils.deleteFileIfPresent(TEST_CSV_DIRECTORY.concat("temp_1.csv"));
    }

    @Test(description = "Can export and read CSV data when given an extra column header and the fill value matches what is expected")
    public void endToEndWriteAllLinesToCsvTestWithMoreColumnHeadersThanInRows() {
        List<String> headers = List.of("type", "rate", "period", "workHours", "extra_header");
        List<List<String>> lines = List.of(
                List.of("Hourly", "25.00", "Bi-Weekly", "80"),
                List.of("Hourly", "18.50", "Bi-Weekly", "80"),
                List.of("Salary", "50000.00", "Bi-Weekly", "80")
        );
        CsvDataWriter.writeAllLinesToCsv(TEST_CSV_DIRECTORY.concat("temp_2.csv"), lines, headers);
        List<List<String>> parsedLines = CsvDataParser.readAllLines("csv/temp_2.csv");

        Assert.assertEquals(
                lastElementOf(firstElementOf(parsedLines)),
                "extra_header"
        );
        Assert.assertEquals(
                lastElementOf(lastElementOf(parsedLines)),
                ""
        );

        FileUtils.deleteFileIfPresent(TEST_CSV_DIRECTORY.concat("temp_2.csv"));
    }

}
