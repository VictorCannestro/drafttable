package com.cannestro.drafttable.core.implementations.tables;

import com.cannestro.drafttable.core.Column;
import com.cannestro.drafttable.core.DraftTable;
import com.cannestro.drafttable.core.Row;
import com.cannestro.drafttable.core.implementations.rows.HashMapRow;
import com.cannestro.drafttable.core.implementations.columns.FlexibleColumn;
import com.cannestro.drafttable.core.options.Item;
import com.cannestro.drafttable.core.options.Items;
import com.cannestro.drafttable.core.options.SortingOrderType;
import com.google.common.annotations.Beta;
import com.cannestro.drafttable.csv.beans.CsvBean;
import com.cannestro.drafttable.core.outbound.DraftTableOutput;
import com.cannestro.drafttable.utils.ListUtils;
import com.cannestro.drafttable.utils.MapUtils;
import com.cannestro.drafttable.utils.DraftTableUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hamcrest.Matcher;
import org.paumard.streams.StreamsUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;
import java.util.stream.IntStream;

import static com.cannestro.drafttable.core.assumptions.DraftTableAssumptions.*;
import static com.cannestro.drafttable.csv.CsvDataParser.csvBeanBuilder;
import static com.cannestro.drafttable.csv.CsvDataParser.readAllLines;
import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeUniformityOf;
import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeUnique;
import static com.cannestro.drafttable.utils.ListUtils.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;


/**
 * @author Victor Cannestro
 */
@Beta
@EqualsAndHashCode
public class FlexibleDraftTable implements DraftTable {

    @Getter(AccessLevel.PRIVATE) List<Column> listOfColumns;


    private FlexibleDraftTable(List<Column> listOfColumns) {
        this.listOfColumns = listOfColumns;
    }

    /**
     * Static factory method to produce a completely empty {@code DraftTable}.
     *
     * @return A new {@code DraftTable} with no contents
     */
    public static DraftTable emptyDraftTable() {
        return new FlexibleDraftTable(emptyList());
    }

    /**
     * Splits a homogenous list of objects into a new {@code DraftTable} in which each field in a given object is mapped
     * to a corresponding column. Items in the list will be converted into rows in a 1-1 mapping.
     *
     * @param objects A homogeneous list of objects
     * @return A new {@code DraftTable}
     * @param <T> Any arbitrary, non-primitive object
     */
    public static <T> DraftTable fromObjects(@NonNull List<T> objects) {
        return FlexibleDraftTable.fromRows(
                objects.stream()
                        .map(HashMapRow::from)
                        .map(Row.class::cast)
                        .toList()
        );
    }

    /**
     * Stacks the provided columns horizontally into a table-like structure, referred to as a {@code DraftTable}
     *
     * @param columns Any list of {@code Column} objects
     * @return A new {@code DraftTable}
     */
    public static DraftTable fromColumns(@NonNull List<Column> columns) {
        if(columns.isEmpty()) {
            return emptyDraftTable();
        }
        assumeUnique(columns.stream().map(Column::label).toList());
        assumeColumnsHaveUniformSize(columns);
        return new FlexibleDraftTable(columns);
    }

    /**
     * Stacks the provided rows vertically into a table-like structure, referred to as a {@code DraftTable}
     *
     * @param listOfRows Any list of {@code Row} objects
     * @return A new {@code DraftTable}
     */
    public static DraftTable fromRows(@NonNull List<Row> listOfRows) {
        if(listOfRows.isEmpty()) {
            return emptyDraftTable();
        }
        assumeRowsHaveEquivalentKeySet(listOfRows);
        return new FlexibleDraftTable(
                firstElementOf(listOfRows)
                        .keys().stream()
                        .map(name -> new FlexibleColumn(
                                name,
                                listOfRows.stream().map(row -> row.valueOf(name)).toList()
                        )).map(Column.class::cast)
                        .toList()
        );
    }

    /**
     * <p><b>Requires</b>: This method assumes that the inner collection {@code List<?>} represents the <u>COLUMNS</u>. </p>
     * <br>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created </p>
     * <br>
     * @param table A collection of collections of arbitrary, yet homogenous type
     * @param columnNames The column names to associate with the {@code DraftTable}
     * @return A new {@code DraftTable} instance
     */
    public static DraftTable from2DCollectionOfColumnValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table) {
        assumeUnique(columnNames);
        assumeUniformityOf(table);
        return new FlexibleDraftTable(
                StreamsUtils.zip(columnNames.stream(), table.stream(), FlexibleColumn::new)
                        .map(Column.class::cast)
                        .toList()
        );
    }

    /**
     * <p><b>Requires</b>: This method assumes that the inner collection {@code List<?>} represents the <u>ROWS</u>. </p>
     * <br>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created </p>
     * <br>
     * @param table A collection of collections of arbitrary, yet homogenous type
     * @param columnNames The column names to associate with the {@code DraftTable}
     * @return A new {@code DraftTable} instance
     */
    public static DraftTable from2DCollectionOfRowValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table) {
        assumeUnique(columnNames);
        assumeUniformityOf(table);
        return FlexibleDraftTable.fromRows(
                table.stream()
                        .map(rowValues -> MapUtils.zip(columnNames, rowValues))
                        .map(HashMapRow::new)
                        .map(Row.class::cast)
                        .toList()
        );
    }

    /**
     * Entry point for creating a {@code DraftTable} instance. Is a static factory method that will return a new
     * instance upon each call. The columns will be mapped to their representations within {@code csvSchema}</b>.
     *
     * @param filePath A valid Path to the CSV resource file to be read, e.g., {@code "csv/data.csv"}
     * @param csvSchema The {@code CsvBean} type representation of the CSV file located at filePath
     * @return A new {@code DraftTable} instance with {@code String} data
     */
    public static DraftTable fromCSV(@NonNull String filePath, @NonNull Class<? extends CsvBean> csvSchema) {
        return FlexibleDraftTable.fromObjects(csvBeanBuilder(filePath, csvSchema));
    }

    /**
     * Entry point for creating a {@code DraftTable} instance. Is a static factory method that will return a new
     * instance upon each call. The columns will be stored <b>verbatim and in the order they appear</b> in the CSV. The
     * first row of the CSV is required to contain the headers/column names.
     *
     * @param filePath A valid Path to the CSV resource file to be read, e.g., {@code "csv/data.csv"}
     * @return A new {@code DraftTable} instance with {@code String} data
     */
    public static DraftTable fromCSV(@NonNull String filePath) {
        List<List<String>> fullTable = readAllLines(filePath);
        List<String> headers = firstElementOf(fullTable);
        List<List<String>> tableData = fullTable.subList(1, fullTable.size());
        return fromRows(
                IntStream.range(1, tableData.size())
                        .mapToObj(rowIndex -> MapUtils.zip(headers, tableData.get(rowIndex)))
                        .map(HashMapRow::new)
                        .map(Row.class::cast)
                        .toList()
        );
    }

    @Override
    public DraftTableOutput write() {
        return new DraftTableOutput(this);
    }

    @Override
    public int rowCount() {
        if(getListOfColumns().isEmpty()) {
            return 0;
        }
        return firstElementOf(getListOfColumns()).size();
    }

    @Override
    public int columnCount() {
        return getListOfColumns().size();
    }

    @Override
    public List<String> columnNames() {
        return new ArrayList<>(getListOfColumns().stream().map(Column::label).toList());
    }

    @Override
    public DraftTable rename(Items<String> targetColumnNames, Items<String> newColumnNames) {
        return fromColumns(
                columns().stream()
                        .map(column -> {
                            if (targetColumnNames.params().contains(column.label())) {
                                return column.renameAs(newColumnNames.params().get(targetColumnNames.params().indexOf(column.label())));
                            }
                            return column;
                        })
                        .toList()
        );
    }

    @Override
    public boolean hasColumn(String name) {
        return getListOfColumns().stream().anyMatch(column -> column.label().equals(name));
    }

    @Override
    public List<Column> columns() {
        return new ArrayList<>(getListOfColumns());
    }

    @Override
    public List<Row> rows() {
        return IntStream.range(0, rowCount())
                 .mapToObj(rowIndex -> MapUtils.zip(
                             columns().stream().map(Column::label).toList(),
                             columns().stream().map(column -> column.values().get(rowIndex)).toList()
                 ))
                .map(HashMapRow::new)
                .map(Row.class::cast)
                .toList();
    }

    @Override
    public DraftTable copy() {
        return FlexibleDraftTable.fromRows(
                rows().stream().map(Row::deepCopy).toList()
        );
    }

    @Override
    public Column select(@NonNull String columnName) {
        assumeColumnExists(columnName, this);
        return getListOfColumns().get(
                columnNames().indexOf(columnName)
        );
    }

    @Override
    public DraftTable selectMultiple(@NonNull Items<String> columnNames) {
        columnNames.params().forEach(columnName -> assumeColumnExists(columnName, this));
        return new FlexibleDraftTable(
                getListOfColumns().stream()
                        .filter(column -> in(columnNames.params()).matches(column.label()))
                        .toList()
        );
    }

    @Override
    public <T> DraftTable whereColumnType(@NonNull Matcher<Class<T>> classMatcher) {
        return fromColumns(columns().stream()
                .filter(column -> classMatcher.matches(column.dataType()))
                .toList()
        );
    }

    @Override
    public DraftTable whereWithDefault(@NonNull String columnName,
                                       @NonNull Matcher<?> matcher,
                                       @NonNull Matcher<?> defaultMatcher) {
        return conditionalAction(df -> df.where(columnName, matcher).isEmpty(),
                df -> df.where(columnName, defaultMatcher),
                df -> df.where(columnName, matcher)
        );
    }

    @Override
    public <T, R> DraftTable whereWithDefault(@NonNull String columnName,
                                              @NonNull Function<T, R> columnAspect,
                                              @NonNull Matcher<R> matcher,
                                              @NonNull Matcher<R> defaultMatcher) {
        return conditionalAction(df -> df.where(columnName, columnAspect, matcher).isEmpty(),
                df -> df.where(columnName, columnAspect, defaultMatcher),
                df -> df.where(columnName, columnAspect, matcher)
        );
    }

    @Override
    public DraftTable where(@NonNull List<Integer> indices) {
        assumeIndicesBoundedByRowCount(indices, this);
        return FlexibleDraftTable.fromColumns(
                columns().stream().map(column -> column.where(indices)).toList()
        );
    }

    @Override
    public DraftTable where(@NonNull String columnName, @NonNull Matcher<?> matcher) {
        assumeColumnExists(columnName, this);
        List<?> columnValues = select(columnName).values();
        List<Integer> matchingIndices = DraftTableUtils.findMatchingIndices(rowCount(), columnValues::get, matcher);
        return FlexibleDraftTable.fromColumns(
                columns().stream().map(column -> column.where(matchingIndices)).toList()
        );
    }

    @Override
    public <T, R> DraftTable where(@NonNull String columnName, @NonNull Function<T, R> columnAspect, @NonNull Matcher<R> matcher) {
        assumeColumnExists(columnName, this);
        List<T> columnValues = select(columnName).values();
        return where(
                DraftTableUtils.findMatchingIndices(rowCount(), idx -> columnAspect.apply(columnValues.get(idx)), matcher)
        );
    }

    @Override
    public <R> DraftTable where(@NonNull Function<Row, R> rowAspect, @NonNull Matcher<R> matcher) {
        List<Row> row = rows();
        return where(
                DraftTableUtils.findMatchingIndices(rowCount(), idx -> rowAspect.apply(row.get(idx)), matcher)
        );
    }

    @Override
    public <T> DraftTable replaceAll(T target, T replacement) {
        if (columns().stream().noneMatch(column -> column.has(target))) {
            return this;
        }
        return fromColumns(
                columns().stream()
                        .map(column -> column.transform(column.label(), value -> Objects.equals(value, target) ? replacement : value))
                        .toList()
        );
    }

    @Override
    public DraftTable introspect(UnaryOperator<DraftTable> action) {
        return action.apply(this);
    }

    @Override
    public DraftTable conditionalAction(Predicate<DraftTable> conditional,
                                        UnaryOperator<DraftTable> actionIfTrue,
                                        UnaryOperator<DraftTable> actionIfFalse) {
        if (conditional.test(this)) {
            return introspect(actionIfTrue);
        }
        return introspect(actionIfFalse);
    }

    @Override
    public DraftTable top(int nRows) {
        return new FlexibleDraftTable(
                getListOfColumns().stream()
                        .map(column -> column.top(nRows))
                        .toList()
        );
    }

    @Override
    public DraftTable bottom(int nRows) {
        return new FlexibleDraftTable(
                getListOfColumns().stream()
                        .map(column -> column.bottom(nRows))
                        .toList()
        );
    }

    @Override
    public DraftTable randomDraw(int nRows) {
        return where(
                ThreadLocalRandom.current()
                        .ints(0, rowCount())
                        .distinct()
                        .limit(DraftTableUtils.calculateEndpoint(nRows, rowCount()))
                        .boxed()
                        .toList()
        );
    }

    @Override
    public DraftTable orderBy(Comparator<Row> comparator) {
        List<Row> sortedRows = new ArrayList<>(rows());
        sortedRows.sort(comparator);
        return FlexibleDraftTable.fromRows(sortedRows);
    }

    @Override
    public DraftTable orderBy(@NonNull String columnName, SortingOrderType sortingOrderType) {
        assumeColumnExists(columnName, this);
        List<Row> sortedRows = new ArrayList<>(rows());
        Comparator<Row> comparator = Comparator.nullsFirst(Comparator.comparing((Row row) -> row.valueOf(columnName)));
        sortedRows.sort(sortingOrderType.equals(SortingOrderType.ASCENDING) ? comparator : comparator.reversed());
        return FlexibleDraftTable.fromRows(sortedRows);
    }

    @Override
    public DraftTable orderByMultiple(@NonNull Items<String> columnNames, SortingOrderType sortingOrderType) {
        columnNames.params().forEach(columnName -> assumeColumnExists(columnName, this));
        List<Row> sortedRows = new ArrayList<>(rows());
        Comparator<Row> comparator = Comparator.nullsFirst(Comparator.comparing((Row row) -> row.valueOf(firstElementOf(columnNames.params()))));
        for (int i = 1; i < columnNames.params().size(); i++) {
            int finalI = i;
            comparator = Comparator.nullsFirst(comparator.thenComparing(
                    (Row row) -> row.valueOf(columnNames.params().get(finalI))
            ));
        }
        sortedRows.sort(sortingOrderType.equals(SortingOrderType.ASCENDING) ? comparator : comparator.reversed());
        return FlexibleDraftTable.fromRows(sortedRows);
    }

    @Override
    public DraftTable append(@NonNull DraftTable otherDraftTable) {
        if (this.isCompletelyEmpty()) {
            return otherDraftTable;
        }
        if (otherDraftTable.isCompletelyEmpty()) {
            return this;
        }
        assumeColumnNamesAreExactMatchesOf(otherDraftTable.columnNames(), this);
        List<Column> copyOfCurrentState = getListOfColumns();
        return new FlexibleDraftTable(
                copyOfCurrentState.stream()
                        .map(column -> column.append(otherDraftTable.select(column.label())))
                        .toList()
        );
    }

    @Override
    public DraftTable append(@NonNull Items<Row> listOfRows) {
        if (isCompletelyEmpty()) {
            return fromRows(listOfRows.params());
        }
        return append(fromRows(listOfRows.params()));
    }

    @Override
    public DraftTable append(@NonNull Row row) {
        return append(Items.using(row));
    }

    @Override
    public DraftTable addColumn(@NonNull Column newColumn) {
        return addColumn(newColumn, null);
    }

    @Override
    public <T> DraftTable addColumn(@NonNull Column newColumn, T fillValue) {
        if (isCompletelyEmpty()) {
            return FlexibleDraftTable.fromColumns(Collections.singletonList(newColumn));
        }
        return addColumn(newColumn.label(), newColumn.values(), fillValue);
    }

    @Override
    public <T> DraftTable addColumn(@NonNull String newColumnName,
                                    @NonNull List<T> newColumnValues,
                                    T fillValue) {
        if (isCompletelyEmpty()) {
            return FlexibleDraftTable.fromColumns(Collections.singletonList(
                    FlexibleColumn.from(newColumnName, newColumnValues))
            );
        }
        assumeColumnDoesNotExist(newColumnName, this);
        List<Column> updatedListOfColumns = new ArrayList<>(getListOfColumns());
        updatedListOfColumns.add(new FlexibleColumn(
                newColumnName,
                ListUtils.fillToTargetLength(newColumnValues, rowCount(), fillValue)
        ));
        return new FlexibleDraftTable(updatedListOfColumns);
    }

    @Override
    public DraftTable addColumns(@NonNull Items<Column> newColumns) {
        newColumns.params().forEach(newColumn -> assumeColumnDoesNotExist(newColumn.label(), this));
        assumeColumnsHaveCompatibleSize(newColumns.params(), this);
        List<Column> updatedColumnList = new ArrayList<>(getListOfColumns());
        updatedColumnList.addAll(newColumns.params());
        return FlexibleDraftTable.fromColumns(updatedColumnList);
    }

    @Override
    public DraftTable dropColumn(@NonNull String columnToDrop) {
        assumeColumnExists(columnToDrop, this);
        if (columnNames().equals(List.of(columnToDrop))) {
            return emptyDraftTable();
        }
        return new FlexibleDraftTable(
                getListOfColumns().stream()
                        .filter(column -> !column.label().equals(columnToDrop))
                        .toList()
        );
    }

    @Override
    public DraftTable dropColumns(@NonNull Items<String> columnsToDrop) {
        columnsToDrop.params().forEach(columnName -> assumeColumnExists(columnName, this));
        if (columnNames().equals(columnsToDrop.params())) {
            return emptyDraftTable();
        }
        return new FlexibleDraftTable(
                getListOfColumns().stream()
                        .filter(column -> not(in(columnsToDrop.params())).matches(column.label()))
                        .toList()
        );
    }

    @Override
    public DraftTable dropAllExcept(@NonNull Items<String> columnsToKeep) {
        List<String> columnsToDrop = columnNames();
        columnsToDrop.removeIf(columnsToKeep.params()::contains);
        return dropColumns(Items.these(columnsToDrop));
    }

    @Override
    public DraftTable deriveNewColumnFrom(@NonNull String columnName,
                                          @NonNull Item<String> newColumnName,
                                          @NonNull Function<?, ?> operationToApply) {
        return addColumn(
                select(columnName).transform(newColumnName.value(), operationToApply),
                null
        );
    }

    @Override
    public <X, Y> DraftTable deriveNewColumnFrom(@NonNull String firstColumnName,
                                                 @NonNull String secondColumnName,
                                                 @NonNull Item<String> newColumnName,
                                                 @NonNull BiFunction<X, Y, ?> operationToApply) {
        List<?> combinedColumnValues = StreamsUtils.zip(
                ((List<X>) select(firstColumnName).values()).stream(),
                ((List<Y>) select(secondColumnName).values()).stream(),
                operationToApply
        ).toList();
        return addColumn(newColumnName.value(), combinedColumnValues, null);
    }

    @Override
    public DraftTable apply(@NonNull String columnName, @NonNull Consumer<?> consumer) {
        assumeColumnExists(columnName, this);
        getListOfColumns().get(
                 IntStream.range(0, columnCount())
                         .filter(idx -> getListOfColumns().get(idx).label().equals(columnName))
                         .findFirst()
                         .orElseThrow()
        ).apply(consumer);
        return this;
    }

    @Override
    public <T> Column gatherInto(Class<T> aggregate, @NonNull Item<String> aggregateColumnName) {
        return new FlexibleColumn(
                aggregateColumnName.value(),
                rows().stream()
                    .map(row -> row.as(aggregate))
                    .toList()
        );
    }

    @Override
    public <T> DraftTable gatherInto(Class<T> aggregate, @NonNull Item<String> aggregateColumnName, @NonNull Items<String> selectColumnNames) {
        return addColumn(selectMultiple(selectColumnNames).gatherInto(aggregate, aggregateColumnName), null)
                .dropColumns(selectColumnNames);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
