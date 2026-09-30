package com.cannestro.drafttable.supporting.utils;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.supporting.options.SupportedExtension;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;


public class TestTypeHelper {

    record Point(int x, int y) {}
    record Data(int x, ArrayList<Integer> list) {}


    @Test(dataProvider = "mutableTypeTestData")
    public void mutableJavaTypesAreNotTreatedAsImmutable(Class<?> type) {
        Assert.assertFalse(TypeHelper.isKnownImmutable(type));
    }

    @Test
    public void recordOfPrimitivesIsKnownImmutable() {
        Class<?> type = Point.class;
        Assert.assertTrue(TypeHelper.isKnownImmutable(type));
    }

    @Test
    public void recordOfNonPrimitivesIsKnownMutable() {
        Class<?> type = Data.class;
        Assert.assertFalse(TypeHelper.isKnownImmutable(type));
    }


    @DataProvider(name = "immutableTypeTestData")
    static Object[][] immutableTypeTestData() {
        return new Object[][] {
                {Point.class},
                {int.class},
                {String.class},
                {Integer.class},
                {SupportedExtension.CSV.getClass()}
        };
    }

    @DataProvider(name = "mutableTypeTestData")
    static Object[][] mutableTypeTestData() {
        return new Object[][] {
                {DateTimeFormatterBuilder.class},
                {java.util.Date.class},
                {java.util.Calendar.class},
                {java.time.zone.ZoneRulesProvider.class},
                {Column.class},
                {Data.class},
                {java.util.List.class}
        };
    }

}
