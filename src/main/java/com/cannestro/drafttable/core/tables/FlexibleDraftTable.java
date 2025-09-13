package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.FlexibleColumn;
import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.rows.HashMapRow;
import com.cannestro.drafttable.core.options.Item;
import com.cannestro.drafttable.core.options.Items;
import com.cannestro.drafttable.core.options.SortingOrderType;
import com.google.common.annotations.Beta;
import com.cannestro.drafttable.core.outbound.DraftTableOutput;
import com.cannestro.drafttable.supporting.utils.ListUtils;
import com.cannestro.drafttable.supporting.utils.MapUtils;
import com.cannestro.drafttable.supporting.utils.DraftTableUtils;
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
import static com.cannestro.drafttable.supporting.utils.ListUtils.*;
import static org.hamcrest.Matchers.*;


/**
 * @author Victor Cannestro
 */
@Beta
@EqualsAndHashCode
public class FlexibleDraftTable implements DraftTable {

    @Getter(AccessLevel.PRIVATE) List<Column> listOfColumns;


    FlexibleDraftTable(List<Column> listOfColumns) {
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
        return create().fromColumns(
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
        return create().fromRows(
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
        return create().fromColumns(columns().stream()
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
                columns().stream().map(column -> column.where(indices)).toList()
        );
    }

    @Override
    public DraftTable where(@NonNull String columnName, @NonNull Matcher<?> matcher) {
        assumeColumnExists(columnName, this);
        List<?> columnValues = select(columnName).values();
        List<Integer> matchingIndices = DraftTableUtils.findMatchingIndices(rowCount(), columnValues::get, matcher);
        return create().fromColumns(
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
        return create().fromColumns(
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
        return create().fromRows(sortedRows);
    }

    @Override
    public DraftTable orderBy(@NonNull String columnName, SortingOrderType sortingOrderType) {
        assumeColumnExists(columnName, this);
        List<Row> sortedRows = new ArrayList<>(rows());
        Comparator<Row> comparator = Comparator.nullsFirst(Comparator.comparing((Row row) -> row.valueOf(columnName)));
        sortedRows.sort(sortingOrderType.equals(SortingOrderType.ASCENDING) ? comparator : comparator.reversed());
        return create().fromRows(sortedRows);
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
        return create().fromRows(sortedRows);
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
            return create().fromRows(listOfRows.params());
        }
        return append(create().fromRows(listOfRows.params()));
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
            return create().fromColumns(Collections.singletonList(newColumn));
        }
        return addColumn(newColumn.label(), newColumn.values(), fillValue);
    }

    @Override
    public <T> DraftTable addColumn(@NonNull String newColumnName,
                                    @NonNull List<T> newColumnValues,
                                    T fillValue) {
        if (isCompletelyEmpty()) {
            return create().fromColumns(Collections.singletonList(
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
        return create().fromColumns(updatedColumnList);
    }

    @Override
    public DraftTable dropColumn(@NonNull String columnToDrop) {
        assumeColumnExists(columnToDrop, this);
        if (columnNames().equals(List.of(columnToDrop))) {
            return create().emptyDraftTable();
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
            return create().emptyDraftTable();
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
