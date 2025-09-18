package com.cannestro.drafttable.core.rows;

import com.cannestro.drafttable.supporting.utils.helper.DailyHireCount;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;


@Test(groups = {"component"})
public class ExperimentalRowTest {

    @Test
    public void canCreateRowFromRecordContainingJavaTimeLocalDates() {
        Row row = ExperimentalRow.from(
                new DailyHireCount(100, LocalDate.of(2023, 1, 1))
        );

        Assert.assertTrue(row.hasKey("n"));
        Assert.assertTrue(row.hasKey("timeStamp"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotMapBackIntoIncompatibleType() {
        ExperimentalRow.from(new DailyHireCount(100, LocalDate.of(2023, 1, 1))).as(LocalDate.class);
    }

    @Test
    public void canMapBackIntoOriginatingType() {
        DailyHireCount hireCount = new DailyHireCount(100, LocalDate.of(2023, 1, 1));

        Assert.assertEquals(
                ExperimentalRow.from(hireCount).as(DailyHireCount.class),
                hireCount
        );
    }

    @Test
    public void underlyingMapMatchesInputDataAsStrings() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        Row row = ExperimentalRow.from(new DailyHireCount(100, date));

        Assert.assertEquals(row.valueMap().get("timeStamp").toString(), date.toString());
    }

    @Test(description = "The true test of whether rows preserve type")
    public void mappedDataTypeMatchesInputDataType() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        Row row = ExperimentalRow.from(new DailyHireCount(100, date));

        
        Assert.assertEquals(row.valueMap().get("timeStamp").getClass(), LocalDate.class);
    }

    @Test
    public void valueOfUnrecognizedColumnNameIsNull() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        Row row = ExperimentalRow.from(new DailyHireCount(100, date));

        Assert.assertNull(row.valueOf("INVALID"));
    }

}
