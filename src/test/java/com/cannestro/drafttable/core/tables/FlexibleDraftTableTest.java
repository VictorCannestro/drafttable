package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.FlexibleColumn;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.supporting.utils.helper.EmploymentContract;
import com.cannestro.drafttable.supporting.utils.helper.PayDetails;
import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.rows.Row;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.cannestro.drafttable.core.options.Item.*;
import static com.cannestro.drafttable.core.options.Items.*;
import static com.cannestro.drafttable.core.options.SortingOrderType.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


/**
 * @author Victor Cannestro
 */
@Test(groups = {"component"})
public class FlexibleDraftTableTest {
    
    @Test
    public void columnNamesMatchUnderLyingObjectsFields() {
        assertThat(
                exampleDraftTableFromColumns().columnNames(),
                containsInAnyOrder("contractType", "payType", "exempt", "minor")
        );
    }

    @Test
    public void shapeReturnsColNumAndRowNum() {
        Assert.assertEquals(
                exampleDraftTableFromColumns().shape(),
                String.format("%d rows X %d columns", exampleDraftTableFromColumns().rowCount(), exampleDraftTableFromColumns().columnCount())
        );
    }

    @Test
    public void emptyDraftTablesHaveEmptyColumnNames() {
        Assert.assertEquals(
                FlexibleDraftTable.create().emptyDraftTable().columnNames(),
                Collections.emptyList()
        );
    }

    @Test
    public void renamedColumns() {
        DraftTable df = exampleDraftTableFromColumns().rename(these("contractType", "pppayType"), using("CONTRACT_TYPE", "invalid"));

        assertThat(df.columnNames(), hasItem("CONTRACT_TYPE"));
        assertThat(df.columnNames(), hasItem("payType"));
        assertThat(df.columnNames(), not(hasItem("contractType")));
        assertThat(df.columnNames(), not(hasItem("invalid")));
    }

    @Test
    public void renamingEmptyDraftTableDoesNotProduceChanges() {
        DraftTable df = FlexibleDraftTable.create().emptyDraftTable().rename(from("nonExisting"), to("newName"));

        Assert.assertEquals(df.columnNames(), Collections.emptyList());
    }

    @Test
    public void emptyDraftTablesHaveEmptyColumns() {
        Assert.assertEquals(FlexibleDraftTable.create().emptyDraftTable().columns(), Collections.emptyList());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotMakeDraftTableFromJagged2DListOfColumnData() {
        List<String> names = List.of("one", "two");
        List<List<?>> values = List.of(List.of("one", "two"), List.of());
        FlexibleDraftTable.create().fromColumnValues(names, values);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotMakeDraftTableFromJagged2DListOfRowData() {
        List<String> names = List.of("one", "two");
        List<List<?>> values = List.of(List.of("one", "two"), List.of());
        FlexibleDraftTable.create().fromRowValues(names, values);
    }

    @Test
    public void fromEmptyRowsReturnsEmptyDraftTable() {
        Assert.assertEquals(
                FlexibleDraftTable.create().fromRows(Collections.emptyList()),
                FlexibleDraftTable.create().emptyDraftTable()
        );
    }

    @Test
    public void canMakeEmptyDraftTableFromEmptyColumnWithLabel() {
        DraftTable df = FlexibleDraftTable.create().fromColumns(List.of(FlexibleColumn.from("", Collections.emptyList())));

        Assert.assertTrue(df.isEmpty());
        Assert.assertFalse(df.isCompletelyEmpty());
    }

    @Test
    public void canSelectASingleColumn() {
        Column contractTypes = exampleDraftTableFromColumns().select("contractType");

        Assert.assertEquals(contractTypes.size(), exampleDraftTableFromColumns().rowCount());
        Assert.assertEquals(contractTypes.label(), "contractType");
    }

    @Test
    public void canSelectMultipleColumns() {
        DraftTable slice = exampleDraftTableFromColumns().selectMultiple(from("contractType", "exempt", "minor"));

        Assert.assertEquals(slice.columnCount(), 3);
        Assert.assertEquals(slice.rowCount(), exampleDraftTableFromColumns().rowCount());
        assertThat(slice.columnNames(), contains("contractType", "exempt", "minor"));
    }

    @Test
    public void canDetermineIfColumnExistsOrNot() {
        Assert.assertTrue(exampleDraftTableFromColumns().hasColumn("contractType"));
        Assert.assertFalse(exampleDraftTableFromColumns().hasColumn("INVALID"));
    }

    @Test
    public void canFilterRowsBySingleColumnsMatchingValue() {
        DraftTable filteredDataFame = exampleDraftTableFromColumns().where("contractType", containsString("full"));

        Assert.assertTrue(filteredDataFame.rowCount() < exampleDraftTableFromColumns().rowCount());
    }

    @Test
    public void whereColumnTypeIsNotDoubleReturnsNonDoubleColumns() {
        Assert.assertEquals(
                exampleDraftTableFromColumns().whereColumnType(is(not(Boolean.class))).columnNames(),
                List.of("contractType", "payType")
        );
    }

    @Test
    public void whereColumnTypeIsNotDoubleReturnsAllColumnsWhenNoneMatching() {
        Assert.assertEquals(
                exampleDraftTableFromColumns().whereColumnType(is(not(Double.class))).columnNames(),
                exampleDraftTableFromColumns().columnNames()
        );
    }

    @Test
    public void whereColumnTypeFiltersOutEveryColumnThenEmptyFrameReturned() {
        Assert.assertEquals(
                exampleDraftTableFromColumns()
                        .whereColumnType(is(Double.class))
                        .whereColumnType(is(Boolean.class))
                        .columnNames(),
                FlexibleDraftTable.create().emptyDraftTable().columnNames()
        );
    }

    @Test
    public void whereColumnTypeReturnsSameEmptyFrameWhenGivenEmptyFrame() {
        Assert.assertEquals(
                FlexibleDraftTable.create().emptyDraftTable().whereColumnType(is(not(Double.class))),
                FlexibleDraftTable.create().emptyDraftTable()
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void throwsExceptionWhenFilteringRowsBySingleColumnsButColumnNotRecognized() {
        exampleDraftTableFromColumns().where("INVALID", is("Joliet"));
    }

    @Test
    public void noMatchesWhereStillReturnsEmptyDraftTableNotACompletelyEmptyDraftTable() {
        DraftTable df = exampleDraftTableFromColumns()
                .where("minor", is(true))
                .where("exempt", is(true));

        Assert.assertTrue(df.isEmpty());
        Assert.assertFalse(df.isCompletelyEmpty());
    }

    @Test
    public void whereWithDefaultReturnsDefaultMatcherWhenConditionNotMet() {
        DraftTable df = exampleDraftTableFromColumns().whereWithDefault("contractType", is("INVALID"), is("full-time"));

        Assert.assertFalse(df.isEmpty());
        Assert.assertFalse(exampleDraftTableFromColumns().select("contractType").has("INVALID"));
    }

    @Test
    public void whereWithDefaultReturnsFirstMatcherWhenConditionMet() {
        DraftTable df = exampleDraftTableFromColumns().whereWithDefault("contractType", is("full-time"), is("INVALID"));

        Assert.assertFalse(df.isEmpty());
        Assert.assertFalse(exampleDraftTableFromColumns().select("contractType").has("INVALID"));
    }

    @Test
    public void whereWithDefaultByColumnAspectReturnsFirstMatcherWhenConditionMet() {
        DraftTable df = exampleDraftTableFromColumnValues()
                .melt("days", "dates", as("newDates"), (Integer days, LocalDate date) -> date.plusDays(days))
                .whereWithDefault("newDates", LocalDate::getDayOfMonth, is(2), is(greaterThan(2)));

        Assert.assertEquals(
                df.select("newDates").firstValue().get(),
                LocalDate.of(2024, 1, 2)
        );
    }

    @Test
    public void whereWithDefaultByColumnAspectReturnsDefaultMatcherWhenConditionNotMet() {
        DraftTable df = exampleDraftTableFromColumnValues()
                .melt("days", "dates", as("newDates"), (Integer days, LocalDate date) -> date.plusDays(days))
                .whereWithDefault("newDates", LocalDate::getDayOfMonth, is(lessThan(0)), is(2));

        Assert.assertEquals(
                df.select("newDates").firstValue().get(),
                LocalDate.of(2024, 1, 2)
        );
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void whereByColumnSubFieldOnNullDataShouldThrowError() {
        Column c = FlexibleColumn.from("data", Collections.nCopies(3, null))
                                 .append("NON_NULL");
        FlexibleDraftTable.create().fromColumns(List.of(c))
                         .where("data", String::length, not(nullValue()));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void whereWithDefaultByColumnSubFieldOnNullDataShouldThrowError() {
        Column c = FlexibleColumn.from("data", Collections.nCopies(3, null))
                .append("NON_NULL");
        FlexibleDraftTable.create().fromColumns(List.of(c))
                .whereWithDefault("data", String::length, not(nullValue()), nullValue());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotAppendMisMatchingColumns() {
        DraftTable draftTable = FlexibleDraftTable.create().fromColumns(List.of(FlexibleColumn.from("rate", List.of("12.33", "23.77"))));
        DraftTable draftTable2 = FlexibleDraftTable.create().fromColumns(List.of(FlexibleColumn.from("date", List.of(LocalDate.now(), LocalDate.now()))));
        draftTable.append(draftTable2);
    }

    @Test
    public void whenAppendingEmptyDraftTableThenExistingDraftTableIsReturned() {
        DraftTable df = exampleDraftTableFromColumns();
        Assert.assertEquals(df.append(FlexibleDraftTable.create().emptyDraftTable()), df);
    }

    @Test
    public void whenAppendingNonEmptyDraftTableToAnEmptyDraftTableThenNonEmptyDraftTableIsReturned() {
        DraftTable df = exampleDraftTableFromColumns();
        Assert.assertEquals(FlexibleDraftTable.create().emptyDraftTable().append(df), df);
    }

    @Test
    public void canFilterRowsByChainingSingleColumnsMatchingValue() {
        DraftTable filteredDataFame = exampleDraftTableFromColumns()
                .where("contractType", containsString("full"))
                .where("payType", is("salary"))
                .where("minor", is(false));

        Assert.assertTrue(filteredDataFame.rowCount() < exampleDraftTableFromColumns().rowCount());
    }

    @Test
    public void selectsCorrespondingIndicesInWhereMethod() {
        Assert.assertEquals(exampleDraftTableFromColumns().where(List.of(0)).rowCount(), 1);
        Assert.assertEquals(exampleDraftTableFromColumns().where(List.of(exampleDraftTableFromColumns().rowCount()-1)).rowCount(), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void throwsExceptionWhenIndicesOutsideLowerBoundInWhereMethod() {
        Assert.assertEquals(exampleDraftTableFromColumns().where(List.of(-1)).rowCount(), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void throwsExceptionWhenIndicesOutsideUpperBoundInWhereMethod() {
        Assert.assertEquals(exampleDraftTableFromColumns().where(List.of(exampleDraftTableFromColumns().rowCount())).rowCount(), 1);
    }

    @Test
    public void canPeekFirst2Rows() {
        Assert.assertEquals(exampleDraftTableFromColumns().top(2).rowCount(), 2);
    }

    @Test
    public void canPeekLast2Rows() {
        Assert.assertEquals(exampleDraftTableFromColumns().bottom(2).rowCount(), 2);
    }

    @Test
    public void canPeekRandom2Rows() {
        Assert.assertEquals(exampleDraftTableFromColumns().randomDraw(2).rowCount(), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void canPeekFirstUsingANegative() {
        exampleDraftTableFromColumns().top(-1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void canPeekLastUsingANegative() {
        exampleDraftTableFromColumns().bottom(-1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void canPeekRandomUsingANegative() {
        exampleDraftTableFromColumns().randomDraw(-1);
    }

    @Test
    public void canPeekLastUpToBeginningRow() {
        Assert.assertEquals(
                exampleDraftTableFromColumns().bottom(exampleDraftTableFromColumns().rowCount() + 100).rowCount(),
                exampleDraftTableFromColumns().rowCount()
        );
    }

    @Test
    public void canPeekFirstUpToLastRow() {
        Assert.assertEquals(
                exampleDraftTableFromColumns().top(exampleDraftTableFromColumns().rowCount() + 100).rowCount(),
                exampleDraftTableFromColumns().rowCount()
        );
    }

    @Test
    public void canPeekRandomUpToRowCount() {
        Assert.assertEquals(
                exampleDraftTableFromColumns().randomDraw(exampleDraftTableFromColumns().rowCount() + 100).rowCount(),
                exampleDraftTableFromColumns().rowCount()
        );
    }

    @Test
    public void canAppendFirstRowWithLastRow() {
        Assert.assertEquals(
                exampleDraftTableFromColumns().top(1).append(exampleDraftTableFromColumns().bottom(1)).rowCount(),
                2
        );
    }

    @Test
    public void canDetermineColumnNamesInTheCorrectOrder() {
        List<String> columnNamesInReverseOrder = exampleDraftTableFromColumns().columnNames();
        columnNamesInReverseOrder.sort(Comparator.reverseOrder());

        Assert.assertNotEquals(
                exampleDraftTableFromColumns().columnNames(),
                columnNamesInReverseOrder
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void willThrowExceptionWhenAppendingDraftTablesWithMoreColumns() {
        exampleDraftTableFromColumns().top(1)
                .selectMultiple(using("payType", "exempt", "minor"))
                .append(exampleDraftTableFromColumns().bottom(1));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void willThrowExceptionWhenAppendingDraftTablesWithFewerColumns() {
        exampleDraftTableFromColumns().bottom(1).append(
                exampleDraftTableFromColumns().top(1).selectMultiple(using("payType", "exempt", "minor"))
        );
    }

    @Test
    public void whenAddingNonEmptyColumnToEmptyDraftTableANonEmptyDraftTableIsProduced() {
        Assert.assertEquals(
                FlexibleDraftTable.create().emptyDraftTable().addColumn("contractType", asList("", "", ""), ""),
                FlexibleDraftTable.create().fromColumns(List.of(FlexibleColumn.from("contractType", asList("", "", ""))))
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotAddColumnWithSameNameAsExisting() {
        exampleDraftTableFromColumns().addColumn("contractType", asList("", "", ""), "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotAddColumnWithMoreEntriesThanExisting() {
        exampleDraftTableFromColumns().addColumn(
                "loginName",
                asList("US112233", "CA112244", "US112255", "US112266", "extra row"),
                ""
        );
    }

    @Test
    public void canAddNewUniqueColumnAndFillMissingRowValue() {
        DraftTable df = exampleDraftTableFromColumns().addColumn("loginName", asList("US112233", "CA112244"), "");

        Assert.assertEquals(df.columnCount(), 5);
        Assert.assertEquals(df.rowCount(), 3);
        Assert.assertTrue(df.hasColumn("loginName"));
        Assert.assertEquals(
                df.select("loginName").values(),
                asList("US112233", "CA112244", "")
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void whenAddingMultipleEmptyColumnsThenExceptionIsThrown() {
        exampleDraftTableFromColumns().addColumns(of(
                FlexibleColumn.from("col n+1", Collections.emptyList()),
                FlexibleColumn.from("col n+1", Collections.emptyList())
        ));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void whenAddingMultipleJaggedColumnsThenExceptionIsThrown() {
        exampleDraftTableFromColumns().addColumns(of(
                FlexibleColumn.from("col n+1", List.of(1,2,3)),
                FlexibleColumn.from("col n+2", List.of(0))
        ));
    }

    @Test
    public void canAddMultipleColumnsOfSameLength() {
        DraftTable df = exampleDraftTableFromColumns().addColumns(
                these(exampleDraftTableFromColumnValues().columns())
        );

        Assert.assertEquals(
                df.columnCount(),
                exampleDraftTableFromColumns().columnCount() + exampleDraftTableFromColumnValues().columnCount()
        );
    }

    @Test
    public void whenCallingTopOfEmptyDraftTableReturnsEmptyDraftTable() {
        Assert.assertEquals(FlexibleDraftTable.create().emptyDraftTable().top(10).rowCount(), 0);
        Assert.assertEquals(FlexibleDraftTable.create().emptyDraftTable().top(10).columnCount(), 0);
    }

    @Test
    public void whenCallingBottomOfEmptyDraftTableReturnsEmptyDraftTable() {
        Assert.assertEquals(FlexibleDraftTable.create().emptyDraftTable().bottom(10).rowCount(), 0);
        Assert.assertEquals(FlexibleDraftTable.create().emptyDraftTable().bottom(10).columnCount(), 0);
    }

    @Test
    public void whenDropping1ColumnTheResultingDraftTableDoesNotContainTheDroppedColumn() {
        DraftTable updatedFrame = exampleDraftTableFromColumns().dropColumn("minor");

        Assert.assertEquals(updatedFrame.rowCount(), exampleDraftTableFromColumns().rowCount());
        Assert.assertEquals(updatedFrame.columnCount(), exampleDraftTableFromColumns().columnCount() - 1);
        assertThat(updatedFrame.columnNames(), not("minor"));
    }

    @Test
    public void whenDroppingMultipleColumnsTheResultingDraftTableDoesNotContainTheDroppedColumns() {
        DraftTable updatedFrame = exampleDraftTableFromColumns().dropColumns(these("payType", "minor"));

        Assert.assertEquals(updatedFrame.rowCount(), exampleDraftTableFromColumns().rowCount());
        Assert.assertEquals(updatedFrame.columnCount(), exampleDraftTableFromColumns().columnCount() - 2);
        assertThat(updatedFrame.columnNames(), not(contains("payType", "minor")));
    }

    @Test
    public void canDropSingleColumn() {
        DraftTable df = exampleDraftTableFromColumnValues().dropColumn("days");

        assertThat(df.columnNames(), not(contains("days")));
    }

    @Test
    public void whenDroppingAllColumnsAnEmptyDraftTableIsReturned() {
        DraftTable df = exampleDraftTableFromColumnValues();
        df = df.dropColumns(these(df.columnNames()));

        Assert.assertTrue(df.isEmpty());
        Assert.assertTrue(df.isCompletelyEmpty());
    }

    @Test
    public void canDropMultipleColumns() {
        DraftTable df = exampleDraftTableFromColumnValues().dropColumns(these("days", "dates"));

        assertThat(df.columnNames(), not(contains("days", "dates")));
    }

    @Test
    public void canDropAllExceptSpecifiedColumns() {
        DraftTable df = exampleDraftTableFromColumnValues().dropAllExcept(these("days", "dates"));

        Assert.assertEquals(df.columnCount(), 2);
        assertThat(df.columnNames(), contains("days", "dates"));
    }

    @Test
    public void whenDropAllExceptIsProvidedAllColumnNamesThenNoColumnsDropped() {
        DraftTable df = exampleDraftTableFromColumnValues().dropAllExcept(these(exampleDraftTableFromColumnValues().columnNames()));

        Assert.assertEquals(df.columnCount(), exampleDraftTableFromColumnValues().columnCount());
        Assert.assertEquals(df, exampleDraftTableFromColumnValues());
    }

    @Test
    public void whenDropAllExceptIsProvidedEmptyListThenAllColumnsDropped() {
        DraftTable df = exampleDraftTableFromColumnValues().dropAllExcept(these());

        Assert.assertEquals(df.columnCount(), 0);
        Assert.assertEquals(df, FlexibleDraftTable.create().emptyDraftTable());
    }

    @Test
    public void whenDropAllExceptOfEmptyFrameIsProvidedEmptyListThenEmptyFrameIsReturned() {
        DraftTable df = FlexibleDraftTable.create().emptyDraftTable().dropAllExcept(these());

        Assert.assertEquals(df.columnCount(), 0);
        Assert.assertEquals(df, FlexibleDraftTable.create().emptyDraftTable());
    }

    @Test
    public void whenTransformingColumnsTheActedUponColumnIsReplaced() {
        DraftTable transformedDateDraftTable = exampleDraftTableFromColumnValues()
                .transform("dates", into("localDateTime"), (LocalDate date) -> date.atStartOfDay().plusDays(20))
                .transform("localDateTime", into("usLocalDate"), (LocalDateTime date) -> date.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));

        Assert.assertEquals(
                transformedDateDraftTable.rowCount(),
                exampleDraftTableFromColumnValues().rowCount()
        );
        Assert.assertEquals(
                transformedDateDraftTable.columnCount(),
                exampleDraftTableFromColumnValues().columnCount()
        );
        assertThat(transformedDateDraftTable.columnNames(), not(contains("dates")));
        assertThat(transformedDateDraftTable.columnNames(), hasItem("usLocalDate"));
    }

    @Test
    public void canTransformInplaceWhenDraftTableHasSingleColumn(){
        DraftTable df = EmploymentContractDraftTable().transform("EmploymentContracts", (EmploymentContract ec) -> ec.getType().equals("full-time"));

        Assert.assertEquals(df.select("EmploymentContracts").dataType(), Boolean.class);
    }

    @Test
    public void canDeriveNewColumnOfDifferentTypeFromSingleInputColumnType() {
        DraftTable df = exampleDraftTableFromColumnValues()
                .deriveNewColumnFrom("dates", as("modifiedDates"), (LocalDate date) -> date.getDayOfWeek());

        Assert.assertEquals(
                df.select("modifiedDates").values(),
                List.of(DayOfWeek.MONDAY, DayOfWeek.MONDAY, DayOfWeek.MONDAY)
        );
    }

    @Test
    public void canDeriveNewColumnOfDifferentTypeFromMultipleInputColumnTypes() {
        DraftTable df = exampleDraftTableFromColumnValues()
                .deriveNewColumnFrom("days", "dates", as("modifiedDates"),
                        (Integer daysToAdd, LocalDate date) -> date.plusDays(daysToAdd).getDayOfWeek());

        Assert.assertEquals(
                df.select("modifiedDates").values(),
                List.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
        );
    }

    @Test
    public void rowCallMatchesUnderlyingData() {
        Assert.assertEquals(
                exampleDraftTableFromRowValues().rows(),
                List.of(
                        HashMapRow.from(List.of("days", "dates", "names"), List.of(1, LocalDate.of(2024, 1, 1), "Alice")),
                        HashMapRow.from(List.of("days", "dates", "names"), List.of(2, LocalDate.of(2024, 1, 1), "Bob")),
                        HashMapRow.from(List.of("days", "dates", "names"), List.of(3, LocalDate.of(2024, 1, 1), "Jose"))
                )
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void fromRowsMustHaveEquivalentKeySemantics() {
        FlexibleDraftTable.create().fromRows(
                List.of(
                        HashMapRow.from(List.of("days", "dates", "names"), List.of(1, LocalDate.of(2024, 1, 1), "Alice")),
                        HashMapRow.from(List.of("days", "dates", "NAMES"), List.of(2, LocalDate.of(2024, 1, 1), "Bob"))
                )
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void fromRowsMustHaveEquivalentKeySizes() {
        FlexibleDraftTable.create().fromRows(
                List.of(
                        HashMapRow.from(List.of("days", "dates", "names"), List.of(1, LocalDate.of(2024, 1, 1), "Alice")),
                        HashMapRow.from(List.of("days", "dates"), List.of(3, LocalDate.of(2024, 1, 1)))
                )
        );
    }

    @Test
    public void columnCallMatchesUnderlyingData() {
        Assert.assertEquals(
                exampleDraftTableFromColumns().columns(),
                List.of(
                        FlexibleColumn.from("contractType", List.of("full-time", "full-time", "part-time")),
                        FlexibleColumn.from("payType", List.of("salary", "salary", "hourly")),
                        FlexibleColumn.from("exempt", List.of(true, false, false)),
                        FlexibleColumn.from("minor", List.of(false, false, true))
                )
        );
    }

    @Test
    public void applyCanMutateUnderlyingColumnStateForMutableValues() {
        Column col = FlexibleDraftTable.create().fromColumns(List.of(new FlexibleColumn(
                        "job names",
                        asList(new ArrayList<>(asList("BAR", "SSV")), new ArrayList<>(asList("ASM", "SM")))
                ))).apply("job names", (List<String> list) -> list.set(0, "RMT"))
                .select("job names");

       Assert.assertEquals(
               col.values(),
               List.of(asList("RMT", "SSV"), asList("RMT", "SM"))
       );
    }

    @Test
    public void canSortByColumnInAscendingOrder() {
        DraftTable sortedFrame = exampleDraftTableFromColumns()
                .orderBy("contractType", ASCENDING);

        Assert.assertEquals(
                sortedFrame.top(1).select("contractType").firstValue().get(),
                "full-time"
        );
        Assert.assertEquals(
                sortedFrame.bottom(1).select("contractType").firstValue().get(),
                "part-time"
        );
    }

    @Test
    public void canSortByColumnInDescendingOrder() {
        DraftTable sortedFrame = exampleDraftTableFromColumns()
                .orderBy("contractType", DESCENDING);

        Assert.assertEquals(
                sortedFrame.top(1).select("contractType").firstValue().get(),
                "part-time"
        );
        Assert.assertEquals(
                sortedFrame.bottom(1).select("contractType").firstValue().get(),
                "full-time"
        );
    }

    @Test
    public void canSortByContractTypeThenExempt() {
        DraftTable sortedFrame = exampleDraftTableFromColumns()
                .orderByMultiple(using("contractType", "exempt"), ASCENDING);

        Assert.assertEquals(
                sortedFrame.top(1).select("contractType").firstValue().get(),
                "full-time"
        );
        Assert.assertEquals(
                sortedFrame.top(1).select("exempt").firstValue().get(),
                false
        );

        Assert.assertEquals(
                sortedFrame.top(2).select("contractType").values().get(1),
                "full-time"
        );
        Assert.assertEquals(
                sortedFrame.top(2).select("exempt").values().get(1),
                true
        );

        Assert.assertEquals(
                sortedFrame.bottom(1).select("contractType").firstValue().get(),
                "part-time"
        );
        Assert.assertEquals(
                sortedFrame.bottom(1).select("exempt").firstValue().get(),
                false
        );
    }

    @Test
    public void canSortByRowComparator() {
        DraftTable sortedFrame = exampleDraftTableFromColumnValues()
                .deriveNewColumnFrom("days", "dates",
                        as("modifiedDates"),
                        (Integer daysToAdd, LocalDate date) -> date.plusDays(daysToAdd))
                .orderBy(Comparator.comparing((Row row) -> row.valueOf("modifiedDates")));

        Assert.assertEquals(
                sortedFrame.top(1).select("modifiedDates").firstValue().get(),
                LocalDate.of(2024, 1, 2)
        );
        Assert.assertEquals(
                sortedFrame.bottom(1).select("modifiedDates").firstValue().get(),
                LocalDate.of(2024, 1, 4)
        );
    }

    @Test
    public void canFilterByAnAspectOfAColumnOfComplexObjects() {
        DraftTable df = EmploymentContractDraftTable()
                .where("EmploymentContracts", EmploymentContract::getType, is("full-time"));

        Assert.assertEquals(df.rowCount(), 1);
        Assert.assertEquals(
                df.select("EmploymentContracts").firstValue().get(),
                new EmploymentContract("full-time", "Y", new PayDetails("Salary", "50000.00", "Bi-Weekly", "80"), LocalDate.of(2024, 1, 1))
        );
    }

    @Test
    public void canFilterByARow() {
        DraftTable df = EmploymentContractDraftTable()
                .where((Row row) -> ((EmploymentContract) row.valueOf("EmploymentContracts")).getType(), is("full-time"));

        Assert.assertEquals(df.rowCount(), 1);
        Assert.assertEquals(
                df.select("EmploymentContracts").firstValue().get(),
                new EmploymentContract("full-time", "Y", new PayDetails("Salary", "50000.00", "Bi-Weekly", "80"), LocalDate.of(2024, 1, 1))
        );
    }

    @Test
    public void canMapDraftTableIntoSingleColumnOfOriginatingType(){
        DraftTable df = FlexibleDraftTable.create().fromRows(List.of(
                HashMapRow.from(new EmploymentContract("part-time", "N", new PayDetails("Hourly", "25.00", "Bi-Weekly", "80"), LocalDate.now())),
                HashMapRow.from(new EmploymentContract("part-time", "N", new PayDetails("Hourly", "18.50", "Bi-Weekly", "80"), LocalDate.now())),
                HashMapRow.from(new EmploymentContract("full-time", "Y", new PayDetails("Salary", "50000.00", "Bi-Weekly", "80"), LocalDate.of(2024, 1, 1)))
        ));
        df.write().structure();
        Column c = df.gatherInto(EmploymentContract.class, as("EmploymentContracts"));

        Assert.assertEquals(c, EmploymentContractDraftTable().select("EmploymentContracts"));
    }

    @Test
    public void canGatherSelectionIntoReducedDraftTable() {
        DraftTable df = FlexibleDraftTable.create().fromRows(List.of(
                        HashMapRow.from(new PayDetails("Hourly", "18.50", "Bi-Weekly", "80")),
                        HashMapRow.from(new PayDetails("Salary", "50000.00", "Bi-Weekly", "80"))))
                .addColumn(FlexibleColumn.from("country", List.of("US", "CA")))
                .gatherInto(PayDetails.class, as("pay"), using("type", "rate", "period", "workHours"));

        Assert.assertEquals(df.columnNames(), List.of("country", "pay"));
        Assert.assertEquals(
                df,
                FlexibleDraftTable.create().fromColumns(List.of(
                        FlexibleColumn.from("country", List.of("US", "CA")),
                        FlexibleColumn.from("pay", List.of(new PayDetails("Hourly", "18.50", "Bi-Weekly", "80"), new PayDetails("Salary", "50000.00", "Bi-Weekly", "80")))
                ))
        );
    }

    @Test
    public void canGatherIntoIfOneOrMoreOfTheTargetObjectsFieldsAreNull() {
        Row row = HashMapRow.from(
                new EmploymentContract(null, null, null, null)
        );
        Column c = FlexibleDraftTable.create().emptyDraftTable()
                .append(row)
                .gatherInto(EmploymentContract.class, as(""));

        Assert.assertEquals(
                c.bottom(1).firstValue().get(),
                new EmploymentContract(null, null, null, null)
        );
    }

    @Test
    public void replacingAllOnlyReplacesMatchingValues(){
        DraftTable draftTable = FlexibleDraftTable.create().fromColumns(List.of(
                FlexibleColumn.from("country", Arrays.asList("US", "CA", "NULL")),
                FlexibleColumn.from("state", List.of("NY", "PA", "NULL")),
                FlexibleColumn.from("hasNewProduct", List.of(true, false, false))
        ));
        DraftTable df = draftTable.replaceAll("NULL", null);

        Assert.assertTrue(df.select("country").hasNulls());
        Assert.assertTrue(df.select("state").hasNulls());
    }

    @Test
    public void whenReplacingAllHasNoMatchesAnIdenticalDraftTableIsReturned() {
        DraftTable draftTable = FlexibleDraftTable.create().fromColumns(List.of(
                FlexibleColumn.from("country", Arrays.asList("US", "CA", "NULL")),
                FlexibleColumn.from("state", List.of("NY", "PA", "NULL")),
                FlexibleColumn.from("hasNewProduct", List.of(true, false, false))
        ));
        DraftTable df = draftTable.replaceAll(42, null);

        Assert.assertEquals(draftTable, df);
    }

    @Test
    public void DraftTableCopyReturnsADeepCopy() {
        Column c = new FlexibleColumn(
                "job names",
                asList(new ArrayList<>(asList("BAR", "SSV")), new ArrayList<>(asList("ASM", "SM")))
        );
        DraftTable df = FlexibleDraftTable.create().fromColumns(List.of(c));
        DraftTable copy = df.copy();

        df.select("job names").apply((List<String> list) -> list.removeIf(name -> name.contains("A")));

        Assert.assertEquals(
                df.select("job names").values(),
                asList(new ArrayList<>(List.of("SSV")), new ArrayList<>(List.of("SM")))
        );
        Assert.assertEquals(
                copy.select("job names").values(),
                asList(new ArrayList<>(asList("BAR", "SSV")), new ArrayList<>(asList("ASM", "SM")))
        );
    }

    @Test
    public void introspectAllowSelfReferencesInPipeline() {
        DraftTable draftTable = EmploymentContractDraftTable()
                .introspect(df -> FlexibleDraftTable.create().emptyDraftTable()
                        .append(these(df.rows().stream()
                                .map(row -> row.valueOf("EmploymentContracts"))
                                .map(Mappable.class::cast)
                                .map(HashMapRow::from)
                                .map(hmr -> (Row) hmr)
                                .toList())));

        assertThat(draftTable.columnNames(), containsInAnyOrder("type", "exemptInd", "payDetails", "effectiveDate"));
    }

    @Test
    public void canMakeDraftTableFromListOfObjects() {
        List<EmploymentContract> testData = List.of(
                new EmploymentContract("part-time", "N", new PayDetails("Hourly", "25.00", "Bi-Weekly", "80"), LocalDate.now()),
                new EmploymentContract("part-time", "N", new PayDetails("Hourly", "18.50", "Bi-Weekly", "80"), LocalDate.now()),
                new EmploymentContract("full-time", "Y", new PayDetails("Salary", "50000.00", "Bi-Weekly", "80"), LocalDate.of(2024, 1, 1))
        );
        DraftTable df = FlexibleDraftTable.create().fromObjects(testData);

        FlexibleDraftTable.create()
                .fromColumns(List.of(df.select("payDetails"))).write().structure();
        Assert.assertEqualsNoOrder(df.columnNames(), List.of("type", "exemptInd", "payDetails", "effectiveDate"));
        Assert.assertEquals(df.rowCount(), testData.size());
        Assert.assertEquals(
                FlexibleDraftTable.create()
                        .fromObjects(df.select("payDetails").values())
                        .gatherInto(PayDetails.class, as(""))
                        .dataType(),
                PayDetails.class
        );
    }


    /* ----------------------------------------------------------------------------- */
    /* --------------------------Test Data and DataProviders------------------------ */
    /* ----------------------------------------------------------------------------- */

    DraftTable EmploymentContractDraftTable() {
        return FlexibleDraftTable.create().fromColumns(List.of(
                new FlexibleColumn("EmploymentContracts", List.of(
                        new EmploymentContract("part-time", "N", new PayDetails("Hourly", "25.00", "Bi-Weekly", "80"), LocalDate.now()),
                        new EmploymentContract("part-time", "N", new PayDetails("Hourly", "18.50", "Bi-Weekly", "80"), LocalDate.now()),
                        new EmploymentContract("full-time", "Y", new PayDetails("Salary", "50000.00", "Bi-Weekly", "80"), LocalDate.of(2024, 1, 1))
                    )
                )
        ));
    }

    DraftTable exampleDraftTableFromColumns() {
        return FlexibleDraftTable.create().fromColumns(
                List.of(
                        FlexibleColumn.from("contractType", List.of("full-time", "full-time", "part-time")),
                        FlexibleColumn.from("payType", List.of("salary", "salary", "hourly")),
                        FlexibleColumn.from("exempt", List.of(true, false, false)),
                        FlexibleColumn.from("minor", List.of(false, false, true))
                )
        );
    }

    DraftTable exampleDraftTableFromColumnValues() {
        return FlexibleDraftTable.create().fromColumnValues(
                List.of("days", "dates", "names"),
                List.of(
                        List.of(1, 2, 3),
                        List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1)),
                        List.of("Alice", "Bob", "Jose")
                )
        );
    }

    DraftTable exampleDraftTableFromRowValues() {
        return FlexibleDraftTable.create().fromRowValues(
                List.of("days", "dates", "names"),
                List.of(
                        List.of(1, LocalDate.of(2024, 1, 1), "Alice"),
                        List.of(2, LocalDate.of(2024, 1, 1), "Bob"),
                        List.of(3, LocalDate.of(2024, 1, 1), "Jose")
                )
        );
    }

}
