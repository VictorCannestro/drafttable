package com.cannestro.drafttable.supporting.utils;

import com.cannestro.drafttable.supporting.utils.JsonUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;


@Test(groups = "unit")
public class TestJsonUtils {

    record HireInfo(String employeeId, LocalDate hireDate) {}

    record Pay(String type, String rate, String period, String workHours) {
        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
        }
    }


    @Test(dataProvider = "nonJsonCompatibles")
    public void nonJsonCompatiblesReturnFalse(List<?> input) {
        Assert.assertFalse(
                JsonUtils.isJSONCompatible(input)
        );
    }

    @Test
    public void recordWithoutJsonStringIsNotJsonCompatible() {
        Assert.assertFalse(
                JsonUtils.isJSONCompatible(List.of(new HireInfo("11234", LocalDate.now())))
        );
    }

    @Test
    public void recordWithJsonStringIsJsonCompatible() {
        Assert.assertTrue(
                JsonUtils.isJSONCompatible(List.of(new Pay("Hourly", "23.55", "Bi-Weekly", "80")))
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotMakeJsonStringListWhenNotJsonCompatible() {
        JsonUtils.jsonStringListFrom(List.of(new HireInfo("11234", LocalDate.now())));
    }


    @DataProvider(name = "nonJsonCompatibles")
    Object[][] nonJsonCompatibles() {
        return new Object[][]{
                {List.of()},
                {List.of(LocalDate.now(), LocalDate.of(2022, 1, 1))},
                {List.of(1, 2)},
                {List.of("one", "two")},
                {List.of(true, false)},
                {List.of(10.3, -3.3)}
        };
    }

}
