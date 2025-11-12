package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.ColumnSplitter;
import com.cannestro.drafttable.core.columns.EmbeddedColumnSplitter;
import com.cannestro.drafttable.core.columns.FlexibleColumn;
import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.options.Item;
import com.cannestro.drafttable.core.options.Items;
import com.cannestro.drafttable.core.options.SortingOrderType;

import com.cannestro.drafttable.core.outbound.DraftTableOutput;
import com.cannestro.drafttable.supporting.utils.ListUtils;
import com.cannestro.drafttable.supporting.utils.MapUtils;
import com.cannestro.drafttable.supporting.utils.DraftTableUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;
import org.paumard.streams.StreamsUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;
import java.util.stream.IntStream;

import static com.cannestro.drafttable.core.assumptions.DraftTableAssumptions.*;
import static com.cannestro.drafttable.supporting.utils.ListUtils.*;
import static org.hamcrest.Matchers.*;


/**
 * @author Victor Cannestro
 */
@Accessors(fluent = true)
@EqualsAndHashCode
public class FlexibleDraftTable implements DraftTable {

    @Getter(AccessLevel.PRIVATE) private final List<Column> listOfColumns;
    @Getter private String tableName;


    FlexibleDraftTable(String tableName, List<Column> listOfColumns) {
        this.tableName = tableName;
        this.listOfColumns = listOfColumns;
    }

    public static TableCreator create() {
        return new FlexibleDraftTableCreator();
    }

    @Override
    public DraftTableOutput write() {
        return new DraftTableOutput(this);
    }

    @Override
    public int rowCount() {
        if(listOfColumns().isEmpty()) {
            return 0;
        }
        return firstElementOf(listOfColumns()).size();
    }

    @Override
    public int columnCount() {
        return listOfColumns().size();
    }

    @Override
    public DraftTable nameTable(@NonNull String newTableName) {
        this.tableName = newTableName;
        return this;
    }

    @Override
    public List<String> columnNames() {
        return new ArrayList<>(listOfColumns().stream().map(Column::label).toList());
    }

    @Override
    public DraftTable rename(@NonNull Items<String> targetColumnNames, @NonNull Items<String> newColumnNames) {
        return create().fromColumns(
                tableName(),
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
    public boolean hasColumn(@NonNull String name) {
        return listOfColumns().stream().anyMatch(column -> column.label().equals(name));
    }

    @Override
    public List<Column> columns() {
        return new ArrayList<>(listOfColumns());
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
        return create().fromRows(
                tableName(),
                rows().stream().map(Row::deepCopy).toList()
        );
    }

    @Override
    public Column select(@NonNull String columnName) {
        assumeColumnExists(columnName, this);
        return listOfColumns().get(
                columnNames().indexOf(columnName)
        );
    }

    @Override
    public DraftTable select(@NonNull String... columnNames) {
        Arrays.stream(columnNames).forEach(columnName -> assumeColumnExists(columnName, this));
        return new FlexibleDraftTable(
                tableName(),
                listOfColumns().stream()
                        .filter(column -> in(columnNames).matches(column.label()))
                        .toList()
        );
    }

    @Override
    public <T> DraftTable whereColumnType(@NonNull Matcher<Class<T>> classMatcher) {
        return create().fromColumns(
                tableName(),
                columns().stream()
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
        return create().fromColumns(
                tableName(),
                columns().stream().map(column -> column.where(indices)).toList()
        );
    }

    @Override
    public DraftTable where(@NonNull String columnName, @NonNull Matcher<?> matcher) {
        assumeColumnExists(columnName, this);
        List<?> columnValues = select(columnName).values();
        List<Integer> matchingIndices = DraftTableUtils.findMatchingIndices(rowCount(), columnValues::get, matcher);
        return create().fromColumns(
                tableName(),
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
    public <T> DraftTable replaceAll(@Nullable T target, @Nullable T replacement) {
        if (columns().stream().noneMatch(column -> column.has(target))) {
            return this;
        }
        return create().fromColumns(
                tableName(),
                columns().stream()
                        .map(column -> column.transform(column.label(), value -> Objects.equals(value, target) ? replacement : value))
                        .toList()
        );
    }

    @Override
    public DraftTable introspect(@NonNull UnaryOperator<DraftTable> action) {
        return action.apply(this);
    }

    @Override
    public DraftTable conditionalAction(@NonNull Predicate<DraftTable> conditional,
                                        @NonNull UnaryOperator<DraftTable> actionIfTrue,
                                        @NonNull UnaryOperator<DraftTable> actionIfFalse) {
        if (conditional.test(this)) {
            return introspect(actionIfTrue);
        }
        return introspect(actionIfFalse);
    }

    @Override
    public DraftTable top(int nRows) {
        return new FlexibleDraftTable(
                tableName(),
                listOfColumns().stream()
                        .map(column -> column.top(nRows))
                        .toList()
        );
    }

    @Override
    public DraftTable bottom(int nRows) {
        return new FlexibleDraftTable(
                tableName(),
                listOfColumns().stream()
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
    public DraftTable orderBy(@NonNull Comparator<Row> comparator) {
        List<Row> sortedRows = new ArrayList<>(rows());
        sortedRows.sort(comparator);
        return create().fromRows(tableName(), sortedRows);
    }

    @Override
    public DraftTable orderBy(@NonNull String columnName, @NonNull SortingOrderType sortingOrderType) {
        assumeColumnExists(columnName, this);
        List<Row> sortedRows = new ArrayList<>(rows());
        Comparator<Row> comparator = Comparator.nullsFirst(Comparator.comparing((Row row) -> row.valueOf(columnName)));
        sortedRows.sort(sortingOrderType.equals(SortingOrderType.ASCENDING) ? comparator : comparator.reversed());
        return create().fromRows(tableName(), sortedRows);
    }

    @Override
    public DraftTable orderBy(@NonNull Items<String> columnNames, @NonNull SortingOrderType sortingOrderType) {
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
        return create().fromRows(tableName(), sortedRows);
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
        List<Column> copyOfCurrentState = listOfColumns();
        return new FlexibleDraftTable(
                tableName(),
                copyOfCurrentState.stream()
                        .map(column -> column.append(otherDraftTable.select(column.label())))
                        .toList()
        );
    }

    @Override
    public DraftTable append(@NonNull Items<Row> listOfRows) {
        if (isCompletelyEmpty()) {
            return create().fromRows(tableName(), listOfRows.params());
        }
        return append(create().fromRows(tableName(), listOfRows.params()));
    }

    @Override
    public DraftTable append(@NonNull Row row) {
        return append(Items.using(row));
    }

    @Override
    public DraftTable add(@NonNull Column newColumn) {
        return add(newColumn, null);
    }

    @Override
    public <T> DraftTable add(@NonNull Column newColumn, @Nullable T fillValue) {
        if (isCompletelyEmpty()) {
            return create().fromColumns(tableName(), Collections.singletonList(newColumn));
        }
        return add(newColumn.label(), newColumn.values(), fillValue);
    }

    @Override
    public <T> DraftTable add(@NonNull String newColumnName,
                              @NonNull List<T> newColumnValues,
                              @Nullable T fillValue) {
        if (isCompletelyEmpty()) {
            return create().fromColumns(
                    tableName(),
                    List.of(FlexibleColumn.from(newColumnName, newColumnValues))
            );
        }
        assumeColumnDoesNotExist(newColumnName, this);
        List<Column> updatedListOfColumns = new ArrayList<>(listOfColumns());
        updatedListOfColumns.add(new FlexibleColumn(
                newColumnName,
                ListUtils.fillToTargetLength(newColumnValues, rowCount(), fillValue)
        ));
        return new FlexibleDraftTable(tableName(), updatedListOfColumns);
    }

    @Override
    public DraftTable add(@NonNull Items<Column> newColumns) {
        newColumns.params().forEach(newColumn -> assumeColumnDoesNotExist(newColumn.label(), this));
        assumeColumnsHaveCompatibleSize(newColumns.params(), this);
        List<Column> updatedColumnList = new ArrayList<>(listOfColumns());
        updatedColumnList.addAll(newColumns.params());
        return create().fromColumns(tableName(), updatedColumnList);
    }

    @Override
    public DraftTable drop(@NonNull String columnToDrop) {
        assumeColumnExists(columnToDrop, this);
        if (columnNames().equals(List.of(columnToDrop))) {
            return create().emptyDraftTable().nameTable(tableName());
        }
        return new FlexibleDraftTable(
                tableName(),
                listOfColumns().stream()
                        .filter(column -> !column.label().equals(columnToDrop))
                        .toList()
        );
    }

    @Override
    public DraftTable drop(@NonNull String... columnsToDrop) {
        Arrays.stream(columnsToDrop).forEach(columnName -> assumeColumnExists(columnName, this));
        if (columnNames().equals(Arrays.asList(columnsToDrop))) {
            return create().emptyDraftTable().nameTable(tableName());
        }
        return new FlexibleDraftTable(
                tableName(),
                listOfColumns().stream()
                        .filter(column -> not(in(columnsToDrop)).matches(column.label()))
                        .toList()
        );
    }

    @Override
    public DraftTable deriveFrom(@NonNull String columnName,
                                 @NonNull Item<String> newColumnName,
                                 @NonNull Function<?, ?> operationToApply) {
        return add(
                select(columnName).transform(newColumnName.value(), operationToApply),
                null
        );
    }

    @Override
    public <T, R> DraftTable deriveFrom(@NonNull String firstColumnName,
                                        @NonNull String secondColumnName,
                                        @NonNull Item<String> newColumnName,
                                        @NonNull BiFunction<T, R, ?> operationToApply) {
        List<?> combinedColumnValues = StreamsUtils.zip(
                ((List<T>) select(firstColumnName).values()).stream(),
                ((List<R>) select(secondColumnName).values()).stream(),
                operationToApply
        ).toList();
        return add(newColumnName.value(), combinedColumnValues, null);
    }

    @Override
    public DraftTable apply(@NonNull String columnName, @NonNull Consumer<?> consumer) {
        assumeColumnExists(columnName, this);
        listOfColumns().get(
                 IntStream.range(0, columnCount())
                         .filter(idx -> listOfColumns().get(idx).label().equals(columnName))
                         .findFirst()
                         .orElseThrow()
        ).apply(consumer);
        return this;
    }

    @Override
    public <T> Column gatherInto(@NonNull Class<T> aggregate, @NonNull Item<String> aggregateColumnName) {
        return new FlexibleColumn(
                aggregateColumnName.value(),
                rows().stream()
                    .map(row -> row.as(aggregate))
                    .toList()
        );
    }

    @Override
    public <T> DraftTable gatherInto(@NonNull Class<T> aggregate, @NonNull Item<String> aggregateColumnName, @NonNull Items<String> selectColumnNames) {
        return add(select(selectColumnNames.paramsArray()).gatherInto(aggregate, aggregateColumnName), null).drop(selectColumnNames.paramsArray());
    }

    @Override
    public ColumnSplitter split(@NonNull String columnName) {
        assumeColumnExists(columnName, this);
        return new EmbeddedColumnSplitter(columnName, this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
