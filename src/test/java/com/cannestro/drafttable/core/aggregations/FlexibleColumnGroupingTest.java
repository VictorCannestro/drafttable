package com.cannestro.drafttable.core.aggregations;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.columns.FlexibleColumn;
import com.cannestro.drafttable.supporting.utils.helper.BareBonesPojo;
import com.cannestro.drafttable.supporting.utils.helper.PayDetails;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
                .select(ColumnGrouping.COUNT)
                .values()
                .forEach(count -> Assert.assertEquals(count, 1L));
    }

    @Test(description = "Value count of mutable Data class counts by value as desired instead of by object reference")
    public void mutableValueCountTest() {
        Column column = FlexibleColumn.from("pay", List.of(
                new PayDetails("Hourly", "20.11", "Bi-Weekly", "80"),
                new PayDetails(null, null, null, null),
                new PayDetails(null, null, null, null),
                new PayDetails(null, null, null, null)
        )).append((PayDetails) null);

        Assert.assertEquals(
                column.group().byValueCounts().where(ColumnGrouping.VALUE, is(new PayDetails("Hourly", "20.11", "Bi-Weekly", "80")))
                        .select(ColumnGrouping.COUNT)
                        .firstValue()
                        .get(),
                1L
        );
        Assert.assertEquals(
                column.group().byValueCounts().where(ColumnGrouping.VALUE, nullValue())
                        .select(ColumnGrouping.COUNT)
                        .firstValue()
                        .get(),
                1L
        );
        Assert.assertEquals(
                column.group().byValueCounts().where(ColumnGrouping.VALUE, is(new PayDetails(null, null, null, null)))
                        .select(ColumnGrouping.COUNT)
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
                        .where(ColumnGrouping.VALUE, is(nullValue()))
                        .select(ColumnGrouping.COUNT)
                        .firstValue()
                        .get(), // i.e., Getting the COUNT
                2L // i.e., Works as expected
        );
        Assert.assertEquals(
                column.group().byValueCounts()
                        .where(ColumnGrouping.VALUE, notNullValue())
                        .where(ColumnGrouping.VALUE, (BareBonesPojo pojo) -> "Rex".equals(pojo.getName()) && List.of(1, 2, 3).equals(pojo.getNumberOfBones()), is(true))
                        .select(ColumnGrouping.VALUE)
                        .values()
                        .size(), // i.e., Getting the size of matching VALUES
                2 // i.e., Produces duplicates!
        );
        Assert.assertEquals(
                column.group().byValueCounts()
                        .where(ColumnGrouping.VALUE, notNullValue())
                        .where(ColumnGrouping.VALUE, (BareBonesPojo pojo) -> isNull(pojo.getName()) && isNull(pojo.getNumberOfBones()), is(true))
                        .select(ColumnGrouping.VALUE)
                        .values()
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
                grouping.where(ColumnGrouping.VALUE, is(nullValue()))
                        .select(ColumnGrouping.VALUE_AGGREGATION)
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
        List<PayDetails> values = new ArrayList<>(List.of(
                new PayDetails("Hourly", "20.11", "Bi-Weekly", "80"),
                new PayDetails(null, null, null, null),
                new PayDetails(null, null, null, null),
                new PayDetails(null, null, null, null)
        ));
        values.add(null);
        Column column = FlexibleColumn.from("pay", values);

        DraftTable grouping = column.group().byValuesUsing(Collectors.toList());

        Assert.assertEquals(
                grouping.where(ColumnGrouping.VALUE, is(new PayDetails(null, null, null, null)))
                        .select(ColumnGrouping.VALUE_AGGREGATION)
                        .firstValue()
                        .get(),
                Collections.nCopies(3, new PayDetails(null, null, null, null))
        );
        Assert.assertEquals(
                grouping.where(ColumnGrouping.VALUE, is(new PayDetails("Hourly", "20.11", "Bi-Weekly", "80")))
                        .select(ColumnGrouping.VALUE_AGGREGATION)
                        .firstValue()
                        .get(),
                List.of(new PayDetails("Hourly", "20.11", "Bi-Weekly", "80"))
        );
    }

    @Test
    public void aggregationToSummingIntFunction() {
        DraftTable grouping = FlexibleColumn.from("pay", List.of("Hourly", "20.11", "Weekly", "40"))
                .group()
                .byValuesUsing(Collectors.summingInt(value -> value.toString().length()));

        Assert.assertEquals(grouping.where(ColumnGrouping.VALUE, is("Hourly")).select(ColumnGrouping.VALUE_AGGREGATION).firstValue().get(), 6);
        Assert.assertEquals(grouping.where(ColumnGrouping.VALUE, is("Weekly")).select(ColumnGrouping.VALUE_AGGREGATION).firstValue().get(), 6);
        Assert.assertEquals(grouping.where(ColumnGrouping.VALUE, is("20.11")).select(ColumnGrouping.VALUE_AGGREGATION).firstValue().get(), 5);
        Assert.assertEquals(grouping.where(ColumnGrouping.VALUE, is("40")).select(ColumnGrouping.VALUE_AGGREGATION).firstValue().get(), 2);
    }

    @Test
    public void byCountsOfUserDefinedFunctionReturnsExpectedFieldCounts() {
        Column column = FlexibleColumn.from("pay", List.of(
                new PayDetails("Hourly", "20.11", "Bi-Weekly", "80"),
                new PayDetails("Hourly", null, null, null),
                new PayDetails("Hourly", null, null, null),
                new PayDetails(null, null, null, null),
                new PayDetails("Salary", null, null, null)
        )).append((PayDetails) null);

        DraftTable grouping = column.group().byCountsOf(PayDetails::getType);

        Assert.assertEquals(grouping.where(ColumnGrouping.VALUE, is("Hourly")).select(ColumnGrouping.COUNT).firstValue().get(), 3L);
        Assert.assertEquals(grouping.where(ColumnGrouping.VALUE, is("Salary")).select(ColumnGrouping.COUNT).firstValue().get(), 1L);
        Assert.assertEquals(grouping.where(ColumnGrouping.VALUE, nullValue()).select(ColumnGrouping.COUNT).firstValue().get(), 2L);
    }

    @Test
    public void byUserDefinedFunctionAndAggregateTruncatesNulls() {
        Column column = FlexibleColumn.from("pay", List.of(
                new PayDetails("Hourly", "20.11", "Bi-Weekly", "80"),
                new PayDetails("Hourly", null, null, null),
                new PayDetails("Hourly", null, null, null),
                new PayDetails(null, null, null, null),
                new PayDetails("Salary", null, null, null)
        )).append((PayDetails) null);

        DraftTable grouping = column.group().by(PayDetails::getType, Collectors.toList());

        Assert.assertEqualsNoOrder(grouping.select(ColumnGrouping.VALUE).values(), List.of("Hourly", "Salary"));
        Assert.assertEquals(
                grouping.where(ColumnGrouping.VALUE, is("Salary"))
                        .select(ColumnGrouping.VALUE_AGGREGATION)
                        .firstValue()
                        .get(),
                List.of(new PayDetails("Salary", null, null, null))
        );
        Assert.assertEquals(
                grouping.where(ColumnGrouping.VALUE, is("Hourly"))
                        .select(ColumnGrouping.VALUE_AGGREGATION)
                        .firstValue()
                        .get(),
                List.of(new PayDetails("Hourly", "20.11", "Bi-Weekly", "80"),
                        new PayDetails("Hourly", null, null, null),
                        new PayDetails("Hourly", null, null, null)
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
