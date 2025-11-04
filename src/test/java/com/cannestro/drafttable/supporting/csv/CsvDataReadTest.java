package com.cannestro.drafttable.supporting.csv;

import com.cannestro.drafttable.core.inbound.CsvOptions;
import com.cannestro.drafttable.supporting.csv.pojo.CustomizableCsvOptions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.cannestro.drafttable.supporting.utils.Constants.TEST_CSV_DIRECTORY;


@Test(groups = {"component"})
public class CsvDataReadTest {

    @Test(dataProvider = "supportedFormatsData")
    public void canReadAndParseSupportedFormats(String filePath, CsvOptions options) {
        List<List<String>> parsedLines = CsvDataParser.readAllLines(filePath, options);
        Assert.assertEquals(parsedLines.size(), 11);
        Assert.assertEquals(parsedLines.get(0).size(), 11);
    }

    @DataProvider(name = "supportedFormatsData")
    public Object[][] supportedFormatsData() {
        return new Object[][] {
                {TEST_CSV_DIRECTORY.concat("some_tornadoes.csv"), CustomizableCsvOptions.allDefaults()},
                {TEST_CSV_DIRECTORY.concat("some_tornadoes.tsv"), CustomizableCsvOptions.builder().delimiter('\t').build()},
                {TEST_CSV_DIRECTORY.concat("some_tornadoes.txt"), CustomizableCsvOptions.builder().delimiter('|').build()}
        };
    }

}
