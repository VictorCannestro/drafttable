package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.csv.CsvDataParser;
import com.cannestro.drafttable.supporting.csv.CsvDataWriter;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.cannestro.drafttable.supporting.utils.helper.PayDetails;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;

import static com.cannestro.drafttable.core.options.Item.as;
import static com.cannestro.drafttable.supporting.utils.Constants.TEST_CSV_DIRECTORY;
import static org.hamcrest.Matchers.is;


@Test(groups = {"component"})
public class DraftTableFromCsvTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unsupportedFileFormatsRaiseException() {
        FlexibleDraftTable.create().fromCSV().at(Path.of("something.json"));
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
                .toCSV(TEST_CSV_DIRECTORY.concat("temp_3.csv"), "NULL");
        Assert.assertEquals(
                CsvDataParser.mapCsvToJsonStrings(TEST_CSV_DIRECTORY.concat("temp_3.csv"), PayDetails.class).size(),
                3
        );

        DraftTable df = FlexibleDraftTable.create()
                .fromCSV(DefaultCsvLoader.class)
                .load(Path.of(TEST_CSV_DIRECTORY.concat("temp_3.csv")), PayDetails.class)
                .where("type", is("Salary"));
        Assert.assertEquals(df.rowCount(), 1);
    }

    @Test
    public void endToEndCsvBeanExportTest() {
        List<String> headers = List.of("type", "rate", "period", "workHours");
        List<List<?>> lines = List.of(
                List.of("Hourly", "25.00", "Bi-Weekly", "80"),
                List.of("Hourly", "18.50", "Bi-Weekly", "80"),
                List.of("Salary", "50000.00", "Bi-Weekly", "80")
        );
        CsvDataWriter.exportBeansToCsv(
                TEST_CSV_DIRECTORY.concat("temp_4.csv"),
                FlexibleDraftTable.create().fromRowValues(headers, lines).gatherInto(PayDetails.class, as("pay")).values()
        );
        DraftTable df = FlexibleDraftTable.create().fromCSV().at(Path.of(TEST_CSV_DIRECTORY.concat("temp_4.csv")));
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
