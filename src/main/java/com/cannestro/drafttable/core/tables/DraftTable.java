package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.columns.ColumnSplitter;
import com.cannestro.drafttable.core.outbound.DraftTableOutput;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.options.Item;
import com.cannestro.drafttable.core.options.Items;
import com.cannestro.drafttable.core.options.SortingOrderType;

import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.*;


/**
 * @author Victor Cannestro
 */
public interface DraftTable {

    String DEFAULT_TABLE_NAME = "important_data";

    /**
     * Fetches the current number of rows or records present in the {@code DraftTable}.
     *
     * @return An integer within [0,n]
     */
    int rowCount();

    /**
     * Fetches the current number of columns present in the {@code DraftTable}.
     *
     * @return An integer within [0,n]
     */
    int columnCount();

    String tableName();

    DraftTable nameTable(@NonNull String newTableName);

    /**
     * Fetches the labels of every column present in the {@code DraftTable}.
     *
     * @return A list of zero or more labels
     */
    List<String> columnNames();

    /**
     * <p><b>Requires</b>: The target column names to replace are in a 1-1 mapping with a value in the new column names.
     *                     Duplicate names are not allowed. </p>
     * <br>
     * <p><b>Guarantees</b>: A new {@code DraftTable} is created using the target names and any remaining columns. </p>
     *
     * @param targetColumnNames The column names to replace
     * @param newColumnNames The new column names to apply
     * @return A new {@code DraftTable}
     */
    DraftTable rename(@NonNull Items<String> targetColumnNames, @NonNull Items<String> newColumnNames);

    /**
     * Will be true if and only if the provided label is an exact match to any column label in the {@code DraftTable}.
     *
     * @param name A string label
     * @return true or false
     */
    boolean hasColumn(@NonNull String name);

    /**
     * Constructs a replica of the current state of the {@code DraftTable}.
     *
     * @return A new {@code DraftTable}
     */
    DraftTable copy();

    /**
     * Converts the current state of the {@code DraftTable} into a collection of {@code Row} objects--order is preserved.
     *
     * @return A list of {@code Row} objects
     */
    List<Row> rows();

    /**
     * Converts the current state of the {@code DraftTable} into a collection of {@code Column} objects--order is
     * preserved.
     *
     * @return A list of {@code Column} objects
     */
    List<Column> columns();

    /**
     * Fetches the {@code Column} matching the provided label, if it exists.
     *
     * @param columnName A string label
     * @return The matching {@code Column}
     */
    Column select(@NonNull String columnName);

    /**
     * Slices the {@code DraftTable} into a subset matching the provided labels, if they exist.
     *
     * @param columnNames A list of string labels
     * @return A new {@code DraftTable} subset
     */
    DraftTable select(@NonNull String... columnNames);

    /**
     * Selects the subset of the {@code DraftTable} that matches the specified column label and selection criteria within
     * the specified column. It may be empty.
     *
     * @param columnName A string label
     * @param matcher Any matcher of compatible type
     * @return A new {@code DraftTable} subset
     */
    DraftTable where(@NonNull String columnName, @NonNull Matcher<?> matcher);

    /**
     * Selects the subset of the {@code DraftTable} that matches the specified row indices. It may be empty.
     *
     * @param indices A list of non-duplicated integers within [0,n)
     * @return A new {@code DraftTable} subset
     */
    DraftTable where(@NonNull List<Integer> indices);

    /**
     * Selects the subset of the {@code DraftTable} that matches the specified row aspect and selection criteria. It may
     * be empty. This method can be used to access and match on the fields of a row whose values are of a more complex
     * object type.
     *
     * @param rowAspect A mapping of the row values to some derived aspect
     * @param matcher Any matcher of compatible type
     * @return A new {@code DraftTable} subset
     * @param <R> The output type of the rowAspect's mapping
     */
    <R> DraftTable where(@NonNull Function<Row, R> rowAspect, @NonNull Matcher<R> matcher);

    /**
     * Selects the subset of the {@code DraftTable} that matches the specified column label and selection criteria within
     * the specified column. It may be empty. This method can be used to access and match on the fields of a
     * column whose values are of a more complex object type.
     *
     * @param columnName A string label
     * @param columnAspect A mapping of the column values to some derived aspect
     * @param matcher Any matcher of compatible type
     * @return A new {@code DraftTable} subset
     * @param <T> The type of the specified column
     * @param <R> The output type of the columnAspect's mapping
     */
    <T, R> DraftTable where(@NonNull String columnName,
                            @NonNull Function<T, R> columnAspect,
                            @NonNull Matcher<R> matcher);

    /**
     * Selects the subset of the {@code DraftTable} with columns that match the specified matching conditions.
     *
     * @param classMatcher Any matcher of compatible type
     * @return A new {@code DraftTable} subset
     * @param <T> The target type of the matcher
     */
    <T> DraftTable whereColumnType(@NonNull Matcher<Class<T>> classMatcher);

    /**
     * Selects the subset of the {@code DraftTable} that matches the specified column label and selection criteria within
     * the specified column. If no matches are found, the results of the default matcher will be returned. It may be
     * empty.
     *
     * @param columnName A string label
     * @param matcher Any matcher of compatible type
     * @param defaultMatcher Any matcher of compatible type
     * @return A new {@code DraftTable} subset
     */
    DraftTable whereWithDefault(@NonNull String columnName,
                                @NonNull Matcher<?> matcher,
                                @NonNull Matcher<?> defaultMatcher);


    /**
     * Selects the subset of the {@code DraftTable} that matches the specified column label and selection criteria within
     * the specified column. It may be empty. This method can be used to access and match on the fields of a  column
     * whose values are of a more complex object type. If no matches are found, the results of the default matcher will
     * be returned. It may be empty.
     *
     * @param columnName A string label
     * @param columnAspect A mapping of the column values to some derived aspect
     * @param matcher Any matcher of compatible type
     * @param defaultMatcher Any matcher of compatible type
     * @return A new {@code DraftTable} subset
     * @param <T> The type of the specified column
     * @param <R> The output type of the columnAspect's mapping
     */
    <T, R> DraftTable whereWithDefault(@NonNull String columnName,
                                       @NonNull Function<T, R> columnAspect,
                                       @NonNull Matcher<R> matcher,
                                       @NonNull Matcher<R> defaultMatcher);

    /**
     * Replaces all instances of the target value with the replacement value across all columns in the {@code DraftTable},
     * if it exists. If no instances of the target value exist, an equivalent {@code DraftTable} will be
     *
     * @param target The value to be replaced
     * @param replacement The value to swap in
     * @return A new {@code DraftTable}
     * @param <T> The type of the data value in 1 or more columns
     */
    <T> DraftTable replaceAll(@Nullable T target, @Nullable T replacement);

    /**
     * Can be used to inspect and access the current state of the pipeline inline without using intermediate variables.
     *
     * @param action The function to apply
     * @return A new {@code DraftTable}
     */
    DraftTable introspect(@NonNull UnaryOperator<DraftTable> action);

    /**
     * Selects between alternative pipeline paths based upon the provided predicate. Can be used to fork the pipeline
     * inline without needing to break the flow to compare the current state using intermediate variables.
     *
     * @param conditional The predicate that will determine the conditional action taken
     * @param actionIfTrue The function to apply if the conditional evaluates to true
     * @param actionIfFalse The function to apply if the conditional evaluates to false
     * @return A new {@code DraftTable}
     */
    DraftTable conditionalAction(@NonNull Predicate<DraftTable> conditional,
                                 @NonNull UnaryOperator<DraftTable> actionIfTrue,
                                 @NonNull UnaryOperator<DraftTable> actionIfFalse);

    /**
     * Produces a new {@code DraftTable} by selecting rows, from the top, up to {@code nRows} or the total row count,
     * whichever is smaller.
     *
     * @param nRows A non-negative integer
     * @return A new {@code DraftTable} subset
     */
    DraftTable top(int nRows);

    /**
     * Produces a new {@code DraftTable} by selecting rows, from the bottom, up to {@code nRows} or the total row count,
     * whichever is smaller.
     *
     * @param nRows A non-negative integer
     * @return A new {@code DraftTable} subset
     */
    DraftTable bottom(int nRows);

    /**
     * Produces a new {@code DraftTable} by selecting entries at random, up to {@code nRows} or the total row count,
     * whichever is smaller.
     *
     * @param nRows A non-negative integer
     * @return A new {@code DraftTable} subset
     */
    DraftTable randomDraw(int nRows);

    /**
     * Orders the data according to the provided {@code Row} comparator. Users are responsible for selecting supported
     * data and using supported types.
     * <pre>{@code
     * DraftTable sortedFrame = exampleDraftTable().orderBy(Comparator.comparing((Row row) -> row.valueOf("modifiedDates"))
     * }</pre>
     * @param comparator A compatible comparator
     * @return A new {@code DraftTable}
     */
    DraftTable orderBy(@NonNull Comparator<Row> comparator);

    /**
     * Orders the data in ascending or descending order as specified. Null values will appear first, followed by any
     * sorted non-null values in <b>natural order</b>. Ties are broken by the next specified column's values.
     *
     * @param columnName A string label
     * @param sortingOrderType Specifying ascending or descending order
     * @return A new {@code DraftTable}
     */
    DraftTable orderBy(@NonNull String columnName, @NonNull SortingOrderType sortingOrderType);

    /**
     * Orders the data in ascending or descending order as specified. Null values will appear first, followed by any
     * sorted non-null values in <b>natural order</b>. Ties are broken by the next specified  column's values.
     *
     * @param columnNames A list of string labels
     * @param sortingOrderType Specifying ascending or descending order
     * @return A new {@code DraftTable}
     */
    DraftTable orderBy(@NonNull Items<String> columnNames, @NonNull SortingOrderType sortingOrderType);

    /**
     * <p><b>Requires</b>: This method assumes that the provided {@code DraftTable} is non-null and its columns exactly
     *                     match those of the appended {@code DraftTable}. Appending an empty {@code DraftTable} is
     *                     allowed but is inconsequential. </p>
     * <br>
     * <p><b>Guarantees</b>: A new instance of {@code DraftTable} containing the provided data as it's last value(s). </p>
     *
     * @param otherDraftTable A {@code DraftTable} with matching columns
     * @return A new {@code DraftTable}
     */
    DraftTable append(@NonNull DraftTable otherDraftTable);

    /**
     * <p><b>Requires</b>: This method assumes that the provided {@code Row} objects are non-null and have keys exactly
     *                     matching the columns of the appended {@code DraftTable}. </p>
     * <br>
     * <p><b>Guarantees</b>: A new instance of {@code DraftTable} containing the provided data as it's last value(s). </p>
     *
     * @param rows A list of compatible {@code Row} objects
     * @return A new {@code DraftTable}
     */
    DraftTable append(@NonNull Items<Row> rows);

    /**
     * <p><b>Requires</b>: This method assumes that the provided {@code Row} is non-null and its keys exactly match the
     *                     columns of the appended {@code DraftTable}. </p>
     * <br>
     * <p><b>Guarantees</b>: A new instance of {@code DraftTable} containing the provided data as it's last value. </p>
     *
     * @param row Any compatible {@code Row}
     * @return A new {@code DraftTable}
     */
    DraftTable append(@NonNull Row row);

    /**
     * Horizontally appends the new column to the {@code DraftTable}. If the current {@code DraftTable} is completely
     * empty, then the new column will be wrapped into a new {@code DraftTable}.
     *
     * @param newColumn A compatible {@code Column}
     * @return A new {@code DraftTable}
     */
    DraftTable add(@NonNull Column newColumn);

    /**
     * Horizontally appends the new column to the {@code DraftTable}. If the new column is shorter than the
     * {@code DraftTable}, then missing entries will be filled according to the provided fill value. If the current
     * {@code DraftTable} is completely empty, then the new column will be wrapped into a new {@code DraftTable}.
     *
     * @param newColumn A compatible {@code Column}
     * @param fillValue A value compatible with the provided column
     * @return A new {@code DraftTable}
     * @param <T> A type compatible with the column
     */
    <T> DraftTable add(@NonNull Column newColumn, @Nullable T fillValue);

    /**
     * Constructs and horizontally appends a new column to the {@code DraftTable}. If the new column is shorter than the
     * {@code DraftTable}, then missing entries will be filled according to the provided fill value. If the current
     * {@code DraftTable} is completely empty, then the new column will be wrapped into a new {@code DraftTable}.
     *
     * @param newColumnName A string label
     * @param newColumnValues A collection of column values
     * @param fillValue A value compatible with the provided column values
     * @return A new {@code DraftTable}
     * @param <T> A type compatible with the column values
     */
    <T> DraftTable add(@NonNull String newColumnName,
                       @NonNull List<T> newColumnValues,
                       @Nullable T fillValue);

    /**
     * Horizontally appends the new columns to the {@code DraftTable}.
     *
     * @param newColumns A collection of compatible {@code Column} objects
     * @return A new {@code DraftTable}
     */
    DraftTable add(@NonNull Items<Column> newColumns);

    /**
     * Removes the specified column from the {@code DraftTable}. Will produce an empty {@code DraftTable} if no columns
     * are left.
     *
     * @param columnToDrop A string label
     * @return A new {@code DraftTable} subset
     */
    DraftTable drop(@NonNull String columnToDrop);

    /**
     * Removes the specified columns from the {@code DraftTable}. Will produce an empty {@code DraftTable} if no columns
     * are left.
     *
     * @param columnsToDrop Zero of more string labels
     * @return A new {@code DraftTable} subset
     */
    DraftTable drop(@NonNull String... columnsToDrop);

    /**
     * Creates a new column derived from the specified column's mapping under the provided function. The specified
     * column will be left intact and the derived column will be added.
     *
     * @param columnName A string label
     * @param newColumnName A string label
     * @param operationToApply The function to apply to the values of columnName
     * @return A new {@code DraftTable}
     */
    DraftTable deriveFrom(@NonNull String columnName,
                          @NonNull Item<String> newColumnName,
                          @NonNull Function<?, ?> operationToApply);

    /**
     * Creates a new column derived from the two specified columns via a mapping by the provided function. The specified
     * columns will be left intact and the derived column will be added. For example:
     * <pre>{@code
     *         DraftTable df = exampleDraftTable().deriveFrom("dayModifier", "dates", as("modifiedDates"), (Integer daysToAdd, LocalDate date) -> date.plusDays(daysToAdd).getDayOfWeek());
     * }</pre>
     *
     * @param firstColumnName A string label
     * @param secondColumnName A string label
     * @param newColumnName A string label
     * @param operationToApply The function to apply to each pair of values in the columns
     * @return A new {@code DraftTable}
     * @param <X> A type compatible with the first column
     * @param <Y> A type compatible with the second column
     */
    <X, Y> DraftTable deriveFrom(@NonNull String firstColumnName,
                                 @NonNull String secondColumnName,
                                 @NonNull Item<String> newColumnName,
                                 @NonNull BiFunction<X, Y, ?> operationToApply);

    /**
     * Applies the provided consumer to the selected {@code Column}. May mutate underlying state if the {@code Column}
     * contains mutable objects.
     *
     * @param columnName A string label
     * @param consumer The consumer to apply to the selected Column
     * @return A {@code DraftTable}
     */
    DraftTable apply(@NonNull String columnName, @NonNull Consumer<?> consumer);

    /**
     * Attempts to map the current state of the {@code DraftTable} into a new {@code Column} of objects of a designated
     * type. Field names and types of the aggregate must correspond to the column names and column data types of each
     * {@code Column} in the {@code DraftTable}.
     *
     * @param aggregate The target class
     * @param aggregateColumnName A string label
     * @return A new {@code Column}
     * @param <T> The target type
     */
    <T> Column gatherInto(@NonNull Class<T> aggregate, @NonNull Item<String> aggregateColumnName);

    /**
     * Attempts to map the current state of the {@code DraftTable} into a new {@code DraftTable} subset. The selected
     * columns will be mapped into an aggregate object of a designated type. Field names and types of the aggregate must
     * correspond to the column names and column data types of each selected {@code Column} in the {@code DraftTable}.
     *
     * @param aggregate The target class of type T
     * @param aggregateColumnName The name of the new column
     * @param selectColumnNames The name(s) of the target columns
     * @return A new {@code DraftTable} subset
     * @param <T> The target type
     */
    <T> DraftTable gatherInto(@NonNull Class<T> aggregate,
                              @NonNull Item<String> aggregateColumnName,
                              @NonNull Items<String> selectColumnNames);

    /**
     * Attempts to flatten the {@code DraftTable} by splitting a chosen {@code Column} into zero or more derived
     * columns. For example:
     * <pre>{@code
     * public record Coordinate(double lat, double lon) {}
     *
     * // Assuming "coordinates" exists as a column of Coordinate objects
     * someGeospatialDraftTable.split("coordinates")
     *      .intoColumn("lat", Coordinate::lat)
     *      .intoColumn("lon", Coordinate::lon)
     *      .intoColumn("classifier", WatershedClassifier::new)
     *      .thenGather();
     * }</pre>
     * @param columnName Any existing column's name
     * @return A {@code ColumnSplitter} to perform the splitting operation
     */
    ColumnSplitter split(@NonNull String columnName);

    /**
     * Removes the pipeline from further processing by switching control to a {@code DraftTableOutput}. From here,
     * users may export the contents of the {@code DraftTable} to a supported format (CSV, JSON, etc.), pretty print,
     * view the structure, etc.
     *
     * @return A {@code DraftTableOutput}
     */
    DraftTableOutput write();


    /**
     * Produces a count of rows and columns. For example: "10000 rows X 10 columns".
     *
     * @return A String contain the row and column counts
     */
    default String shape() {
        return String.format("%d rows X %d columns", rowCount(), columnCount());
    }

    /**
     * Determines whether the current {@code DraftTable} contains one or more rows
     *
     * @return true or false
     */
    default boolean isEmpty() {
        return rowCount() == 0;
    }

    /**
     * Determines whether the current {@code DraftTable} contains one or more rows and columns
     *
     * @return true or false
     */
    default boolean isCompletelyEmpty() {
        return rowCount() == 0 && columnCount() == 0;
    }

    default DraftTable select(@NonNull Items<String> columns) {
        return select(columns.paramsArray());
    }

    default DraftTable drop(@NonNull Items<String> columnsToDrop) {
        return drop(columnsToDrop.paramsArray());
    }

    /**
     * Retains the specified columns from the {@code DraftTable}. Will produce an empty {@code DraftTable} if no
     * columns are left.
     *
     * @param columnsToKeep Zero or more string labels
     * @return A new {@code DraftTable} subset
     */
    default DraftTable dropAllExcept(@NonNull String... columnsToKeep) {
        return select(columnsToKeep);
    }

    /**
     * Retains the specified columns from the {@code DraftTable}. Will produce an empty {@code DraftTable} if no
     * columns are left.
     *
     * @param columnsToKeep Zero or more string labels as items
     * @return A new {@code DraftTable} subset
     */
    default DraftTable dropAllExcept(@NonNull Items<String> columnsToKeep) {
        return select(columnsToKeep.paramsArray());
    }

    /**
     * Updates the specified column's values based upon the provided function. The new column label must be distinct
     * from originating column.
     *
     * @param columnName A string label
     * @param newColumnName A string label
     * @param operationToApply The function to apply to the values of columnName
     * @return A new {@code DraftTable}
     */
    default DraftTable transform(@NonNull String columnName,
                                 @NonNull Item<String> newColumnName,
                                 @NonNull Function<?, ?> operationToApply)  {
        return deriveFrom(
                columnName, newColumnName, operationToApply
        ).drop(columnName);
    }

    /**
     * Updates the specified column's values based upon the provided function.
     *
     * @param columnName A string label
     * @param operationToApply The function to apply to the values of columnName
     * @return A new {@code DraftTable}
     */
    default DraftTable transform(@NonNull String columnName, @NonNull Function<?, ?> operationToApply)  {
        return drop(columnName).add(
                select(columnName).transform(columnName, operationToApply),
                null
        );
    }

    /**
     * Creates a new column derived from the two specified columns via a mapping by the provided function. The specified
     * columns will be removed and the derived column will be added.
     *
     * @param firstColumnName A string label
     * @param secondColumnName A string label
     * @param newColumnName A string label
     * @param operationToApply The function to apply to each pair of values in the columns
     * @return A new {@code DraftTable}
     * @param <T> A type compatible with the first column
     * @param <R> A type compatible with the second column
     */
    default <T, R> DraftTable melt(@NonNull String firstColumnName,
                                   @NonNull String secondColumnName,
                                   @NonNull Item<String> newColumnName,
                                   @NonNull BiFunction<T, R, ?> operationToApply)  {
        return deriveFrom(
                firstColumnName, secondColumnName, newColumnName, operationToApply
        ).drop(firstColumnName, secondColumnName);
    }

    default <T extends DraftTableOutput> T write(@NonNull Class<T> outputClass) {
        try {
            return outputClass.getDeclaredConstructor(DraftTable.class).newInstance(this);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("An accessible 1-arg constructor taking a DraftTable input was not found.", e);
        }
    }

}
