package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.inbound.HttpLoader;
import com.cannestro.drafttable.core.inbound.JsonLoader;
import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.inbound.CsvLoader;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.net.http.HttpClient;
import java.util.List;

import static com.cannestro.drafttable.core.tables.DraftTable.DEFAULT_TABLE_NAME;


/**
 * @author Victor Cannestro
 */
public interface TableCreator {

    /**
     * <b>Guarantees</b>: A completely empty {@code DraftTable} with be instantiated. Specifically, it will not have any
     * rows or columns.
     *
     * @return A new {@code DraftTable} with no contents
     */
    DraftTable emptyDraftTable();

    /**
     * <p><b>Requires</b>: The collection and its contents must not be null and all {@code Column} objects must have
     * equivalent sizes. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created by stacking each column horizontally.
     * {@code Column} order may differ from the input list. </p>
     *
     * @param tableName Any String
     * @param columns Any list of {@code Column} objects
     * @return A new {@code DraftTable}
     */
    DraftTable fromColumns(@NonNull String tableName, @NonNull List<Column> columns);

    /**
     * <p><b>Requires</b>: The collection and its contents must not be null and all {@code Row} objects must have
     * equivalent key sets. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created by stacking each row vertically </p>
     *
     * @param tableName Any String
     * @param listOfRows Any list of {@code Row} objects
     * @return A new {@code DraftTable}
     * @param <R> Any {@code Row} type
     */
    <R extends Row> DraftTable fromRows(@NonNull String tableName, @NonNull List<R> listOfRows);

    /**
     * <p><b>Requires</b>: The collection and its contents must not be null. Furthermore, each object within must be
     * {@code Mappable}. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created in which each field in a given object
     * is mapped to a corresponding column. Items in the list will be converted into rows in a 1-1 mapping. </p>
     *
     * @param tableName Any String
     * @param objects A homogeneous list of objects
     * @return A new {@code DraftTable}
     * @param <M> Any arbitrary, non-primitive object that is {@code Mappable}
     */
    <M extends Mappable> DraftTable fromObjects(@NonNull String tableName, @NonNull List<M> objects);

    /**
     * <p><b>Requires</b>: The inner collection represents a particular <u>row's</u> values. Value order, with respect to
     *                     column position, must be uniform per collection. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created, zipping the each collection of row
     *                       values with the corresponding column names in a 1-1 mapping. </p>
     *
     * @param table A collection of collections of arbitrary, yet homogenous type
     * @param columnNames The column names to associate with the {@code DraftTable}
     * @return A new {@code DraftTable} instance
     */
    DraftTable fromRowValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table);

    /**
     * <p><b>Requires</b>: The inner collection represents a particular <u>column's</u> values. Collections of column
     *                     values must align positionally with corresponding column names in a 1-1 mapping. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created. Column name order may not be preserved. </p>
     *
     * @param table A collection of collections of arbitrary, yet homogenous type
     * @param columnNames The column names to associate with the {@code DraftTable}
     * @return A new {@code DraftTable} instance
     */
    DraftTable fromColumnValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table);

    /**
     * <p><b>Guarantees</b>: Access to the default implementation of the {@code CsvLoader} API, from which, a
     * {@code DraftTable} may be constructed </p>
     *
     * @return A {@code CsvLoader} object
     */
    CsvLoader fromCsv();

    /**
     * <p><b>Requires</b>: A non-null, concrete {@code HttpClient}. Users are responsible for supplying any necessary
     * authentication, proxy settings, etc. </p>
     * <p><b>Guarantees</b>: Access to the default implementation of the {@code CsvLoader} API, from which, a
     * {@code DraftTable} may be constructed </p>
     *
     * @param client An instantiated {@code HttpClient} to use during loading
     * @return A {@code HttpLoader} object
     */
    HttpLoader fromHttp(@NonNull HttpClient client);

    /**
     * <p><b>Guarantees</b>: Access to the default implementation of the {@code JsonLoader} API, from which, a
     * {@code DraftTable} may be constructed </p>
     *
     * @return A {@code JsonLoader} object
     */
    JsonLoader fromJsonArray();


    /**
     * <p><b>Requires</b>: The collection and its contents must not be null and all {@code Column} objects must have
     * equivalent sizes. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created by stacking each column horizontally.
     * {@code Column} order may differ from the input list. The table will initialize with the default table name. </p>
     *
     * @param columns Any list of {@code Column} objects
     * @return A new {@code DraftTable}
     */
    default DraftTable fromColumns(@NonNull List<Column> columns) {
        return fromColumns(DEFAULT_TABLE_NAME, columns);
    }

    /**
     * <p><b>Requires</b>: The collection and its contents must not be null and all {@code Row} objects must have
     * equivalent key sets. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created by stacking each row vertically. The
     * table will initialize with the default table name. </p>
     *
     * @param listOfRows Any list of {@code Row} objects
     * @return A new {@code DraftTable}
     * @param <R> Any {@code Row} type
     */
    default <R extends Row> DraftTable fromRows(@NonNull List<R> listOfRows) {
        return fromRows(DEFAULT_TABLE_NAME, listOfRows);
    }

    /**
     * <p><b>Requires</b>: The collection and its contents must not be null. Furthermore, each object within must be
     * {@code Mappable}. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created in which each field in a given object
     * is mapped to a corresponding column. Items in the list will be converted into rows in a 1-1 mapping. The table
     * will initialize with the default table name.</p>
     *
     * @param objects A homogeneous list of objects
     * @return A new {@code DraftTable}
     * @param <M> Any arbitrary, non-primitive object that is {@code Mappable}
     */
    default <M extends Mappable> DraftTable fromObjects(@NonNull List<M> objects) {
        return fromObjects(DEFAULT_TABLE_NAME, objects);
    }

    /**
     * <p><b>Requires</b>: The input {@code csvLoaderClass} must implement the {@code CsvLoader} API </p>
     * <p><b>Guarantees</b>: Access to a user supplied implementation of the {@code CsvLoader} API, from which, a
     * {@code DraftTable} may be constructed. Clients may, for example, pass {@code SomeCustomCsvLoader.class}, assuming
     * it abides by the API contract. The library's own {@code DefaultCsvLoader.class} may also be supplied explicitly. </p>
     *
     * @param csvLoaderClass A specified {@code CsvLoader} implementation
     * @return A {@code CsvLoader} object
     * @param <C> Any {@code CsvLoader} type
     */
    default <C extends CsvLoader> C fromCsv(@NonNull Class<C> csvLoaderClass) {
        try {
            return csvLoaderClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("An accessible zero args constructor was not found.", e);
        }
    }

    /**
     * <p><b>Requires</b>: A non-null, concrete {@code HttpClient}. Users are responsible for supplying any necessary
     * authentication, proxy settings, etc. The input {@code httpLoaderClass} must implement the {@code HttpLoader} API. </p>
     * <p><b>Guarantees</b>: Access to a user supplied implementation of the {@code HttpLoader} API, from which, a
     * {@code DraftTable} may be constructed. Clients may, for example, pass {@code SomeCustomHttpLoader.class}, assuming
     * it abides by the API contract. The library's own {@code DefaultHttpLoader.class} may also be supplied explicitly. </p>
     *
     * @param httpLoaderClass A specified {@code HttpLoader} implementation
     * @param client An instantiated {@code HttpClient} to use during loading
     * @return A {@code HttpLoader} object
     * @param <H> Any {@code HttpLoader} type
     */
    default <H extends HttpLoader> H fromHttp(@NonNull Class<H> httpLoaderClass, @NonNull HttpClient client) {
        try {
            return httpLoaderClass.getDeclaredConstructor(HttpClient.class).newInstance(client);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("An accessible 1-arg constructor taking a HttpClient input was not found.", e);
        }
    }

    /**
     * <p><b>Requires</b>: The input {@code jsonLoaderClass} must implement the {@code JsonLoader} API </p>
     * <p><b>Guarantees</b>: Access to a user supplied implementation of the {@code JsonLoader} API, from which, a
     * {@code DraftTable} may be constructed. Clients may, for example, pass {@code SomeCustomJsonLoader.class}, assuming
     * it abides by the API contract. The library's own {@code DefaultJsonLoader.class} may also be supplied explicitly. </p>
     *
     * @param jsonLoaderClass A specified {@code JsonLoader} implementation
     * @return A {@code JsonLoader} object
     * @param <J> Any {@code JsonLoader} type
     */
    default <J extends JsonLoader> J fromJson(@NonNull Class<J> jsonLoaderClass) {
        try {
            return jsonLoaderClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("An accessible zero args constructor was not found.", e);
        }
    }

}
