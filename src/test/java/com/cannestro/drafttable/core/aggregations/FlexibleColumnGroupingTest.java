package com.cannestro.drafttable.core.aggregations;

import com.cannestro.drafttable.core.Column;
import com.cannestro.drafttable.core.DraftTable;
import com.cannestro.drafttable.core.implementations.FlexibleColumn;
import com.cannestro.drafttable.utils.helper.BareBonesPojo;
import com.cannestro.drafttable.utils.helper.Pay;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.cannestro.drafttable.core.aggregations.FlexibleColumnGrouping.*;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static org.hamcrest.Matchers.*;


@Test(groups = "component")
public class FlexibleColumnGroupingTest {

    @Test
    public void valueCountsOfDistinctValuesAre1() {
        Column column = FlexibleColumn.from("dates", dateCollectionHelper());

        column.group()
                .byValueCounts()
                .select(COUNT)
                .getValues()
                .forEach(count -> Assert.assertEquals(count, 1L));
    }

    @Test(description = "Value count of mutable Data class counts by value as desired instead of by object reference")
    public void mutableValueCountTest() {
        Column column = FlexibleColumn.from("pay", List.of(
                new Pay("Hourly", "20.11", "Bi-Weekly", "80"),
                new Pay(null, null, null, null),
                new Pay(null, null, null, null),
                new Pay(null, null, null, null)
        )).append((Pay) null);

        Assert.assertEquals(
                column.group().byValueCounts().where(VALUE, is(new Pay("Hourly", "20.11", "Bi-Weekly", "80")))
                        .select(COUNT)
                        .firstValue()
                        .get(),
                1L
        );
        Assert.assertEquals(
                column.group().byValueCounts().where(VALUE, nullValue())
                        .select(COUNT)
                        .firstValue()
                        .get(),
                1L
        );
        Assert.assertEquals(
                column.group().byValueCounts().where(VALUE, is(new Pay(null, null, null, null)))
                        .select(COUNT)
                        .firstValue()
                        .get(),
                3L
        );
    }

    @Test(description = "Value count of mutable object counts by object reference instead of value")
    public void valueCountsOfMutableObjectWithoutEqualsOrHashCode() {
        Column column = FlexibleColumn.from("pay", List.of(
                        new BareBonesPojo("Rex", List.of(1, 2, 3)),
                        new BareBonesPojo("Rex", List.of(1, 2, 3)),
                        new BareBonesPojo("Chachamaru", List.of(1, 2, 3)),
                        new BareBonesPojo(null, null),
                        new BareBonesPojo(null, null)))
                .append((BareBonesPojo) null)
                .append((BareBonesPojo) null);

        Assert.assertEquals(
                column.group().byValueCounts()
                        .where(VALUE, is(nullValue()))
                        .select(COUNT)
                        .firstValue()
                        .get(), // i.e., Getting the COUNT
                2L // i.e., Works as expected
        );
        Assert.assertEquals(
                column.group().byValueCounts()
                        .where(VALUE, notNullValue())
                        .where(VALUE, (BareBonesPojo pojo) -> "Rex".equals(pojo.getName()) && List.of(1, 2, 3).equals(pojo.getNumberOfBones()), is(true))
                        .select(VALUE)
                        .getValues()
                        .size(), // i.e., Getting the size of matching VALUES
                2 // i.e., Produces duplicates!
        );
        Assert.assertEquals(
                column.group().byValueCounts()
                        .where(VALUE, notNullValue())
                        .where(VALUE, (BareBonesPojo pojo) -> isNull(pojo.getName()) && isNull(pojo.getNumberOfBones()), is(true))
                        .select(VALUE)
                        .getValues()
                        .size(),
                2 // i.e., Produces duplicates!
        );
    }

    @Test
    public void aggregationWithNullAggregatesToNull() {
        List<String> values = new ArrayList<>(List.of("Hourly", "20.11", "Weekly", "40"));
        values.add(null);

        DraftTable grouping = FlexibleColumn.from("pay", values)
                .group()
                .byValuesUsing(Collectors.summingInt(value -> value.toString().length()));

        Assert.assertNull(
                grouping.where(VALUE, is(nullValue()))
                        .select(VALUE_AGGREGATION)
                        .firstValue()
                        .get()
        );
    }

    @Test
    public void aggregationOfEmptyColumnHasNoEntries() {
        DraftTable grouping = FlexibleColumn.from("pay", Collections.emptyList())
                .group()
                .byValuesUsing(Collectors.summingInt(value -> value.toString().length()));

        Assert.assertEquals(grouping.rowCount(), 0);
    }

    @Test
    public void aggregationToListGroupsLikeValuesIntoSameListPerValue() {
        List<Pay> values = new ArrayList<>(List.of(
                new Pay("Hourly", "20.11", "Bi-Weekly", "80"),
                new Pay(null, null, null, null),
                new Pay(null, null, null, null),
                new Pay(null, null, null, null)
        ));
        values.add(null);
        Column column = FlexibleColumn.from("pay", values);

        DraftTable grouping = column.group().byValuesUsing(Collectors.toList());

        Assert.assertEquals(
                grouping.where(VALUE, is(new Pay(null, null, null, null)))
                        .select(VALUE_AGGREGATION)
                        .firstValue()
                        .get(),
                Collections.nCopies(3, new Pay(null, null, null, null))
        );
        Assert.assertEquals(
                grouping.where(VALUE, is(new Pay("Hourly", "20.11", "Bi-Weekly", "80")))
                        .select(VALUE_AGGREGATION)
                        .firstValue()
                        .get(),
                List.of(new Pay("Hourly", "20.11", "Bi-Weekly", "80"))
        );
    }

    @Test
    public void aggregationToSummingIntFunction() {
        DraftTable grouping = FlexibleColumn.from("pay", List.of("Hourly", "20.11", "Weekly", "40"))
                .group()
                .byValuesUsing(Collectors.summingInt(value -> value.toString().length()));

        Assert.assertEquals(grouping.where(VALUE, is("Hourly")).select(VALUE_AGGREGATION).firstValue().get(), 6);
        Assert.assertEquals(grouping.where(VALUE, is("Weekly")).select(VALUE_AGGREGATION).firstValue().get(), 6);
        Assert.assertEquals(grouping.where(VALUE, is("20.11")).select(VALUE_AGGREGATION).firstValue().get(), 5);
        Assert.assertEquals(grouping.where(VALUE, is("40")).select(VALUE_AGGREGATION).firstValue().get(), 2);
    }

    @Test
    public void byCountsOfUserDefinedFunctionReturnsExpectedFieldCounts() {
        Column column = FlexibleColumn.from("pay", List.of(
                new Pay("Hourly", "20.11", "Bi-Weekly", "80"),
                new Pay("Hourly", null, null, null),
                new Pay("Hourly", null, null, null),
                new Pay(null, null, null, null),
                new Pay("Salary", null, null, null)
        )).append((Pay) null);

        DraftTable grouping = column.group().byCountsOf(Pay::getType);

        Assert.assertEquals(grouping.where(VALUE, is("Hourly")).select(COUNT).firstValue().get(), 3L);
        Assert.assertEquals(grouping.where(VALUE, is("Salary")).select(COUNT).firstValue().get(), 1L);
        Assert.assertEquals(grouping.where(VALUE, nullValue()).select(COUNT).firstValue().get(), 2L);
    }

    @Test
    public void byUserDefinedFunctionAndAggregateTruncatesNulls() {
        Column column = FlexibleColumn.from("pay", List.of(
                new Pay("Hourly", "20.11", "Bi-Weekly", "80"),
                new Pay("Hourly", null, null, null),
                new Pay("Hourly", null, null, null),
                new Pay(null, null, null, null),
                new Pay("Salary", null, null, null)
        )).append((Pay) null);

        DraftTable grouping = column.group().by(Pay::getType, Collectors.toList());

        Assert.assertEqualsNoOrder(grouping.select(VALUE).getValues(), List.of("Hourly", "Salary"));
        Assert.assertEquals(
                grouping.where(VALUE, is("Salary"))
                        .select(VALUE_AGGREGATION)
                        .firstValue()
                        .get(),
                List.of(new Pay("Salary", null, null, null))
        );
        Assert.assertEquals(
                grouping.where(VALUE, is("Hourly"))
                        .select(VALUE_AGGREGATION)
                        .firstValue()
                        .get(),
                List.of(new Pay("Hourly", "20.11", "Bi-Weekly", "80"),
                        new Pay("Hourly", null, null, null),
                        new Pay("Hourly", null, null, null)
                )
        );
    }


    /* ----------------------------------------------------------------------------- */
    /* --------------------------Test Data and DataProviders------------------------ */
    /* ----------------------------------------------------------------------------- */

    List<LocalDate> dateCollectionHelper() {
        return asList(
                LocalDate.of(2016, 12, 14),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2016, 10, 14),
                LocalDate.of(2016, 4, 14)
        );
    }

    List<String> regionCollectionHelper() {
        return asList(
                "South Atlantic",
                "Mountain",
                "Mountain",
                "Mountain"
        );
    }

}
