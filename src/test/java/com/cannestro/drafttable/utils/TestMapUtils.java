package com.cannestro.drafttable.utils;

import com.cannestro.drafttable.supporting.utils.MapUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Test(groups = "unit")
public class TestMapUtils {

    @Test
    public void keyAndValueInputAreMappedOneToOne() {
        List<String> keys = List.of("one", "two", "three");
        List<Integer> values = List.of(1, 2, 3);

        Assert.assertEquals(
                MapUtils.zip(keys, values),
                Map.of("one", 1,
                       "two", 2,
                       "three", 3)
        );
    }

    @Test
    public void zippingEmptyInputProducesEmptyOutput() {
        Assert.assertEquals(
                MapUtils.zip(Collections.emptyList(), Collections.emptyList()),
                Collections.emptyMap()
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zipThrowsExceptionWhenValuesMoreThanKeys() {
        List<String> keys = List.of("one");
        List<Integer> values = List.of(1, 2, 3);
        MapUtils.zip(keys, values);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zipThrowsExceptionWhenValuesLessThanKeys() {
        List<String> keys = List.of("one", "two", "three");
        List<Integer> values = List.of(1);
        MapUtils.zip(keys, values);
    }

}
