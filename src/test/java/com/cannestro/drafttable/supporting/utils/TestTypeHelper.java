package com.cannestro.drafttable.supporting.utils;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.supporting.options.SupportedExtension;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;


public class TestTypeHelper {

    record Point(int x, int y) {}
    record Line(Point p1, Point p2) {}
    record Data(int x, ArrayList<Integer> list) {}
    record Dangerous(Dangerous d) {}
    record Cycle1(Cycle2 two) {}
    record Cycle2(Cycle1 one) {}
    record La(int something, Di di) {}
    record Di(Da Da) {}
    record Da(La la) {}

    enum Values {
        A("a") {
            public String tripleIt() { return val + val + val; }
        };
        Values(String val) { this.val = val; }
        public final String val;
    }


    @Test(dataProvider = "immutableTypeTestData")
    public void immutableJavaTypesAreTreatedAsImmutable(Class<?> type) {
        Assert.assertTrue(TypeHelper.isKnownImmutable(type));
    }

    @Test(dataProvider = "mutableTypeTestData")
    public void mutableJavaTypesAreNotTreatedAsImmutable(Class<?> type) {
        Assert.assertFalse(TypeHelper.isKnownImmutable(type));
    }

    @Test
    public void selfReferringTypeIsTreatedAsMutable() {
        Assert.assertFalse(TypeHelper.isKnownImmutable(Dangerous.class));
    }

    @Test
    public void cyclingTypesAreTreatedAsMutable() {
        Assert.assertFalse(TypeHelper.isKnownImmutable(Cycle1.class));
        Assert.assertFalse(TypeHelper.isKnownImmutable(Cycle2.class));

        Assert.assertFalse(TypeHelper.isKnownImmutable(La.class));
        Assert.assertFalse(TypeHelper.isKnownImmutable(Di.class));
        Assert.assertFalse(TypeHelper.isKnownImmutable(Da.class));
    }


    @DataProvider(name = "immutableTypeTestData")
    static Object[][] immutableTypeTestData() {
        return new Object[][] {
                {Point.class},
                {int.class},
                {String.class},
                {Integer.class},
                {SupportedExtension.CSV.getClass()},
                {DayOfWeek.FRIDAY.getClass()},
                {Values.A.getClass()},
                {Line.class}
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
