package com.cannestro.drafttable.core.columns;

import com.cannestro.drafttable.core.aggregations.ColumnGrouping;
import com.cannestro.drafttable.core.options.SortingOrderType;
import com.cannestro.drafttable.core.options.StatisticName;
import com.cannestro.drafttable.core.outbound.ColumnOutput;

import org.jspecify.annotations.NonNull;
import org.hamcrest.Matcher;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;


/**
 * @author Victor Cannestro
 */
public interface Column {

    /**
     * <p><b>Guarantees</b>: The {@code Type} information of the underlying column data. </p>
     *
     * @return A {@code Type}
     */
    Type dataType();

    /**
     * <p><b>Guarantees</b>: A copy of the column's current label. </p>
     *
     * @return A String
     */
    String label();

    /**
     * <p> <b>Guarantees</b>: The list of underlying values within the column. </p>
     *
     * @return The underlying column data
     * @param <T> The homogenous type of the value list
     */
    <T> List<T> values();

    /**
     * <p><b>Guarantees</b>: The first value within the underlying values of the column, if it exists. </p>
     *
     * @return The first value of the underlying column data
     * @param <T> Any type
     * @throws IndexOutOfBoundsException if empty
     */
    <T> Supplier<T> firstValue();

    /**
     * <p><b>Guarantees</b>: The last value within the underlying values of the column, if it exists. </p>
     *
     * @return The last value of the underlying column data
     * @param <T> Any type
     * @throws IndexOutOfBoundsException if empty
     */
    <T> Supplier<T>  lastValue();

    /**
     * <p><b>Guarantees</b>: The cardinality of the column. If the column contains more than Integer.MAX_VALUE values,
     * it will return Integer.MAX_VALUE. </p>
     *
     * @return The number of elements in this column
     */
    int size();

    /**
     * <p><b>Guarantees</b>: Queries the current state of the column to determine if it does not contain any values. </p>
     *
     * @return True if and only if the column does not contain any values
     */
    boolean isEmpty();

    /**
     * <p><b>Guarantees</b>: Queries the current state of the column to determine if it contains any null values. </p>
     *
     * @return True if and only if the column contains at least one null
     */
    boolean hasNulls();

    /**
     * <p><b>Guarantees</b>: Queries the current state of the column to determine if it contains the provided value. </p>
     *
     * @return True if and only if the column contains the provided non-null value
     */
    <T> boolean has(@NonNull T element);

    /**
     * <p> Can be used to inspect and access the current state of the pipeline inline without using intermediate
     *     variables. </p>
     *
     * @param action The function to apply
     * @return A new {@code Column}
     */
    Column introspect(@NonNull UnaryOperator<Column> action);

    /**
     * <p> Selects between alternative pipeline paths based upon the provided predicate. </p>
     *
     * @param conditional The predicate that will determine the conditional action taken
     * @param actionIfTrue The function to apply if the conditional evaluates to true
     * @param actionIfFalse The function to apply if the conditional evaluates to false
     * @return A new {@code Column}
     */
    Column conditionalAction(@NonNull Predicate<Column> conditional,
                             @NonNull UnaryOperator<Column> actionIfTrue,
                             @NonNull UnaryOperator<Column> actionIfFalse);

    /**
     * <p> Produces a new {@code Column} by selecting rows, from the top, up to {@code n} or the total row count,
     *     whichever is smaller. </p>
     *
     * @param n A non-negative integer
     * @return A new {@code Column}
     */
    Column top(int n);

    /**
     * <p> Produces a new {@code Column} by selecting rows, from the bottom, up to {@code n} or the total row count,
     *     whichever is smaller. </p>
     *
     * @param n A non-negative integer
     * @return A new {@code Column}
     */
    Column bottom(int n);

    /**
     * <p> Produces a new {@code Column} by selecting entries at random, up to {@code n} or the total row count,
     *     whichever is smaller. </p>
     *
     * @param n A non-negative integer
     * @return A new {@code Column}
     */
    Column randomDraw(int n);

    /**
     * Selects the subset of the {@code Column} that matches the specified selection criteria. It may be empty.
     *
     * @param matcher Any matcher of compatible type
     * @return A new {@code Column} subset
     */
    <T> Column where(@NonNull Matcher<T> matcher);

    /**
     * Selects the subset of the {@code Column} that matches the specified row indices. It may be empty.
     *
     * @param indices A list of non-duplicated integers within [0,n)
     * @return A new {@code Column} subset
     */
    Column where(@NonNull List<Integer> indices);

    /**
     * Selects the subset of the {@code Column} that matches the specified selection criteria. It may be empty. This
     * method can be used to access and match on the fields of a column whose values are of a more complex object type.
     * For example:
     * <pre>{@code
     * Column c = productColumn().where(Product::type, is(ProductTypes.IMPORTED));
     * }</pre>
     *
     * @param aspect A mapping of the column values to some derived aspect
     * @param matcher Any matcher of compatible type
     * @return A new {@code Column} subset
     * @param <T> The type of the column
     * @param <R> The output type of the aspect's mapping
     */
    <T, R>  Column where(@NonNull Function<? super T, ? extends R> aspect, @NonNull Matcher<R> matcher);

    /**
     * Orders the column data in ascending or descending order as specified. Null values will appear first, followed by
     * any sorted non-null values in <b>natural order</b>.
     *
     * @param sortingOrderType Specifying ascending or descending order
     * @return A new {@code Column}
     * @param <T> Type of the underlying column data (often an implementation detail)
     */
    <T> Column orderBy(@NonNull SortingOrderType sortingOrderType);

    /**
     * <p> <b>Requires</b>: Null safe comparator behavior must be specified if relevant. To ensure compilation, a casting
     *                     operation to the underlying data type must be performed. </p>
     * <p> <b>Guarantees</b>: The column data will be ordered according to the specified comparator. </p>
     * <p> Example usage with a {@code FlexibleColumn} implementation:
     * <pre>{@code
     *      Column c = new FlexibleColumn("hireDates", employeeHireDates)
     *                   .orderBy(Comparator.comparing(data -> ((LocalDate) data).getMonth()));
     * }</pre> </p>
     *
     * @param comparator Any compatible comparator
     * @return A new {@code Column}
     * @param <T> Type of the underlying column data
     */
    <T> Column orderBy(@NonNull Comparator<T> comparator);

    /**
     * <p><b>Requires</b>: This method assumes that the provided value is of an arbitrary, yet homogeneous type
     *                     consistent with the current column type. </p>
     * <p><b>Guarantees</b>: A new instance of {@code Column} containing the provided element as it's last  value. </p>
     *
     * @param element An object or primitive
     * @return A new instance of {@code Column}
     * @param <T> An arbitrary type, consistent with the current column type
     */
    <T> Column append(@Nullable T element);

    /**
     * <p><b>Requires</b>: This method assumes that the provided list is non-null and contains values of an arbitrary,
     *                     yet homogeneous type consistent with the current column type. Appending an empty list is
     *                     allowed but is inconsequential. </p>
     * <p><b>Guarantees</b>: A new instance of {@code Column} containing the provided element(s) as it's last
     *                       value(s). </p>
     *
     * @param otherCollection A non-null list of type {@code T}
     * @return A new instance of {@code Column}
     * @param <T> An arbitrary type, consistent with the current column type
     */
    <T> Column append(@NonNull List<T> otherCollection);

    /**
     * <p><b>Requires</b>: This method assumes that the provided column is non-null and contains values of an arbitrary,
     *                     yet homogeneous type consistent with the calling column's type. Appending an empty column is
     *                     allowed but is inconsequential. </p>
     * <p><b>Guarantees</b>: A new instance of {@code Column} containing the provided element(s) as it's last
     *                       value(s). </p>
     *
     * @param otherColumn A column of an arbitrary type, consistent with the calling column's type
     * @return A new instance of {@code Column}
     */
    Column append(@NonNull Column otherColumn);

    /**
     * <p><b>Requires</b>: This method assumes that the provided column contains a mix of zero-or-more null values
     *                     and/or zero-or-more values of an arbitrary, yet homogeneous type consistent with the
     *                     calling column's type. </p>
     * <p><b>Guarantees</b>: A {@code Column} that does not contain null values. It may be empty. It may be the same
     *                       reference. </p>
     *
     * @return An instance of {@code Column}. It may be the same reference.
     */
    Column dropNulls();

    /**
     * <p><b>Requires</b>: This method assumes that the provided column contains a mix of zero-or-more null values
     *                     and/or zero-or-more values of an arbitrary, yet homogeneous type consistent with the
     *                     calling column's type. </p>
     * <p><b>Guarantees</b>: A {@code Column} that does not contain null values. It may be empty. It may be the same
     *                       reference.
     *
     * @param fillValue An object or primitive
     * @return An instance of {@code Column}. It may be the same reference.
     * @param <T> Type of the underlying column data
     */
    <T> Column fillNullsWith(@NonNull T fillValue);

    /**
     * <p> <b>Requires</b>: A {@code Consumer} of compatible type. Users are expected to handle null values, if
     *                      relevant. </p>
     * <p> <b>Guarantees</b>: The {@code Consumer} is applied to <b>every value</b> in the {@code Column}, if there are
     *                        values present. </p>
     * <p> Example usage:
     * <pre>{@code
     *     products().apply(Product::enableBuyOneGetOnePromotion);  // Assuming Product contains the relevant void method
     * }</pre> </p>
     *
     * @param consumer An operation that accepts a single input argument and returns no result. It may produce side effects.
     * @param <T> Type of the underlying column data
     */
    <T> Column apply(@NonNull Consumer<T> consumer);

    /**
     * <p> <b>Guarantees</b>: The current label assigned to the {@code Column} is replaced with the provided label. </p>
     *
     * @param newLabel Any valid String
     * @return An instance of Column. It may be the same reference.
     */
    Column renameAs(@NonNull String newLabel);

    <T, R> Column transform(@NonNull Function<? super T, ? extends R> function);

    <T, R> Column transform(@NonNull String newLabel, @NonNull Function<? super T, ? extends R> function);

    <T> Optional<T> aggregate(@NonNull BinaryOperator<T> accumulator);

    <T> T aggregate(T identity, @NonNull BinaryOperator<T> accumulator);

    <T, R> R aggregate(R identity,
                       @NonNull BiFunction<R, ? super T, R> accumulator,
                       @NonNull BinaryOperator<R> combiner);

    ColumnSplitter split();

    ColumnGrouping group();

    Map<StatisticName, Number> descriptiveStats();

    ColumnOutput write();


    default <T extends ColumnOutput> T write(@NonNull Class<T> outputClass) {
        try {
            return outputClass.getDeclaredConstructor(Column.class).newInstance(this);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("An accessible 1-arg constructor taking a Column input was not found.", e);
        }
    }
}
