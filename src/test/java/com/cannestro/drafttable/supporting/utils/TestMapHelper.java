package com.cannestro.drafttable.supporting.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Test(groups = "unit")
public class TestMapHelper {

    @Test
    public void keyAndValueInputAreMappedOneToOne() {
        List<String> keys = List.of("one", "two", "three");
        List<Integer> values = List.of(1, 2, 3);

        Assert.assertEquals(
                MapHelper.zip(keys, values),
                Map.of("one", 1,
                       "two", 2,
                       "three", 3)
        );
    }

    @Test
    public void zippingEmptyInputProducesEmptyOutput() {
        Assert.assertEquals(
                MapHelper.zip(Collections.emptyList(), Collections.emptyList()),
                Collections.emptyMap()
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zipThrowsExceptionWhenValuesMoreThanKeys() {
        List<String> keys = List.of("one");
        List<Integer> values = List.of(1, 2, 3);
        MapHelper.zip(keys, values);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zipThrowsExceptionWhenValuesLessThanKeys() {
        List<String> keys = List.of("one", "two", "three");
        List<Integer> values = List.of(1);
        MapHelper.zip(keys, values);
    }

}
