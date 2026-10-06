package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.ColumnSplitter;
import com.cannestro.drafttable.core.columns.EmbeddedColumnSplitter;
import com.cannestro.drafttable.core.columns.FlexibleColumn;
import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.outbound.DraftTableOutput;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.options.Item;
import com.cannestro.drafttable.core.options.Items;
import com.cannestro.drafttable.core.options.SortingOrderType;

import com.cannestro.drafttable.core.outbound.DefaultDraftTableOutput;
import com.cannestro.drafttable.supporting.utils.ListHelper;
import com.cannestro.drafttable.supporting.utils.DraftTableHelper;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.NonNull;
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
import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeUniquenessOf;
import static com.cannestro.drafttable.supporting.utils.ListHelper.*;
import static org.hamcrest.Matchers.*;


/**
 * @author Victor Cannestro
 */
@EqualsAndHashCode
public class FlexibleDraftTable implements DraftTable {

    private final List<Column> listOfColumns;
    private String tableName;


    FlexibleDraftTable(final String tableName, final List<Column> listOfColumns) {
        this.tableName = tableName;
        this.listOfColumns = listOfColumns;
    }

    public static TableCreator create() {
        return new FlexibleDraftTableCreator();
    }

    @Override
    public DraftTableOutput write() {
        return new DefaultDraftTableOutput(this);
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
    public String tableName() {
        return tableName;
    }

    @Override
    public DraftTable nameTable(final @NonNull String newTableName) {
        this.tableName = newTableName;
        return this;
    }

    @Override
    public List<String> columnNames() {
        return new ArrayList<>(listOfColumns().stream().map(Column::label).toList());
    }

    @Override
    public DraftTable rename(final @NonNull Items<String> targetColumnNames, final @NonNull Items<String> newColumnNames) {
        return create().fromColumns(
                tableName(),
                listOfColumns().stream()
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
    public boolean hasColumn(final @NonNull String name) {
        return listOfColumns().stream().anyMatch(column -> column.label().equals(name));
    }

    @Override
    public List<Column> columns() {
        return new ArrayList<>(listOfColumns());
    }

    @Override
    public Optional<Row> row(final int n) {
        if (isEmpty() || n < 0 || n >= rowCount()) {
            return Optional.empty();
        }
        Map<String, ?> map = new HashMap<>();
        for (Column column: listOfColumns()) {
            map.put(column.label(), column.valueAt(n));
        }
        return Optional.of(new HashMapRow(map));
    }

    @Override
    public List<Row> rows() {
        int length = rowCount();
        List<Row> rowList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            rowList.add(row(i).orElseThrow());
        }
        return rowList;
    }

    @Override
    public DraftTable copy() {
        return new FlexibleDraftTable(
                tableName(),
                listOfColumns().stream().map(Column::deepCopy).toList()
        );
    }

    @Override
    public Column select(final @NonNull String columnName) {
        assumeColumnExists(columnName, this);
        return listOfColumns().get(
                columnNames().indexOf(columnName)
        );
    }

    @Override
    public DraftTable select(final @NonNull String... columnNames) {
        Arrays.stream(columnNames).forEach(columnName -> assumeColumnExists(columnName, this));
        return new FlexibleDraftTable(
                tableName(),
                listOfColumns().stream()
                        .filter(column -> in(columnNames).matches(column.label()))
                        .toList()
        );
    }

    @Override
    public <T> DraftTable whereColumnType(final @NonNull Matcher<Class<T>> classMatcher) {
        return create().fromColumns(
                tableName(),
                columns().stream()
                        .filter(column -> classMatcher.matches(column.dataType()))
                        .toList()
        );
    }

    @Override
    public DraftTable whereWithDefault(final @NonNull String columnName,
                                       final @NonNull Matcher<?> matcher,
                                       final @NonNull Matcher<?> defaultMatcher) {
        return conditionalAction(df -> df.where(columnName, matcher).isEmpty(),
                df -> df.where(columnName, defaultMatcher),
                df -> df.where(columnName, matcher)
        );
    }

    @Override
    public <T, R> DraftTable whereWithDefault(final @NonNull String columnName,
                                              final @NonNull Function<T, R> columnAspect,
                                              final @NonNull Matcher<R> matcher,
                                              final @NonNull Matcher<R> defaultMatcher) {
        return conditionalAction(df -> df.where(columnName, columnAspect, matcher).isEmpty(),
                df -> df.where(columnName, columnAspect, defaultMatcher),
                df -> df.where(columnName, columnAspect, matcher)
        );
    }

    @Override
    public DraftTable where(final @NonNull List<Integer> indices) {
        assumeUniquenessOf(indices);
        assumeIndicesBoundedByRowCount(indices, this);
        return create().fromColumns(
                tableName(),
                columns().stream().map(column -> column.where(indices)).toList()
        );
    }

    @Override
    public DraftTable where(final @NonNull String columnName, final @NonNull Matcher<?> matcher) {
        assumeColumnExists(columnName, this);
        List<?> columnValues = select(columnName).values();
        List<Integer> matchingIndices = DraftTableHelper.findMatchingIndices(rowCount(), columnValues::get, matcher);
        return create().fromColumns(
                tableName(),
                columns().stream().map(column -> column.where(matchingIndices)).toList()
        );
    }

    @Override
    public <T, R> DraftTable where(final @NonNull String columnName,
                                   final @NonNull Function<T, R> columnAspect,
                                   final @NonNull Matcher<R> matcher) {
        assumeColumnExists(columnName, this);
        List<T> columnValues = select(columnName).values();
        return where(
                DraftTableHelper.findMatchingIndices(rowCount(), idx -> columnAspect.apply(columnValues.get(idx)), matcher)
        );
    }

    @Override
    public <R> DraftTable where(final @NonNull Function<Row, R> rowAspect, final @NonNull Matcher<R> matcher) {
        List<Row> row = rows();
        return where(
                DraftTableHelper.findMatchingIndices(rowCount(), idx -> rowAspect.apply(row.get(idx)), matcher)
        );
    }

    @Override
    public <T> DraftTable replaceAll(final @Nullable T target, final @Nullable T replacement) {
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
    public DraftTable introspect(final @NonNull UnaryOperator<DraftTable> action) {
        return action.apply(this);
    }

    @Override
    public DraftTable conditionalAction(final @NonNull Predicate<DraftTable> conditional,
                                        final @NonNull UnaryOperator<DraftTable> actionIfTrue,
                                        final @NonNull UnaryOperator<DraftTable> actionIfFalse) {
        if (conditional.test(this)) {
            return introspect(actionIfTrue);
        }
        return introspect(actionIfFalse);
    }

    @Override
    public DraftTable top(final int nRows) {
        return new FlexibleDraftTable(
                tableName(),
                listOfColumns().stream()
                        .map(column -> column.top(nRows))
                        .toList()
        );
    }

    @Override
    public DraftTable bottom(final int nRows) {
        return new FlexibleDraftTable(
                tableName(),
                listOfColumns().stream()
                        .map(column -> column.bottom(nRows))
                        .toList()
        );
    }

    @Override
    public DraftTable randomDraw(final int nRows) {
        return where(
                ThreadLocalRandom.current()
                        .ints(0, rowCount())
                        .distinct()
                        .limit(DraftTableHelper.calculateEndpoint(nRows, rowCount()))
                        .boxed()
                        .toList()
        );
    }

    @Override
    public DraftTable orderBy(final @NonNull Comparator<Row> comparator) {
        List<Row> sortedRows = new ArrayList<>(rows());
        sortedRows.sort(comparator);
        return create().fromRows(tableName(), sortedRows);
    }

    @Override
    public DraftTable orderBy(final @NonNull String columnName, final @NonNull SortingOrderType sortingOrderType) {
        assumeColumnExists(columnName, this);
        List<Row> sortedRows = new ArrayList<>(rows());
        Comparator<Row> comparator = Comparator.nullsFirst(Comparator.comparing((Row row) -> row.valueOf(columnName)));
        sortedRows.sort(sortingOrderType.equals(SortingOrderType.ASCENDING) ? comparator : comparator.reversed());
        return create().fromRows(tableName(), sortedRows);
    }

    @Override
    public DraftTable orderBy(final @NonNull Items<String> columnNames, final @NonNull SortingOrderType sortingOrderType) {
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
    public DraftTable append(final @NonNull DraftTable otherDraftTable) {
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
    public DraftTable append(final @NonNull Items<Row> listOfRows) {
        if (isCompletelyEmpty()) {
            return create().fromRows(tableName(), listOfRows.params());
        }
        return append(create().fromRows(tableName(), listOfRows.params()));
    }

    @Override
    public DraftTable append(final @NonNull Row row) {
        return append(Items.using(row));
    }

    @Override
    public DraftTable add(final @NonNull Column newColumn) {
        return add(newColumn, null);
    }

    @Override
    public <T> DraftTable add(final @NonNull Column newColumn, @Nullable T fillValue) {
        if (isCompletelyEmpty()) {
            return create().fromColumns(tableName(), Collections.singletonList(newColumn));
        }
        return add(newColumn.label(), newColumn.values(), fillValue);
    }

    @Override
    public <T> DraftTable add(final @NonNull String newColumnName,
                              final @NonNull List<T> newColumnValues,
                              final @Nullable T fillValue) {
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
                ListHelper.fillToTargetLength(newColumnValues, rowCount(), fillValue)
        ));
        return new FlexibleDraftTable(tableName(), updatedListOfColumns);
    }

    @Override
    public DraftTable add(final @NonNull Items<Column> newColumns) {
        newColumns.params().forEach(newColumn -> assumeColumnDoesNotExist(newColumn.label(), this));
        assumeColumnsHaveCompatibleSize(newColumns.params(), this);
        List<Column> updatedColumnList = new ArrayList<>(listOfColumns());
        updatedColumnList.addAll(newColumns.params());
        return create().fromColumns(tableName(), updatedColumnList);
    }

    @Override
    public DraftTable drop(final @NonNull String columnToDrop) {
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
    public DraftTable drop(final @NonNull String... columnsToDrop) {
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
    public DraftTable deriveFrom(final @NonNull String columnName,
                                 final @NonNull Item<String> newColumnName,
                                 final @NonNull Function<?, ?> operationToApply) {
        return add(
                select(columnName).transform(newColumnName.value(), operationToApply),
                null
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> DraftTable deriveFrom(final @NonNull String firstColumnName,
                                        final @NonNull String secondColumnName,
                                        final @NonNull Item<String> newColumnName,
                                        final @NonNull BiFunction<T, R, ?> operationToApply) {
        List<?> combinedColumnValues = StreamsUtils.zip(
                ((List<T>) select(firstColumnName).values()).stream(),
                ((List<R>) select(secondColumnName).values()).stream(),
                operationToApply
        ).toList();
        return add(newColumnName.value(), combinedColumnValues, null);
    }

    @Override
    public DraftTable apply(final @NonNull String columnName, final @NonNull Consumer<?> consumer) {
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
    public <T> Column gatherInto(final @NonNull Class<T> aggregate, final @NonNull Item<String> aggregateColumnName) {
        return new FlexibleColumn(
                aggregateColumnName.value(),
                rows().stream()
                    .map(row -> row.as(aggregate))
                    .toList()
        );
    }

    @Override
    public <T> DraftTable gatherInto(final @NonNull Class<T> aggregate,
                                     final @NonNull Item<String> aggregateColumnName,
                                     final @NonNull Items<String> selectColumnNames) {
        return add(select(selectColumnNames.paramsArray(String[]::new)).gatherInto(aggregate, aggregateColumnName), null).drop(selectColumnNames.paramsArray(String[]::new));
    }

    @Override
    public ColumnSplitter split(final @NonNull String columnName) {
        assumeColumnExists(columnName, this);
        return new EmbeddedColumnSplitter(columnName, this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }


    private List<Column> listOfColumns() {
        return listOfColumns;
    }

}
