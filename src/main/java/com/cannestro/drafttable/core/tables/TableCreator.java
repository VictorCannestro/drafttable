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


public interface TableCreator {

    /**
     * Instantiates a completely empty {@code DraftTable}.
     *
     * @return A new {@code DraftTable} with no contents
     */
    DraftTable emptyDraftTable();

    /**
     * Stacks the provided columns horizontally into a table-like structure, referred to as a {@code DraftTable}
     *
     * @param tableName Any String
     * @param columns Any list of {@code Column} objects
     * @return A new {@code DraftTable}
     */
    DraftTable fromColumns(@NonNull String tableName, @NonNull List<Column> columns);

    /**
     * Stacks the provided rows vertically into a table-like structure, referred to as a {@code DraftTable}
     *
     * @param tableName Any String
     * @param listOfRows Any list of {@code Row} objects
     * @return A new {@code DraftTable}
     */
    <R extends Row> DraftTable fromRows(@NonNull String tableName, @NonNull List<R> listOfRows);

    /**
     * Splits a homogenous list of mappable objects into a new {@code DraftTable} in which each field in a given object
     * is mapped to a corresponding column. Items in the list will be converted into rows in a 1-1 mapping.
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

    CsvLoader fromCsv();

    HttpLoader fromHttp(@NonNull HttpClient client);

    JsonLoader fromJsonArray();


    default DraftTable fromColumns(@NonNull List<Column> columns) {
        return fromColumns(DEFAULT_TABLE_NAME, columns);
    }

    default <R extends Row> DraftTable fromRows(@NonNull List<R> listOfRows) {
        return fromRows(DEFAULT_TABLE_NAME, listOfRows);
    }

    default <M extends Mappable> DraftTable fromObjects(@NonNull List<M> objects) {
        return fromObjects(DEFAULT_TABLE_NAME, objects);
    }

    default <C extends CsvLoader> C fromCsv(@NonNull Class<C> csvLoaderClass) {
        try {
            return csvLoaderClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("An accessible zero args constructor was not found.", e);
        }
    }

    default <H extends HttpLoader> H fromHttp(@NonNull Class<H> httpLoaderClass, @NonNull HttpClient client) {
        try {
            return httpLoaderClass.getDeclaredConstructor(HttpClient.class).newInstance(client);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("An accessible 1-arg constructor taking a HttpClient input was not found.", e);
        }
    }

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
