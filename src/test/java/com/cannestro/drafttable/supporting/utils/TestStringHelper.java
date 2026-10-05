package com.cannestro.drafttable.supporting.utils;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class TestStringHelper {

    @Test(dataProvider = "safeConcatTestData")
    public void canSafeConcatAsExpected(String expected, String... testData) {
        Assert.assertEquals(StringHelper.safeConcat(testData), expected);
    }

    @DataProvider(name = "safeConcatTestData")
    static Object[][] safeConcatTestData() {
        return new Object[][] {
                {"", new String[]{"", ""}},
                {"", new String[]{null, null}},
                {"", new String[]{null, ""}},
                {"1", new String[]{"1", null}},
                {"test", new String[]{"te", "st"}},
        };
    }

}
