package com.cannestro.drafttable.supporting.utils;

import com.cannestro.drafttable.supporting.utils.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


@Test(groups = {"component"})
public class TestFileUtils {

    @Test
    public void canLocateExistingCsvFileFromResourcePath() {
        String resourceFilePath = "csv/example_jobs.csv";
        Assert.assertTrue(FileUtils.searchForResource(resourceFilePath).getParent().endsWith("csv"));
        Assert.assertEquals(FileUtils.searchForResource(resourceFilePath).getFileName().toString(), "example_jobs.csv");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void throwsExpectedExceptionWhenProvidedInvalidPath() {
        FileUtils.searchForResource("INVALID/resourceFilePath");
    }

}
