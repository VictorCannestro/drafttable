package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.options.SortingOrderType;
import com.cannestro.drafttable.core.outbound.ColumnOutput;
import com.cannestro.drafttable.supporting.json.ObjectMapperManager;

import com.cannestro.drafttable.core.options.StatisticName;
import com.cannestro.drafttable.core.outbound.DefaultColumnOutput;
import com.cannestro.drafttable.core.aggregations.FlexibleColumnGrouping;
import com.cannestro.drafttable.supporting.utils.DraftTableHelper;
import com.cannestro.drafttable.supporting.utils.TypeHelper;
import lombok.*;
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
import static com.cannestro.drafttable.supporting.utils.ListHelper.containsMultipleTypes;
import static com.cannestro.drafttable.supporting.utils.ListHelper.copyWithoutNulls;
import static com.cannestro.drafttable.supporting.utils.NullDetector.hasNullIn;
import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@EqualsAndHashCode
public class FlexibleColumn implements Column {

    private final String label;
    private final List<?> values;
    private final JavaType type;

    private static final String EXCEPTION_FORMAT_STRING = "Input type of the provided expression must match the Column data type: %s";


    public FlexibleColumn(final @NonNull String label, final @NonNull List<?> values) {
        List<?> nonNullValues = copyWithoutNulls(values);
        if (!nonNullValues.isEmpty() && containsMultipleTypes(nonNullValues)) {
            throw new IllegalArgumentException("Values cannot be of mixed type");
        }
        this.label = label;
        this.values = new ArrayList<>(values);
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
    public static Column from(final String label, final List<?> values) {
        return new FlexibleColumn(label, values);
    }


    @Override
    public Type dataType() {
        return type.getRawClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> values() {
        return (List<T>) new ArrayList<>(values);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T firstValue() {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("The index is out of range (index < 0 || index >= size()) for size 0");
        }
        return (T) values.get(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lastValue() {
        if (this.isEmpty()) {
            throw new IndexOutOfBoundsException("The index is out of range (index < 0 || index >= size()) for size 0");
        }
        return (T) values.get(size() - 1);
    }

    @Override
    public Column deepCopy() {
        if (TypeHelper.isKnownImmutable(type.getRawClass())) {
            return new FlexibleColumn(label, values);
        }
        try {
            return new FlexibleColumn(
                    label,
                    values.stream().map(value -> isNull(value)
                                    ? null
                                    : ObjectMapperManager.getInstance().defaultMapper().convertValue(value, type))
                            .toList()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(String.format("Internal Object Mapper could not map column of type %s", type), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T valueAt(final int n) {
        if (this.isEmpty() || n < 0 || n >= size()) {
            throw new IndexOutOfBoundsException("The index is out of range (index < 0 || index >= size())");
        }
        return (T) values.get(n);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean hasNulls() {
        return hasNullIn(values);
    }

    @Override
    public <T> boolean has(final @Nullable T element) {
        return values.stream().anyMatch(value -> Objects.equals(value, element));
    }

    @Override
    public <T> Column where(final @NonNull Matcher<T> matcher) {
        return new FlexibleColumn(
                label(),
                values.stream().filter(matcher::matches).toList()
        );
    }

    @Override
    public Column where(final @NonNull List<Integer> indices) {
        return new FlexibleColumn(
                label(),
                indices.stream().map(values::get).toList()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R>  Column where(final @NonNull Function<? super T, ? extends R> aspect, final @NonNull Matcher<R> matcher) {
        List<Integer> matchingIndices = IntStream.range(0, size())
                .filter(idx -> matcher.matches(
                        aspect.apply((T) values.get(idx))
                )).boxed()
                .toList();
        return where(matchingIndices);
    }

    @Override
    public Column introspect(final @NonNull UnaryOperator<Column> action) {
        return action.apply(this);
    }

    @Override
    public Column conditionalAction(final @NonNull Predicate<Column> conditional,
                                    final @NonNull UnaryOperator<Column> actionIfTrue,
                                    final @NonNull UnaryOperator<Column> actionIfFalse) {
        if (conditional.test(this)) {
            return introspect(actionIfTrue);
        }
        return introspect(actionIfFalse);
    }

    @Override
    public Column top(final int n) {
        return new FlexibleColumn(
                label(),
                values.subList(0, DraftTableHelper.calculateEndpoint(n, size()))
        );
    }

    @Override
    public Column bottom(final int n) {
        return new FlexibleColumn(
                label(),
                values.subList(size() - DraftTableHelper.calculateEndpoint(n, size()), size())
        );
    }

    @Override
    public Column randomDraw(final int n) {
        return where(
                ThreadLocalRandom.current()
                        .ints(0, size())
                        .distinct()
                        .limit(DraftTableHelper.calculateEndpoint(n, size()))
                        .boxed()
                        .toList()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Column orderBy(final @NonNull SortingOrderType sortingOrderType) {
        List<T> sortedValues = values();
        Comparator<? super T> comparator = (Comparator<? super T>) Comparator.nullsFirst(Comparator.naturalOrder());
        sortedValues.sort(sortingOrderType.equals(SortingOrderType.ASCENDING)
                ? comparator
                : comparator.reversed()
        );
        return new FlexibleColumn(label(), sortedValues);
    }

    @Override
    public <T> Column orderBy(final @NonNull Comparator<T> comparator) {
        List<T> sortedValues = values();
        sortedValues.sort(comparator);
        return new FlexibleColumn(label(), sortedValues);
    }

    @Override
    public <T> Column append(final @Nullable T element) {
        if (!isEmpty() && !hasNulls() && !isNull(element)) {
            assumeDataTypesMatch(dataType(), element.getClass());
        }
        List<T> newValues = values();
        newValues.add(element);
        return new FlexibleColumn(label(), newValues);
    }

    @Override
    public <T> Column append(final @NonNull List<T> otherCollection) {
        if (!isEmpty() && !hasNulls()) {
            otherCollection.forEach(element -> assumeDataTypesMatch(dataType(), element.getClass()));
        }
        List<T> newValues = values();
        newValues.addAll(otherCollection);
        return new FlexibleColumn(label(), newValues);
    }

    @Override
    public Column append(final @NonNull Column otherColumn) {
        if (!this.hasNulls() && !otherColumn.isEmpty() && !otherColumn.hasNulls()) {
            assumeDataTypesMatch(dataType(), otherColumn.dataType());
        }
        List<?> newValues = values();
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
                values.stream().filter(Objects::nonNull).toList()
        );
    }

    @Override
    public <T> Column fillNullsWith(final @NonNull T fillValue) {
        if (!hasNulls()) {
            return this;
        }
        return new FlexibleColumn(
                label(),
                values.stream().map(value -> isNull(value) ? fillValue : value).toList()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Column apply(final @NonNull Consumer<T> consumer) {
        try {
            values.forEach(value -> consumer.accept((T) value));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
        return this;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public Column renameAs(final @NonNull String newLabel) {
        return new FlexibleColumn(newLabel, values);
    }

    @Override
    public <T, R> Column transform(final @NonNull Function<? super T, ? extends R> function) {
        return transform(label(), function);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> Column transform(final @NonNull String newLabel, final @NonNull Function<? super T, ? extends R> function) {
        try {
            return new FlexibleColumn(
                    newLabel,
                    ((List<T>) values).stream().map(function).toList()
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
    @SuppressWarnings("unchecked")
    public <T> Optional<T> aggregate(final @NonNull BinaryOperator<T> accumulator) {
        try {
            return ((List<T>) values()).stream().reduce(accumulator);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T aggregate(final T identity, final @NonNull BinaryOperator<T> accumulator) {
        try {
            return ((List<T>) values).stream().reduce(identity, accumulator);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXCEPTION_FORMAT_STRING, dataType()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> R aggregate(final R identity,
                              final @NonNull BiFunction<R, ? super T, R> accumulator,
                              final @NonNull BinaryOperator<R> combiner) {
        try {
            return ((List<T>) values).stream().reduce(identity, accumulator, combiner);
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
        if (!Number.class.isAssignableFrom(type.getRawClass())) {
            return Collections.emptyMap();
        }
        final int TWENTY_FIFTH = 25;
        final int FIFTIETH = 25;
        final int SEVENTY_FIFTH = 25;
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        values.forEach(value -> descriptiveStatistics.addValue(Double.parseDouble(value.toString())));
        return Map.of(
                N, descriptiveStatistics.getN(),
                MIN, descriptiveStatistics.getMin(),
                MAX, descriptiveStatistics.getMax(),
                MEAN, descriptiveStatistics.getMean(),
                STANDARD_DEVIATION, descriptiveStatistics.getStandardDeviation(),
                VARIANCE, descriptiveStatistics.getVariance(),
                PERCENTILE_25, descriptiveStatistics.getPercentile(TWENTY_FIFTH),
                PERCENTILE_50, descriptiveStatistics.getPercentile(FIFTIETH),
                PERCENTILE_75, descriptiveStatistics.getPercentile(SEVENTY_FIFTH)
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
