package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.options.StatisticName;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.options.SortingOrderType;
import com.cannestro.drafttable.helper.Library;
import org.hamcrest.MatcherAssert;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;


@Test(groups = {"component"})
public class FlexibleColumnTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void mixedInputTypesShouldThrowError() {
        new FlexibleColumn("mixed_data", asList(1, "one", 3.3, true));
    }

    @Test
    public void shouldStoreUnderlyingValuesInList() {
        List<Integer> list = asList(1, 2, 3, 4, 5);
        Column c = new FlexibleColumn("data", list);
        assertEquals(c.values(), list);
    }

    @Test(dataProvider = "parameterizedListsWithTypes")
    public void dataTypeOfColumnMatchesDataTypeOfInputDataForNonNestedCollections(List<?> list, Type expectedType) {
        Column c = new FlexibleColumn("arbitrary_label", list);
        assertEquals(c.dataType(), expectedType);
    }

    @Test
    public void dataTypeWhenIEmptyIsObject() {
        Column c = new FlexibleColumn("data", emptyList());
        assertEquals(c.dataType(), Object.class);
    }

    @Test
    public void isEmptyIsTrueWhenEmpty() {
        Column c = new FlexibleColumn("data", emptyList());
        assertTrue(c.isEmpty());
    }

    @Test
    public void isEmptyIsFalseWhenNotEmpty() {
        Column c = new FlexibleColumn("data", asList(1,2,3,4,5));
        assertFalse(c.isEmpty());
    }

    @Test
    public void sizeMatchesInputData() {
        List<Integer> list = asList(1,2,3,4,5);
        Column c = new FlexibleColumn("data", list);
        assertEquals(c.size(), list.size());
    }

    @Test
    public void sizeMatchesInputDataWhenEmpty() {
        Column c = new FlexibleColumn("data", emptyList());
        assertEquals(c.size(), 0);
        assertFalse(c.hasNulls());

    }

    @Test(dataProvider = "containsData")
    public <T> void containsReturnsTrueWhenColumnContainsTheInputValue(List<T> data, T expectedValue) {
        Column c = new FlexibleColumn("data", data);
        assertTrue(c.has(expectedValue));
    }

    @Test
    public void containsNullReturnsTrueWhenContainsNull() {
        List<Integer> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        Column c = new FlexibleColumn("data", list);
        assertTrue(c.hasNulls());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void hasElementThrowsExceptionWhenCheckingNull() {
        List<Integer> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        Column c = new FlexibleColumn("data", list);
        assertTrue(c.has(null));
    }

    @Test
    public void introspectionAllowsForSelfReference() {
        List<Integer> list = asList(1,2,3,4,5);
        Column column = new FlexibleColumn("data", list);

        Assert.assertEquals(
                column.introspect(c -> c.where(
                        greaterThan(c.aggregate(0, Integer::sum) / c.size()) // i.e. the average
                )),
                FlexibleColumn.from("data", asList(4,5))
        );
    }

    @Test
    public void conditionalActionIsTakenWhenConditionIsTrue() {
        List<Integer> list = asList(1,2,3,4,5);
        Column column = new FlexibleColumn("data", list)
                .conditionalAction(Column::hasNulls,
                                   Column::dropNulls,
                                   c -> c.transform("Square", (Integer x) -> x*x));

        assertThat(
                column.values(),
                contains(1,4,9,16,25)
        );
    }

    @Test
    public void conditionalActionIsTakenWhenConditionIsFalse() {
        List<Integer> list = new ArrayList<>(asList(1,2,3,4,5));
        list.add(null);
        Column column = new FlexibleColumn("data", list)
                .conditionalAction(Column::hasNulls,
                                   Column::dropNulls,
                                   c -> c.transform("Square", (Integer x) -> x*x));

        assertEquals(column.size(), asList(1,2,3,4,5).size());
        assertThat(
                column.values(),
                contains(1,2,3,4,5)
        );
    }

    @Test
    public void afterFillNullsWithFillValueColumnWithNullsContainsFillValue() {
        List<String> list = new ArrayList<>(List.of("rules", "laws"));
        list.add(null);
        list.add(null);
        Column c = new FlexibleColumn("policies", list);

        assertThat(
                c.fillNullsWith("INVALID").values(),
                contains("rules", "laws", "INVALID", "INVALID")
        );
    }

    @Test
    public void afterFillNullsWithFillValueColumnWithoutNullsIsTheSame() {
        List<String> list = Collections.nCopies(5, "n");
        Column c = new FlexibleColumn("letters", list);

        assertThat(
                c.fillNullsWith("INVALID").values(),
                not(hasItem("INVALID"))
        );
        Assert.assertEquals(c.fillNullsWith("INVALID"), c);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotFillNullsWithValueOfMismatchedType() {
        List<Integer> list = new ArrayList<>(List.of(1,2,3));
        list.add(null);
        new FlexibleColumn("num_list", list).fillNullsWith("zero");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void cannotFillNullsWithNull() {
        List<Integer> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        new FlexibleColumn("data", list).fillNullsWith(null);
    }

    @Test
    public void afterCallingDropNullsColumnDoesNotContainNullValues() {
        List<Integer> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        Column c = new FlexibleColumn("data", list);

        assertEquals(c.dropNulls().size(), 0);
        assertEquals(c.dropNulls().values(), emptyList());
    }

    @Test
    public void afterCallingDropNullsOnEmptyColumnThenAnEmptyColumnIsReturned() {
        Column c = new FlexibleColumn("data", emptyList());

        assertEquals(c.isEmpty(), c.dropNulls().isEmpty());
    }

    @Test
    public void canAppendNonNullValueToColumnWithOnlyNulls() {
        List<Integer> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        Column c = new FlexibleColumn("data", list).append(true);

        assertTrue(c.hasNulls());
        assertEquals(c.size(), 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotAppendMismatchingType() {
        Column c = new FlexibleColumn("data", emptyList())
                .append(1)
                .append("INVALID");
    }

    @Test
    public void overloadedAppendsProducesNewColumnWithAllProvidedElements() {
        Column c = new FlexibleColumn("data", emptyList())
                .append(1)
                .append(asList(0, 0, 0))
                .append(new FlexibleColumn("other data", List.of(1000)));

        assertEquals(c.size(), 5);
        assertThat(c.values(), contains(1, 0, 0, 0, 1000));
    }

    @Test
    public void appendingAListProducesNewColumnWithAllProvidedElements() {
        Column c = new FlexibleColumn("data", emptyList())
                .append(asList(
                        LocalDate.now(),
                        LocalDate.of(2023, 1, 1)
                ));
        assertEquals(c.size(), 2);
        assertThat(
                c.values(),
                contains(
                    LocalDate.now(),
                    LocalDate.of(2023, 1, 1)
                )
        );
    }

    @Test
    public void appendingAColumnProducesNewColumnWithAllProvidedElements() {
        Column c = new FlexibleColumn("data", List.of("barista"))
                .append(new FlexibleColumn("other data", emptyList()));
        assertEquals(c.size(), 1);
        assertThat(c.values(), contains("barista"));
    }

    @Test
    public void transformingLocalDateColumnIntoIntegerColumn() {
        Column c = new FlexibleColumn(
                "dates",
                List.of(LocalDate.of(2023, 1, 1))
        );
        assertEquals(c.dataType(), LocalDate.class);

        c = c.transform("transformedColumn", LocalDate::getYear);
        assertEquals(c.dataType(), Integer.class);
        assertThat(c.values(), contains(2023));
    }

    @Test
    public void transformingStringColumnIntoIntegerColumn() {
        Column c = new FlexibleColumn(
                "job names",
                asList("cafe attendant", "barista", "shift supervisor")
        );
        assertEquals(c.dataType(), String.class);

        c = c.transform("nameLength", String::length);
        assertEquals(c.dataType(), Integer.class);
        assertThat(c.values(), contains(14, 7, 16));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenTransformPassedMismatchedFunctionType() {
        new FlexibleColumn(
                "job names",
                asList("cafe attendant", "barista", "shift supervisor")
        ).transform((Integer i) -> "Number: " + i);
    }

    @Test
    public void applyingConsumerToNonPrimitiveColumnCanMutateItsUnderlyingState() {
        Column c = new FlexibleColumn(
                "job names",
                asList(new ArrayList<>(asList("BAR", "SSV")), new ArrayList<>(asList("ASM", "SM")))
        );
        c.apply((List<String> list) -> list.removeIf(name -> name.contains("A")));

        assertThat(
                c.values(),
                contains(List.of("SSV"), List.of("SM"))
        );
    }

    @Test
    public void usingApplyMayMutateTheStateOfAUserDefinedObject() {
        Column c = FlexibleColumn.from("New Libraries", List.of(new Library(), new Library()));
        assertEquals(c.transform(Library::numberOfBooks).values(), List.of(0, 0));

        c.apply(Library::addSomething);
        assertEquals(c.transform(Library::numberOfBooks).values(), List.of(1, 1));
    }

    @Test
    public void applyingFunctionToPrimitiveColumnDoesNotMutateState() {
        Column c = new FlexibleColumn("job names", asList("BAR", "SSV"));
        c.apply((String name) -> name.toLowerCase());

        assertThat(c.values(), contains("BAR", "SSV"));
    }

    @Test
    public void applyFunctionToEmptyColumnHasNoMaterialEffect() {
        Column c = FlexibleColumn.from("data", List.of());

        c.apply((String name) -> name.toLowerCase());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenApplyPassedMismatchedConsumerType() {
        new FlexibleColumn("job names", asList("BAR", "SSV")).apply((Integer mismatch) -> mismatch = 10);
    }

    @Test
    public void top2SelectsUpTo2ValuesFromTop() {
        Column c = new FlexibleColumn(
                "job names",
                asList("cafe attendant", "barista", "shift supervisor")
        ).top(2);
        assertEquals(c.size(), 2);
        assertThat(c.values(), contains("cafe attendant", "barista"));
    }

    @Test
    public void top10Selects3ValuesFromTopWhenColumnSizeIs3() {
        Column c = new FlexibleColumn(
                "job names",
                asList("cafe attendant", "barista", "shift supervisor")
        ).top(10);
        assertEquals(c.size(), 3);
        assertThat(c.values(), contains("cafe attendant", "barista", "shift supervisor"));
    }

    @Test
    public void bottom2SelectsUpTo2ValuesFromBottom() {
        Column c = new FlexibleColumn(
                "job names",
                asList("cafe attendant", "barista", "shift supervisor")
        ).bottom(2);
        assertEquals(c.size(), 2);
        assertThat(c.values(), contains("barista", "shift supervisor"));
    }

    @Test
    public void bottom10Selects3ValuesFromBottomWhenColumnSizeIs3() {
        Column c = new FlexibleColumn(
                "job names",
                asList("cafe attendant", "barista", "shift supervisor")
        ).bottom(10);
        assertEquals(c.size(), 3);
        assertThat(c.values(), contains("cafe attendant", "barista", "shift supervisor"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSelectingTopWithNegativeNumber() {
        new FlexibleColumn(
                "job names",
                asList("cafe attendant", "barista", "shift supervisor")
        ).top(-1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSelectingBottomWithNegativeNumber() {
        new FlexibleColumn(
                "job names",
                asList("cafe attendant", "barista", "shift supervisor")
        ).bottom(-1);
    }

    @Test
    public void randomDrawReturnsUpToNSamples() {
        Column c = FlexibleColumn.from("job names", asList("cafe attendant", "barista", "shift supervisor"))
                                 .randomDraw(2);

        Assert.assertEquals(c.size(), 2);
    }

    @Test
    public void whenFilteringLocalDatesGreaterThanAGivenValueOnlyLaterDatesRemain() {
        List<LocalDate> list = asList(
                LocalDate.of(2016, 12, 14),
                LocalDate.of(2009, 9, 14),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2012, 12, 14)
        );
        Column c = new FlexibleColumn("hireDates", list)
                .where(greaterThan(LocalDate.of(2013, 1, 1)));

        assertEquals(c.size(), 2);
        assertThat(
                c.values(),
                contains(
                        LocalDate.of(2016, 12, 14),
                        LocalDate.of(2023, 1, 1)
        ));
    }

    @Test
    public void canChainTransformsOfMultipleTypes() {
        List<LocalDate> list = asList(
                LocalDate.of(2016, 12, 14),
                LocalDate.of(2009, 9, 14),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2012, 12, 14)
        );
        Column c = new FlexibleColumn("hireDates", list);

        assertEquals(c.dataType(), LocalDate.class);

        c = c.where(greaterThan(LocalDate.of(2013, 1, 1)))
                .transform("Length of Service", (LocalDate date) -> Period.between(date, LocalDate.of(2023, 1, 1)))
                .transform("Years of Service", Period::getYears);

        assertEquals(c.size(), 2);
        assertEquals(c.dataType(), Integer.class);
    }

    @Test(dataProvider = "orderByData")
    public void canOrderByDataAscending(List<?> data, List<?> expectedOrder) {
        Column c = new FlexibleColumn("testData", data)
                .orderBy(SortingOrderType.ASCENDING);

        assertEquals(c.values(), expectedOrder);
    }

    @Test
    public void whenOrderingByDescendingNullsAreLast() {
        List<LocalDate> list = asList(
                LocalDate.of(2016, 12, 14),
                null,
                LocalDate.of(2023, 1, 1),
                null
        );
        Column c = new FlexibleColumn("testData", list)
                .orderBy(SortingOrderType.DESCENDING);

        assertThat(
                c.values(),
                contains(
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2016, 12, 14),
                        null,
                        null
                )
        );
    }

    @Test
    public void orderingByCustomComparatorReturnsExpectedOrder() {
        Column c = new FlexibleColumn("testData", dateCollectionHelper())
                .orderBy(Comparator.comparing(data -> ((LocalDate) data).getMonth()));

        assertThat(
                c.values(),
                contains(
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2016, 4, 14),
                        LocalDate.of(2016, 10, 14),
                        LocalDate.of(2016, 12, 14)
                )
        );

    }

    @Test
    public void whereObjectAspectIsFilterable() {
        Column c = new FlexibleColumn("testData", dateCollectionHelper())
                .where(
                        LocalDate::getMonth,
                        is(Month.of(12))
                );

        Assert.assertEquals(
                c.firstValue().get(),
                LocalDate.of(2016, 12, 14)
        );
    }

    @Test
    public void whereObjectAspectDoesNotMatchReturnsEmptyColumn() {
        Column c = new FlexibleColumn("testData", dateCollectionHelper())
                .where(
                        LocalDate::getMonth,
                        is(Month.of(5))
                );

        Assert.assertEquals(c.size(), 0);
    }

    @Test
    public void canAggregateToSameType(){
        List<Integer> list = asList(1,2,3,4,5);
        Integer sum = new FlexibleColumn("data", list)
                .aggregate(0, Integer::sum);

        assertThat(sum, is(15));
    }

    @Test
    public void canAggregateToIdentityWhenEmpty(){
        Integer sum = new FlexibleColumn("data", emptyList())
                .aggregate(0, Integer::sum);

        assertThat(sum, is(0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotAggregateToDifferentType(){
        new FlexibleColumn("data", asList(1,2,3,4,5)).aggregate(false, (aBoolean, aBoolean2) -> true);
    }

    @Test
    public void canAggregateImplicitTypeComparatorByCasting() {
        LocalDate date = new FlexibleColumn("testData", dateCollectionHelper())
                .aggregate(BinaryOperator.minBy((Comparator<LocalDate>) Comparator.naturalOrder()))
                .orElseThrow();

        Assert.assertEquals(date, LocalDate.of(2016, 4, 14));
    }

    @Test
    public void canAggregateByExplicitlyStatedTypeWithoutCasting() {
        LocalDate date = new FlexibleColumn("testData", dateCollectionHelper())
                .aggregate(BinaryOperator.minBy(Comparator.comparing(LocalDate::getMonth)))
                .orElseThrow();

        Assert.assertEquals(date, LocalDate.of(2023, 1, 1));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotAggregateByExplicitlyStatedMismatchingType() {
        new FlexibleColumn("testData", asList(1,5,3,4))
                .aggregate(BinaryOperator.minBy(Comparator.comparing(LocalDate::getMonth)));
    }

    @Test
    public void canAggregateWithAccumulatorAndCombiner() {
        int sum = new FlexibleColumn("data", asList("one", "two", "three", "four"))
                .aggregate(0, (Integer accumulatedInt, String str) -> accumulatedInt + str.length(), Integer::sum);

        Assert.assertEquals(
                sum,
                "one".length() + "two".length() + "three".length() + "four".length()
        );
    }

    @Test
    public void renamedLabelIsEqualToUpdatedLabelData() {
        Column c = new FlexibleColumn(
                "job names",
                asList("cafe attendant", "barista", "shift supervisor")
        ).renameAs("jobs");

        Assert.assertEquals(c.label(), "jobs");
    }

    @Test
    public void canSplitColumnIntoDraftTable() {
        Column c = new FlexibleColumn("localDates", dateCollectionHelper());
        DraftTable dates = c.split()
                .intoColumn("day", LocalDate::getDayOfMonth)
                .intoColumn("month", LocalDate::getMonth)
                .intoColumn("year", LocalDate::getYear)
                .gatherIntoNewTable();

        MatcherAssert.assertThat(dates.columnNames(), containsInAnyOrder("day", "month", "year"));
        Assert.assertEquals(dates.rowCount(), c.size());
        Assert.assertEquals(dates.select("day").dataType(), Integer.class);
        Assert.assertEquals(dates.select("month").dataType(), Month.class);
        Assert.assertEquals(dates.select("year").dataType(), Integer.class);
    }

    @Test
    public void whenSplittingColumnIntoItselfTheDraftTableContainsItself() {
        Column c = new FlexibleColumn("localDates", dateCollectionHelper());
        DraftTable dates = c.split().gatherIntoNewTable();

        MatcherAssert.assertThat(dates.columnNames(), containsInAnyOrder("localDates"));
        Assert.assertEquals(dates.rowCount(), c.size());
        Assert.assertEquals(dates.select("localDates").dataType(), LocalDate.class);
        Assert.assertEquals(c, dates.select("localDates"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void splitColumnIntoDraftTableThrowsExceptionWhenGivenDuplicateLabels() {
        new FlexibleColumn("localDates", dateCollectionHelper())
                .split()
                .intoColumn("day", LocalDate::getDayOfMonth)
                .intoColumn("day", LocalDate::getDayOfYear)
                .gatherIntoNewTable();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void splitColumnIntoDraftTableThrowsExceptionWhenEmpty() {
        new FlexibleColumn("empty", List.of())
                .split()
                .intoColumn("day", LocalDate::getDayOfMonth)
                .gatherIntoNewTable();
    }

    @Test
    public void descriptiveStatsGeneratedForIntegerType() {
        List<Integer> list = IntStream.range(0, 10).boxed().toList();
        var stats = FlexibleColumn.from("num", list).descriptiveStats();

        Assert.assertFalse(stats.isEmpty());
        Assert.assertTrue(stats.containsKey(StatisticName.MAX));
    }

    @Test
    public void emptyMapGeneratedForDescriptiveStatsOfNonNumericType() {
        Assert.assertEquals(
                FlexibleColumn.from("dates", dateCollectionHelper()).descriptiveStats(),
                Collections.emptyMap()
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

    @DataProvider(name = "parameterizedListsWithTypes")
    static Object[][] parameterizedListsWithTypes() {
        return new Object[][] {
                {asList(true, false, true), Boolean.class},
                {asList(1, 2, 3, 4, 5), Integer.class},
                {asList(1.1, 2.2, 3.3, 4.4, 5.5), Double.class},
                {asList("barista", "shift supervisor", ""), String.class},
                {asList(LocalDate.now(), LocalDate.of(2023, 1, 1)), LocalDate.class}
        };

    }

    @DataProvider(name = "containsData")
    static Object[][] containsData() {
        return new Object[][] {
                {asList(true, false, true), false},
                {asList(1, 2, 3, 4, 5), 3},
                {asList(1.1, 2.2, 3.3, 4.4, 5.5), 1.1},
                {asList("barista", "shift supervisor", ""), ""},
                {asList(LocalDate.now(), LocalDate.of(2023, 1, 1)), LocalDate.of(2023, 1, 1)}
        };
    }

    @DataProvider(name = "orderByData")
    static Object[][] orderByData() {
        return new Object[][] {
                {
                    asList(
                        LocalDate.of(2016, 12, 14),
                        LocalDate.of(2009, 9, 14),
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2012, 12, 14)
                    ),
                    asList(
                        LocalDate.of(2009, 9, 14),
                        LocalDate.of(2012, 12, 14),
                        LocalDate.of(2016, 12, 14),
                        LocalDate.of(2023, 1, 1)
                    )
                },
                {
                    asList(2016.1, 2016.2, -2016.7),
                    asList(-2016.7, 2016.1, 2016.2)
                },
                {
                    asList("donald", "goofy", "Sora"),
                    asList("Sora", "donald", "goofy")
                }
        };

    }

}
