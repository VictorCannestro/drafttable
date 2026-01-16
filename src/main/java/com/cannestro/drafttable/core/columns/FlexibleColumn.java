package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.options.SortingOrderType;
import com.cannestro.drafttable.core.outbound.ColumnOutput;
import com.cannestro.drafttable.supporting.json.ObjectMapperManager;

import com.cannestro.drafttable.core.options.StatisticName;
import com.cannestro.drafttable.core.outbound.DefaultColumnOutput;
import com.cannestro.drafttable.core.aggregations.FlexibleColumnGrouping;
import com.cannestro.drafttable.supporting.utils.DraftTableUtils;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;
import java.util.stream.IntStream;

import static com.cannestro.drafttable.core.assumptions.DraftTableAssumptions.assumeDataTypesMatch;
import static com.cannestro.drafttable.core.options.StatisticName.*;
import static com.cannestro.drafttable.supporting.utils.ListUtils.containsMultipleTypes;
import static com.cannestro.drafttable.supporting.utils.ListUtils.copyWithoutNulls;
import static com.cannestro.drafttable.supporting.utils.NullDetector.hasNullIn;
import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
public class FlexibleColumn implements Column {

    private String label;
    private final List<?> values;
    @Getter(AccessLevel.PRIVATE) private final JavaType type;

    private static final String EXCEPTION_FORMAT_STRING = "Input type of the provided expression must match the Column data type: %s";


    public FlexibleColumn(@NonNull String label, @NonNull List<?> values) {
        List<?> nonNullValues = copyWithoutNulls(values);
        if (!nonNullValues.isEmpty() && containsMultipleTypes(nonNullValues)) {
            throw new IllegalArgumentException("Values cannot be of mixed type");
        }
        this.label = label;
        this.values = values;
        if (this.values.isEmpty() || nonNullValues.isEmpty()) {
            this.type = ObjectMapperManager.getInstance().defaultMapper()
                    .getTypeFactory()
                    .constructType(Object.class);
        } else {
            this.type = ObjectMapperManager.getInstance().defaultMapper()
                    .getTypeFactory()
                    .constructType(nonNullValues.get(0).getClass());
        }
    }

    /**
     * <p><b>Requires</b>: This method assumes that the provided values are of a single, arbitrary, yet homogeneous type.
     *                     For example: {@code List<LocalDate>} or {@code List<Product>}.</p>
     * <p><b>Guarantees</b>: A new instance of {@code FlexibleColumn} from the provided input. </p>
     *
     * @param label A non-null string
     * @param values A list of an arbitrary, yet homogeneous type
     * @return A new instance of {@code FlexibleColumn}
     */
    public static Column from(String label, List<?> values) {
        return new FlexibleColumn(label, values);
    }

    @Override
    public Type dataType() {
        return type.getRawClass();
    }

    @Override
    public <T> Supplier<T> firstValue() {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("The index is out of range (index < 0 || index >= size()) for size 0");
        }
        return () -> (T) values().get(0);
    }

    @Override
    public <T> Supplier<T> lastValue() {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("The index is out of range (index < 0 || index >= size()) for size 0");
        }
        return () -> (T) values().get(size() - 1);
    }

    @Override
    public boolean isEmpty() {
        return values().isEmpty();
    }

    @Override
    public int size() {
        return values().size();
    }

    @Override
    public boolean hasNulls() {
        return hasNullIn(values());
    }

    @Override
    public <T> boolean has(@NonNull T element) {
        return values().stream().anyMatch(value -> value.equals(element));
    }

    @Override
    public <T> Column where(@NonNull Matcher<T> matcher) {
        return new FlexibleColumn(
                label(),
                values().stream().filter(matcher::matches).toList()
        );
    }

    @Override
    public Column where(@NonNull List<Integer> indices) {
        return new FlexibleColumn(
                label(),
                indices.stream().map(idx -> values().get(idx)).toList()
        );
    }

    @Override
    public <T, R>  Column where(@NonNull Function<? super T, ? extends R> aspect, @NonNull Matcher<R> matcher) {
        List<Integer> matchingIndices = IntStream.range(0, size())
                .filter(idx -> matcher.matches(
                        aspect.apply((T) values.get(idx))
                )).boxed()
                .toList();
        return where(matchingIndices);
    }

    @Override
    public Column introspect(@NonNull UnaryOperator<Column> action) {
        return action.apply(this);
    }

    @Override
    public Column conditionalAction(@NonNull Predicate<Column> conditional,
                                    @NonNull UnaryOperator<Column> actionIfTrue,
                                    @NonNull UnaryOperator<Column> actionIfFalse) {
        if (conditional.test(this)) {
            return introspect(actionIfTrue);
        }
        return introspect(actionIfFalse);
    }

    @Override
    public Column top(int n) {
        return new FlexibleColumn(
                label(),
                values().subList(0, DraftTableUtils.calculateEndpoint(n, size()))
        );
    }

    @Override
    public Column bottom(int n) {
        return new FlexibleColumn(
                label(),
                values().subList(size() - DraftTableUtils.calculateEndpoint(n, size()), size())
        );
    }

    @Override
    public Column randomDraw(int n) {
        return where(
                ThreadLocalRandom.current()
                        .ints(0, size())
                        .distinct()
                        .limit(DraftTableUtils.calculateEndpoint(n, size()))
                        .boxed()
                        .toList()
        );
    }

    @Override
    public <T> Column orderBy(@NonNull SortingOrderType sortingOrderType) {
        List<T> sortedValues = new ArrayList<>((List<T>) values());
        Comparator<? super T> comparator = (Comparator<? super T>) Comparator.nullsFirst(Comparator.naturalOrder());
        sortedValues.sort(sortingOrderType.equals(SortingOrderType.ASCENDING)
                ? comparator
                : comparator.reversed()
        );
        return new FlexibleColumn(label(), sortedValues);
    }

    @Override
    public <T> Column orderBy(@NonNull Comparator<T> comparator) {
        List<T> sortedValues = new ArrayList<>((List<T>) values());
        sortedValues.sort(comparator);
        return new FlexibleColumn(label(), sortedValues);
    }

    @Override
    public <T> Column append(@Nullable T element) {
        if (!isEmpty() && !hasNulls() && !isNull(element)) {
            assumeDataTypesMatch(dataType(), element.getClass());
        }
        List<T> newValues = (List<T>) new ArrayList<>(values());
        newValues.add(element);
        return new FlexibleColumn(label(), newValues);
    }

    @Override
    public <T> Column append(@NonNull List<T> otherCollection) {
        if (!isEmpty() && !hasNulls()) {
            otherCollection.forEach(element -> assumeDataTypesMatch(dataType(), element.getClass()));
        }
        List<T> newValues = (List<T>) new ArrayList<>(values());
        newValues.addAll(otherCollection);
        return new FlexibleColumn(label(), newValues);
    }

    @Override
    public Column append(@NonNull Column otherColumn) {
        if (!this.hasNulls() && !otherColumn.isEmpty() && !otherColumn.hasNulls()) {
            assumeDataTypesMatch(dataType(), otherColumn.dataType());
        }
        List<?> newValues = new ArrayList<>(values());
        newValues.addAll(otherColumn.values());
        return new FlexibleColumn(label(), newValues);
    }

    @Override
    public Column dropNulls() {
        if (!hasNulls()) {
            return this;
        }
        return new FlexibleColumn(
                label(),
                values().stream().filter(Objects::nonNull).toList()
        );
    }

    @Override
    public <T> Column fillNullsWith(@NonNull T fillValue) {
        if (!hasNulls()) {
            return this;
        }
        return new FlexibleColumn(
                label(),
                values().stream().map(value -> isNull(value) ? fillValue : value).toList()
        );
    }

    @Override
    public <T> Column apply(@NonNull Consumer<T> consumer) {
        try {
            values().forEach(value -> consumer.accept((T) value));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
        return this;
    }

    @Override
    public Column renameAs(@NonNull String newLabel) {
        this.label = newLabel;
        return this;
    }

    @Override
    public <T, R> Column transform(@NonNull Function<? super T, ? extends R> function) {
        return transform(label(), function);
    }

    @Override
    public <T, R> Column transform(@NonNull String newLabel, @NonNull Function<? super T, ? extends R> function) {
        try {
            return new FlexibleColumn(
                    newLabel,
                    ((List<T>) values()).stream().map(function).toList()
            );
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    public FlexibleColumnSplitter split() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot split an empty column.");
        }
        return new FlexibleColumnSplitter(this);
    }

    @Override
    public <T> Optional<T> aggregate(@NonNull BinaryOperator<T> accumulator) {
        try {
            return ((List<T>) values()).stream().reduce(accumulator);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    public <T> T aggregate(T identity, @NonNull BinaryOperator<T> accumulator) {
        try {
            return ((List<T>) values()).stream().reduce(identity, accumulator);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    public <T, R> R aggregate(R identity,
                              @NonNull BiFunction<R, ? super T, R> accumulator,
                              @NonNull BinaryOperator<R> combiner) {
        try {
            return ((List<T>) values()).stream().reduce(identity, accumulator, combiner);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    public FlexibleColumnGrouping group() {
        return new FlexibleColumnGrouping(this);
    }

    @Override
    public Map<StatisticName, Number> descriptiveStats() {
        if (!type().getRawClass().getGenericSuperclass().equals(Number.class)) {
            return Collections.emptyMap();
        }
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        values().forEach(value -> descriptiveStatistics.addValue(Double.parseDouble(value.toString())));
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
        return new DefaultColumnOutput(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
