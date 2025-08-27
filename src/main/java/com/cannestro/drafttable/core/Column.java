package com.cannestro.drafttable.core;

import com.cannestro.drafttable.core.options.SortingOrderType;
import com.google.common.annotations.Beta;
import com.cannestro.drafttable.core.options.StatisticName;
import com.cannestro.drafttable.core.outbound.ColumnOutput;
import com.cannestro.drafttable.core.aggregations.ColumnGrouping;
import lombok.NonNull;
import org.hamcrest.Matcher;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;


/**
 * @author Victor Cannestro
 */
@Beta
public interface Column {

    /**
     * <p><b>Guarantees</b>: Queries the current state of the column to determine the type of its values. </p>
     * <br>
     * @return The {@code Type} of the underlying column data
     */
    Type dataType();

    /**
     * <p><b>Guarantees</b>: Queries the current state of the column to determine the column label. </p>
     * <br>
     * @return The column label or name
     */
    String getLabel();

    /**
     * <p><b>Guarantees</b>: The list of underlying values within the current state of the column. </p>
     * <br>
     * @return The values of the underlying column data
     * @param <T> The homogenous type of the value list
     */
    <T> List<T> getValues();

    /**
     * <p><b>Guarantees</b>: The first value within the underlying values of the column, if it exists. </p>
     * <br>
     * @return The first value of the underlying column data
     * @param <T> Any type
     * @throws IndexOutOfBoundsException if empty
     */
    <T> Supplier<T> firstValue();

    /**
     * <p><b>Guarantees</b>: The last value within the underlying values of the column, if it exists. </p>
     * <br>
     * @return The last value of the underlying column data
     * @param <T> Any type
     * @throws IndexOutOfBoundsException if empty
     */
    <T> Supplier<T>  lastValue();

    /**
     * <p><b>Guarantees</b>: The cardinality of the column. If the column contains more than Integer.MAX_VALUE values,
     * it will return Integer.MAX_VALUE. </p>
     * <br>
     * @return The number of elements in this column
     */
    int size();

    /**
     * <p><b>Guarantees</b>: Queries the current state of the column to determine if it does not contain any values. </p>
     * <br>
     * @return True if and only if the column does not contain any values
     */
    boolean isEmpty();

    /**
     * <p><b>Guarantees</b>: Queries the current state of the column to determine if it contains any null values. </p>
     * <br>
     * @return True if and only if the column contains at least one null
     */
    boolean hasNulls();

    /**
     * <p><b>Guarantees</b>: Queries the current state of the column to determine if it contains the provided value. </p>
     * <br>
     * @return True if and only if the column contains the provided non-null value
     */
    <T> boolean has(T element);

    /**
     * <p> Can be used to inspect and access the current state of the pipeline inline without using intermediate
     *     variables. </p>
     * <br>
     * @param action The function to apply
     * @return A new {@code Column}
     */
    Column introspect(UnaryOperator<Column> action);

    /**
     * <p> Selects between alternative pipeline paths based upon the provided predicate. </p>
     *
     * @param conditional The predicate that will determine the conditional action taken
     * @param actionIfTrue The function to apply if the conditional evaluates to true
     * @param actionIfFalse The function to apply if the conditional evaluates to false
     * @return A new {@code Column}
     */
    Column conditionalAction(Predicate<Column> conditional,
                             UnaryOperator<Column> actionIfTrue,
                             UnaryOperator<Column> actionIfFalse);

    /**
     * <p> Produces a new {@code Column} by selecting rows, from the top, up to {@code nRows} or the total row count,
     *     whichever is smaller. </p>
     *
     * @param nRows A non-negative integer
     * @return A new {@code Column}
     */
    Column top(int nRows);

    /**
     * <p> Produces a new {@code Column} by selecting rows, from the bottom, up to {@code nRows} or the total row count,
     *     whichever is smaller. </p>
     *
     * @param nRows A non-negative integer
     * @return A new {@code Column}
     */
    Column bottom(int nRows);

    /**
     * <p> Produces a new {@code Column} by selecting entries at random, up to {@code nRows} or the total row count,
     *     whichever is smaller. </p>
     *
     * @param nRows A non-negative integer
     * @return A new {@code Column}
     */
    Column randomDraw(int nRows);

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
    <T, R>  Column where(@NonNull Function<T, R> aspect, @NonNull Matcher<R> matcher);

    /**
     * Orders the column data in ascending or descending order as specified. Null values will appear first, followed by
     * any sorted non-null values in <b>natural order</b>.
     *
     * @param sortingOrderType Specifying ascending or descending order
     * @return A new {@code Column}
     * @param <T> Type of the underlying column data (often an implementation detail)
     */
    <T> Column orderBy(SortingOrderType sortingOrderType);

    /**
     * <p> <b>Requires</b>: Null safe comparator behavior must be specified if relevant. To ensure compilation, a casting
     *                     operation to the underlying data type must be performed. </p>
     * <br>
     * <p> <b>Guarantees</b>: The column data will be ordered according to the specified comparator. </p>
     * <br>
     * <p> Example usage with a {@code FlexibleColumn} implementation:
     * <pre>{@code
     *      Column c = new FlexibleColumn("hireDates", employeeHireDates)
     *                   .orderBy(Comparator.comparing(data -> ((LocalDate) data).getMonth()));
     * }</pre> </p>
     * <br>
     * @param comparator Any compatible comparator
     * @return A new {@code Column}
     * @param <T> Type of the underlying column data
     */
    <T> Column orderBy(Comparator<T> comparator);

    /**
     * <p><b>Requires</b>: This method assumes that the provided value is of an arbitrary, yet homogeneous type
     *                     consistent with the current column type. </p>
     * <br>
     * <p><b>Guarantees</b>: A new instance of {@code Column} containing the provided element as it's last  value. </p>
     * <br>
     * @param element An object or primitive
     * @return A new instance of {@code Column}
     * @param <T> An arbitrary type, consistent with the current column type
     */
    <T> Column append(T element);

    /**
     * <p><b>Requires</b>: This method assumes that the provided list is non-null and contains values of an arbitrary,
     *                     yet homogeneous type consistent with the current column type. Appending an empty list is
     *                     allowed but is inconsequential. </p>
     * <br>
     * <p><b>Guarantees</b>: A new instance of {@code Column} containing the provided element(s) as it's last
     *                       value(s). </p>
     * <br>
     * @param otherCollection A non-null list of type {@code T}
     * @return A new instance of {@code Column}
     * @param <T> An arbitrary type, consistent with the current column type
     */
    <T> Column append(List<T> otherCollection);

    /**
     * <p><b>Requires</b>: This method assumes that the provided column is non-null and contains values of an arbitrary,
     *                     yet homogeneous type consistent with the calling column's type. Appending an empty column is
     *                     allowed but is inconsequential. </p>
     * <br>
     * <p><b>Guarantees</b>: A new instance of {@code Column} containing the provided element(s) as it's last
     *                       value(s). </p>
     * <br>
     * @param otherColumn A column of an arbitrary type, consistent with the calling column's type
     * @return A new instance of {@code Column}
     */
    Column append(Column otherColumn);

    Column dropNulls();

    <T> Column fillNullsWith(T fillValue);

    <T> void apply(@NonNull Consumer<T> consumer);

    Column rename(String newLabel);

    <T, R> Column transform(@NonNull Function<T, R> function);

    <T, R> Column transform(@NonNull String newLabel, @NonNull Function<T, R> function);

    <T> Optional<T> aggregate(BinaryOperator<T> accumulator);

    <T> T aggregate(T identity, BinaryOperator<T> accumulator);

    <T, R> R aggregate(R identity,
                       BiFunction<R, ? super T, R> accumulator,
                       BinaryOperator<R> combiner);

    ColumnGrouping group();

    Map<StatisticName, Number> descriptiveStats();

    ColumnOutput write();

}
