package com.cannestro.drafttable.supporting.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Test(groups = "unit")
public class TestListUtils {

    @Test
    public void fillingEmptyCollectionToLengthNProducesListWithNFillValues() {
        Assert.assertEquals(
            ListUtils.fillToTargetLength(new ArrayList<>(), 10, "value"),
                Collections.nCopies(10, "value")
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotFillToLengthWhenTargetLengthIsLessThanInputLength() {
        ListUtils.fillToTargetLength(List.of(1,2,3,4), 1, 100);
    }

    @Test
    public void fillingCollectionAtTargetLengthToLengthProducesSameList() {
        Assert.assertEquals(
                ListUtils.fillToTargetLength(List.of(1,2,3,4), 4, 100),
                List.of(1,2,3,4)
        );
    }

    @Test
    public void fillingCollectionBelowLengthToLengthProducesFilledList() {
        Assert.assertEquals(
                ListUtils.fillToTargetLength(List.of(1,2,3,4), 6, 100),
                List.of(1, 2, 3, 4, 100, 100)
        );
    }

    @Test
    public void firstElementOfPicksFirstElementOnly() {
        Assert.assertEquals(
                ListUtils.firstElementOf(List.of(1,2,3)),
                1
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void firstElementOfThrowsErrorOnEmptyList() {
        ListUtils.firstElementOf(List.of());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void firstElementOfThrowsErrorOnEmptyArray() {
        ListUtils.firstElementOf(new Object[] {});
    }

    @Test
    public void firstElementOfArrayPicksFirstElementOnly() {
        Assert.assertEquals(
                ListUtils.firstElementOf(new Integer[] {1, 2, 3}),
                1
        );
    }

    @Test
    public void lastElementOfPicksFirstElementOnly() {
        Assert.assertEquals(
                ListUtils.lastElementOf(List.of(1,2,3)),
                3
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void lastElementOfThrowsErrorOnEmptyList() {
        ListUtils.lastElementOf(List.of());
    }

    @Test
    public void lasstElementOfArrayPicksFirstElementOnly() {
        Assert.assertEquals(
                ListUtils.lastElementOf(new Integer[] {1, 2, 3}),
                3
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void lastElementOfThrowsErrorOnEmptyArray() {
        ListUtils.lastElementOf(new Object[] {});
    }

}
