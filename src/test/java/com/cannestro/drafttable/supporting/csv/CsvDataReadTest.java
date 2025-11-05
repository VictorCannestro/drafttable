package com.cannestro.drafttable.supporting.csv;

import com.cannestro.drafttable.supporting.csv.implementation.CsvDataParser;
import com.cannestro.drafttable.supporting.csv.options.CustomizableParsingOptions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.cannestro.drafttable.Constants.TEST_CSV_DIRECTORY;


@Test(groups = {"component"})
public class CsvDataReadTest {

    @Test(dataProvider = "supportedFormatsData")
    public void canReadAndParseSupportedFormats(String filePath, CsvParsingOptions options) {
        List<List<String>> parsedLines = CsvDataParser.readAllLines(filePath, options);
        Assert.assertEquals(parsedLines.size(), 11);
        Assert.assertEquals(parsedLines.get(0).size(), 11);
    }

    @DataProvider(name = "supportedFormatsData")
    public Object[][] supportedFormatsData() {
        return new Object[][] {
                {TEST_CSV_DIRECTORY.concat("some_tornadoes.csv"), CustomizableParsingOptions.allDefaults()},
                {TEST_CSV_DIRECTORY.concat("some_tornadoes.tsv"), CustomizableParsingOptions.builder().delimiter('\t').build()},
                {TEST_CSV_DIRECTORY.concat("some_tornadoes.txt"), CustomizableParsingOptions.builder().delimiter('|').build()}
        };
    }

}
