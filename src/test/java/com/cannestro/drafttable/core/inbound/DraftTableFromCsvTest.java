package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.csv.implementation.CsvDataParser;
import com.cannestro.drafttable.supporting.csv.options.CustomizableWritingOptions;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.cannestro.drafttable.helper.PayDetails;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.cannestro.drafttable.Constants.TEST_CSV_DIRECTORY;
import static org.hamcrest.Matchers.is;


@Test(groups = {"component"})
public class DraftTableFromCsvTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unsupportedFileFormatsRaiseException() {
        FlexibleDraftTable.create().fromCsv().at(Path.of("something.json"));
    }

    @Test
    public void canCreateFromCsvWhenOnlyHeadersPresent() {
        DraftTable df = FlexibleDraftTable.create().fromCsv().at(Path.of(TEST_CSV_DIRECTORY.concat("no_tornadoes.csv")));
        Assert.assertEquals(df.rowCount(), 0);
        Assert.assertEquals(df.columnCount(), 11);
    }

    @Test
    public void endToEndDataFrameCsvTest() {
        List<String> headers = List.of("type", "rate", "period", "workHours");
        List<List<?>> lines = List.of(
                List.of("Hourly", "25.00", "Bi-Weekly", "80"),
                List.of("Hourly", "18.50", "", "80"),
                List.of("Salary", "50000.00", "Bi-Weekly", "80")
        );
        FlexibleDraftTable.create()
                .fromRowValues(headers, lines)
                .write()
                .toCSV(new File(TEST_CSV_DIRECTORY.concat("temp_3.csv")), CustomizableWritingOptions.builder().fillerValue("NULL").build());
        Assert.assertEquals(
                CsvDataParser.mapCsvToJsonStrings(TEST_CSV_DIRECTORY.concat("temp_3.csv"), PayDetails.class).size(),
                3
        );

        DraftTable df = FlexibleDraftTable.create()
                .fromCsv(DefaultCsvLoader.class)
                .load(Path.of(TEST_CSV_DIRECTORY.concat("temp_3.csv")), PayDetails.class)
                .where("type", is("Salary"));
        Assert.assertEquals(df.rowCount(), 1);
    }

    @Test
    public void endToEndCsvExportWithOptionsTest() {
        List<String> headers = List.of("type", "rate", "period", "workHours");
        List<List<?>> lines = List.of(
                List.of("Hourly", "25.00", "Bi-Weekly", "80"),
                List.of("Hourly", "18.50", "Bi-Weekly", "80"),
                List.of("Salary", "50000.00", "Bi-Weekly", "80")
        );

        FlexibleDraftTable.create().fromRowValues(headers, lines)
                .write()
                .toCSV(new File(TEST_CSV_DIRECTORY.concat("temp_4.csv")), CustomizableWritingOptions.allDefaults());
        DraftTable df = FlexibleDraftTable.create().fromCsv().at(Path.of(TEST_CSV_DIRECTORY.concat("temp_4.csv")));
        Assert.assertEqualsNoOrder(
                df.columnNames(),
                List.of("type", "rate", "period", "workHours")
        );
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        FileUtils.deleteFileIfPresent(TEST_CSV_DIRECTORY.concat("temp_3.csv"));
        FileUtils.deleteFileIfPresent(TEST_CSV_DIRECTORY.concat("temp_4.csv"));
    }

}
