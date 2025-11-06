package com.cannestro.drafttable.supporting.csv;

import com.cannestro.drafttable.supporting.csv.implementation.CsvDataParser;
import com.cannestro.drafttable.supporting.csv.implementation.CsvDataWriter;
import com.cannestro.drafttable.supporting.csv.options.CustomizableWritingOptions;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.cannestro.drafttable.helper.PayDetails;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static com.cannestro.drafttable.Constants.TEST_CSV_DIRECTORY;


@Test(groups = {"component"})
public class CsvDataWriteTest {

    @Test
    public void endToEndWriteAllLinesToCsvTest() {
        List<String> headers = List.of("type", "rate", "period", "workHours");
        List<List<String>> lines = List.of(
                List.of("Hourly", "25.00", "Bi-Weekly", "80"),
                List.of("Hourly", "18.50", "", "80"),
                List.of("Salary", "50000.00", "Bi-Weekly", "80")
        );
        CsvDataWriter.writeAllLinesToCsv(new File(TEST_CSV_DIRECTORY.concat("temp_1.csv")), headers, lines, CustomizableWritingOptions.builder().fillerValue("NULL").build());

        Assert.assertEquals(lines.size(), 3);
        Assert.assertEquals(
                CsvDataParser.mapCsvToJsonStrings(TEST_CSV_DIRECTORY.concat("temp_1.csv"), PayDetails.class).size(),
                3
        );
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        FileUtils.deleteFileIfPresent(TEST_CSV_DIRECTORY.concat("temp_1.csv"));
    }

}
