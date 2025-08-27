package com.cannestro.drafttable.core.implementations;

import com.cannestro.drafttable.core.Column;
import com.cannestro.drafttable.core.options.SortingOrderType;
import com.google.common.annotations.Beta;
import com.google.gson.reflect.TypeToken;
import com.cannestro.drafttable.core.options.StatisticName;
import com.cannestro.drafttable.core.outbound.ColumnOutput;
import com.cannestro.drafttable.core.aggregations.ColumnGrouping;
import com.cannestro.drafttable.utils.DraftTableUtils;
import com.cannestro.drafttable.core.assumptions.DataframeAssumptions;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.hamcrest.Matcher;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;

import static com.cannestro.drafttable.core.options.StatisticName.*;
import static com.cannestro.drafttable.utils.ListUtils.firstElementOf;
import static com.cannestro.drafttable.utils.NullDetector.hasNullIn;
import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@Beta
@Getter
@EqualsAndHashCode
public class FlexibleColumn implements Column {

    private final String label;
    private final List<?> values;
    private final TypeToken<?> typeToken;

    private static final String EXCEPTION_FORMAT_STRING = "Input type of the provided expression must match the Column data type: %s";


    public FlexibleColumn(@NonNull String label, @NonNull List<?> values) {
        List<?> nonNullValues = values.stream().filter(value -> !isNull(value)).toList();
        if (!nonNullValues.isEmpty() && 1 != nonNullValues.stream().map(value -> value.getClass().getTypeName()).distinct().count()) {
            throw new IllegalArgumentException("Values cannot be of mixed type");
        }
        this.label = label;
        this.values = values;
        this.typeToken = (this.values.isEmpty() || nonNullValues.isEmpty())
                ? TypeToken.get(Object.class)
                : TypeToken.get(firstElementOf(nonNullValues).getClass());
    }

    /**
     * <p><b>Requires</b>: This method assumes that the provided values are of a single, arbitrary, yet homogeneous type.
     *                     For example: {@code List<LocalDate>} or {@code List<Product>}.</p>
     * <br>
     * <p><b>Guarantees</b>: A new instance of {@code FlexibleColumn} from the provided input. </p>
     * <br>
     * @param label A non-null string
     * @param values A list of an arbitrary, yet homogeneous type
     * @return A new instance of {@code FlexibleColumn}
     */
    public static Column from(String label, List<?> values) {
        return new FlexibleColumn(label, values);
    }

    @Override
    public Type dataType() {
        return getTypeToken().getType();
    }

    @Override
    public <T> Supplier<T> firstValue() {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("The index is out of range (index < 0 || index >= size()) for size 0");
        }
        return () -> (T) getValues().get(0);
    }

    @Override
    public <T> Supplier<T> lastValue() {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("The index is out of range (index < 0 || index >= size()) for size 0");
        }
        return () -> (T) getValues().get(size() - 1);
    }

    @Override
    public boolean isEmpty() {
        return getValues().isEmpty();
    }

    @Override
    public int size() {
        return getValues().size();
    }

    @Override
    public boolean hasNulls() {
        return hasNullIn(getValues());
    }

    @Override
    public <T> boolean has(@NonNull T element) {
        return getValues().stream().anyMatch(value -> value.equals(element));
    }

    @Override
    public <T> Column where(@NonNull Matcher<T> matcher) {
        return new FlexibleColumn(
                getLabel(),
                getValues().stream().filter(matcher::matches).toList()
        );
    }

    @Override
    public Column where(@NonNull List<Integer> indices) {
        return new FlexibleColumn(
                getLabel(),
                indices.stream().map(idx -> getValues().get(idx)).toList()
        );
    }

    @Override
    public <T, R>  Column where(@NonNull Function<T, R> aspect, @NonNull Matcher<R> matcher) {
        List<Integer> matchingIndices = IntStream.range(0, size())
                .filter(idx -> matcher.matches(
                        aspect.apply((T) values.get(idx))
                )).boxed()
                .toList();
        return where(matchingIndices);
    }

    @Override
    public Column introspect(UnaryOperator<Column> action) {
        return action.apply(this);
    }

    @Override
    public Column conditionalAction(Predicate<Column> conditional,
                                    UnaryOperator<Column> actionIfTrue,
                                    UnaryOperator<Column> actionIfFalse) {
        if (conditional.test(this)) {
            return introspect(actionIfTrue);
        }
        return introspect(actionIfFalse);
    }

    @Override
    public Column top(int nRows) {
        return new FlexibleColumn(
                getLabel(),
                getValues().subList(0, DraftTableUtils.calculateEndpoint(nRows, size()))
        );
    }

    @Override
    public Column bottom(int nRows) {
        return new FlexibleColumn(
                getLabel(),
                getValues().subList(size() - DraftTableUtils.calculateEndpoint(nRows, size()), size())
        );
    }

    @Override
    public Column randomDraw(int nRows) {
        List<?> sortedRows = new ArrayList<>(getValues());
        Collections.shuffle(sortedRows);
        return new FlexibleColumn(
                getLabel(),
                sortedRows.subList(0, DraftTableUtils.calculateEndpoint(nRows, size()))
        );
    }

    @Override
    public <T> Column orderBy(SortingOrderType sortingOrderType) {
        List<T> sortedValues = new ArrayList<>((List<T>) getValues());
        Comparator<? super T> comparator = (Comparator<? super T>) Comparator.nullsFirst(Comparator.naturalOrder());
        sortedValues.sort(sortingOrderType.equals(SortingOrderType.ASCENDING)
                ? comparator
                : comparator.reversed()
        );
        return new FlexibleColumn(getLabel(), sortedValues);
    }

    @Override
    public <T> Column orderBy(Comparator<T> comparator) {
        List<T> sortedValues = new ArrayList<>((List<T>) getValues());
        sortedValues.sort(comparator);
        return new FlexibleColumn(getLabel(), sortedValues);
    }

    @Override
    public <T> Column append(T element) {
        if (!isEmpty() && !hasNulls() && !isNull(element)) {
            DataframeAssumptions.assumeDataTypesMatch(dataType(), element.getClass());
        }
        List<T> newValues = (List<T>) new ArrayList<>(getValues());
        newValues.add(element);
        return new FlexibleColumn(getLabel(), newValues);
    }

    @Override
    public <T> Column append(List<T> otherCollection) {
        if (!isEmpty() && !hasNulls()) {
            otherCollection.forEach(element -> DataframeAssumptions.assumeDataTypesMatch(dataType(), element.getClass()));
        }
        List<T> newValues = (List<T>) new ArrayList<>(getValues());
        newValues.addAll(otherCollection);
        return new FlexibleColumn(getLabel(), newValues);
    }

    @Override
    public Column append(Column otherColumn) {
        if (!this.hasNulls() && !otherColumn.isEmpty() && !otherColumn.hasNulls()) {
            DataframeAssumptions.assumeDataTypesMatch(dataType(), otherColumn.dataType());
        }
        List<?> newValues = new ArrayList<>(getValues());
        newValues.addAll(otherColumn.getValues());
        return new FlexibleColumn(getLabel(), newValues);
    }

    @Override
    public Column dropNulls() {
        return new FlexibleColumn(
                getLabel(),
                getValues().stream().filter(Objects::nonNull).toList()
        );
    }

    @Override
    public <T> Column fillNullsWith(T fillValue) {
        if (!hasNulls()) {
            DataframeAssumptions.assumeDataTypesMatch(dataType(), fillValue.getClass());
        }
        return new FlexibleColumn(
                getLabel(),
                getValues().stream().map(value -> {
                    if (isNull(value)) {
                        return fillValue;
                    }
                    return value;
                }).toList()
        );
    }

    @Override
    public <T> void apply(@NonNull Consumer<T> consumer) {
        try {
            getValues().forEach(value -> consumer.accept((T) value));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    public Column rename(@NonNull String newLabel) {
        return new FlexibleColumn(newLabel, getValues());
    }

    @Override
    public <T, R> Column transform(@NonNull Function<T, R> function) {
        return transform(getLabel(), function);
    }

    @Override
    public <T, R> Column transform(@NonNull String newLabel, @NonNull Function<T, R> function) {
        try {
            return new FlexibleColumn(
                    newLabel,
                    ((List<T>) getValues()).stream()
                            .map(function)
                            .toList()
            );
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    public <T> Optional<T> aggregate(BinaryOperator<T> accumulator) {
        try {
            return ((List<T>) getValues()).stream().reduce(accumulator);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    public <T> T aggregate(T identity, BinaryOperator<T> accumulator) {
        try {
            return ((List<T>) getValues()).stream().reduce(identity, accumulator);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    public <T, R> R aggregate(R identity,
                              BiFunction<R, ? super T, R> accumulator,
                              BinaryOperator<R> combiner) {
        try {
            return ((List<T>) getValues()).stream().reduce(identity, accumulator, combiner);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    public ColumnGrouping group() {
        return new ColumnGrouping(this);
    }

    @Override
    public Map<StatisticName, Number> descriptiveStats() {
        if (!getTypeToken().getRawType().getSuperclass().equals(Number.class)) {
            return Collections.emptyMap();
        }
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        getValues().forEach(value -> descriptiveStatistics.addValue(Double.parseDouble(value.toString())));
        return Map.of(
                N, descriptiveStatistics.getN(),
                MIN, descriptiveStatistics.getMin(),
                MAX, descriptiveStatistics.getMax(),
                MEAN, descriptiveStatistics.getMean(),
                STANDARD_DEVIATION, descriptiveStatistics.getStandardDeviation(),
                VARIANCE, descriptiveStatistics.getVariance(),
                PERCENTILE_25, descriptiveStatistics.getPercentile(25),
                PERCENTILE_50, descriptiveStatistics.getPercentile(50),
                PERCENTILE_75, descriptiveStatistics.getPercentile(75)
        );
    }

    @Override
    public ColumnOutput write() {
        return new ColumnOutput(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
