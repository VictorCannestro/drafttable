package com.cannestro.drafttable.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Test(groups = "unit")
public class TestListUtils {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void transposeThrowsExceptionWhen2DCollectionIsJagged_FirstRowShorter() {
        List<List<Integer>> table = Arrays.asList(
                Arrays.asList(0, 1, 2),
                Arrays.asList(4, 5, 6, 7),
                Arrays.asList(8, 9, 10, 11)
        );
        ListUtils.transpose(table);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void transposeThrowsExceptionWhen2DCollectionIsJagged_FirstRowLonger() {
        List<List<Integer>> table = Arrays.asList(
                Arrays.asList(0, 1, 2, 4, 4),
                Arrays.asList(4, 5, 6, 7),
                Arrays.asList(8, 9, 10, 11)
        );
        ListUtils.transpose(table);
    }

    @Test
    public void canTransposeRowsAndColumns() {
        List<List<Integer>> table = Arrays.asList(
                Arrays.asList(0, 1, 2, 3),
                Arrays.asList(4, 5, 6, 7),
                Arrays.asList(8, 9, 10, 11)
        );
        Assert.assertEquals(
                ListUtils.transpose(table),
                Arrays.asList(
                        Arrays.asList(0, 4, 8),
                        Arrays.asList(1, 5, 9),
                        Arrays.asList(2, 6, 10),
                        Arrays.asList(3, 7, 11)
                )
        );
    }

    @Test
    public void doubleTransposeReturnsOriginal() {
        List<List<Integer>> testCollection = Arrays.asList(
                Arrays.asList(0, 1, 2),
                Arrays.asList(3, 4, 5),
                Arrays.asList(6, 7, 8)
        );
        Assert.assertEquals(
                testCollection,
                ListUtils.transpose(ListUtils.transpose(testCollection))
        );
    }

    @Test
    public void transposeOfEmptyListIsSameList() {
        Assert.assertEquals(
                ListUtils.transpose(new ArrayList<>()),
                new ArrayList<>()
        );
    }

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
