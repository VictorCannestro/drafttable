package com.cannestro.drafttable.core.implementations;

import com.cannestro.drafttable.core.Row;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.Collections;


@Test(groups = {"component"})
public class HashMapRowTest {

    record DailyHireCount(int n, LocalDate timeStamp){}


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotMakeRowFromNull() {
        HashMapRow.from(null);
    }

    @Test
    public void isEmptyTrueWhenEmpty() {
        Assert.assertTrue(
                HashMapRow.from(Collections.emptyMap()).isEmpty()
        );
    }

    @Test
    public void canCreateRowFromRecordContainingJavaTimeLocalDates() {
        Row row = HashMapRow.from(
                new DailyHireCount(100, LocalDate.of(2023, 1, 1))
        );

        Assert.assertTrue(row.hasKey("timeStamp"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenCreatingRowFromIncompatibleType() {
        HashMapRow.from(LocalDate.of(2023, 1, 1));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotMapBackIntoIncompatibleType() {
        HashMapRow.from(new DailyHireCount(100, LocalDate.of(2023, 1, 1)))
                  .as(LocalDate.class);
    }

    @Test
    public void canMapBackIntoOriginatingType() {
        DailyHireCount hireCount = new DailyHireCount(100, LocalDate.of(2023, 1, 1));

        Assert.assertEquals(
                HashMapRow.from(hireCount).as(DailyHireCount.class),
                hireCount
        );
    }

    @Test
    public void underlyingMapMatchesInputData() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        Row row = HashMapRow.from(new DailyHireCount(100, date));

        Assert.assertEquals(row.valueMap().get("timeStamp"), date.toString());
    }

    @Test
    public void valueOfUnrecognizedColumnNameIsNull() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        Row row = HashMapRow.from(new DailyHireCount(100, date));

        Assert.assertNull(row.valueOf("INVALID"));
    }

}
