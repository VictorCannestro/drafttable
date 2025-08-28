package com.cannestro.drafttable.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


@Test(groups = "unit")
public class TestNullDetector {

    @Test
    public void hasNullsIsTrueWhenHasNulls() {
        List<String> list = new ArrayList<>();
        list.add(null);
        list.add("element");
        Assert.assertTrue(NullDetector.hasNullIn(list));
    }

    @Test
    public void hasNullsFalseWhenHasNoNulls() {
        List<String> list = new ArrayList<>();
        list.add("element");
        Assert.assertFalse(NullDetector.hasNullIn(list));
    }

    @Test
    public void hasNullsFalseWhenEmpty() {
        Assert.assertFalse(NullDetector.hasNullIn(List.of()));
    }

    @Test
    public void noNullsFalseWhenHasNulls() {
        List<String> list = new ArrayList<>();
        list.add(null);
        list.add("element");
        Assert.assertFalse(NullDetector.noNullsIn(list));
    }

    @Test
    public void noNullsTrueWhenHasNoNulls() {
        List<String> list = new ArrayList<>();
        list.add("element");
        Assert.assertTrue(NullDetector.noNullsIn(list));
    }

    @Test
    public void noNullsTrueWhenEmpty() {
        Assert.assertFalse(NullDetector.hasNullIn(List.of()));
    }

}
