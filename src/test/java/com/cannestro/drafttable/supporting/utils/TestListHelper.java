package com.cannestro.drafttable.supporting.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Test(groups = "unit")
public class TestListHelper {

    @Test
    public void fillingEmptyCollectionToLengthNProducesListWithNFillValues() {
        Assert.assertEquals(
            ListHelper.fillToTargetLength(new ArrayList<>(), 10, "value"),
                Collections.nCopies(10, "value")
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotFillToLengthWhenTargetLengthIsLessThanInputLength() {
        ListHelper.fillToTargetLength(List.of(1,2,3,4), 1, 100);
    }

    @Test
    public void fillingCollectionAtTargetLengthToLengthProducesSameList() {
        Assert.assertEquals(
                ListHelper.fillToTargetLength(List.of(1,2,3,4), 4, 100),
                List.of(1,2,3,4)
        );
    }

    @Test
    public void fillingCollectionBelowLengthToLengthProducesFilledList() {
        Assert.assertEquals(
                ListHelper.fillToTargetLength(List.of(1,2,3,4), 6, 100),
                List.of(1, 2, 3, 4, 100, 100)
        );
    }

    @Test
    public void firstElementOfPicksFirstElementOnly() {
        Assert.assertEquals(
                ListHelper.firstElementOf(List.of(1,2,3)),
                1
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void firstElementOfThrowsErrorOnEmptyList() {
        ListHelper.firstElementOf(List.of());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void firstElementOfThrowsErrorOnEmptyArray() {
        ListHelper.firstElementOf(new Object[] {});
    }

    @Test
    public void firstElementOfArrayPicksFirstElementOnly() {
        Assert.assertEquals(
                ListHelper.firstElementOf(new Integer[] {1, 2, 3}),
                1
        );
    }

    @Test
    public void lastElementOfPicksFirstElementOnly() {
        Assert.assertEquals(
                ListHelper.lastElementOf(List.of(1,2,3)),
                3
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void lastElementOfThrowsErrorOnEmptyList() {
        ListHelper.lastElementOf(List.of());
    }

    @Test
    public void lasstElementOfArrayPicksFirstElementOnly() {
        Assert.assertEquals(
                ListHelper.lastElementOf(new Integer[] {1, 2, 3}),
                3
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void lastElementOfThrowsErrorOnEmptyArray() {
        ListHelper.lastElementOf(new Object[] {});
    }

}
