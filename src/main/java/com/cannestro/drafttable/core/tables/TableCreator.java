package com.cannestro.drafttable.core.tables;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.inbound.JsonLoader;
import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.rows.Row;
import com.cannestro.drafttable.core.inbound.CsvLoader;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
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
    <T extends Row> DraftTable fromRows(@NonNull String tableName, @NonNull List<T> listOfRows);

    /**
     * Splits a homogenous list of mappable objects into a new {@code DraftTable} in which each field in a given object
     * is mapped to a corresponding column. Items in the list will be converted into rows in a 1-1 mapping.
     *
     * @param tableName Any String
     * @param objects A homogeneous list of objects
     * @return A new {@code DraftTable}
     * @param <T> Any arbitrary, non-primitive object that is {@code Mappable}
     */
    <T extends Mappable> DraftTable fromObjects(@NonNull String tableName, @NonNull List<T> objects);

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

    CsvLoader fromCSV();

    JsonLoader fromJson();

    default <T extends CsvLoader> T fromCSV(Class<T> csvLoaderClass) {
        try {
            return csvLoaderClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("An accessible zero args constructor was not found.", e);
        }
    }

    default DraftTable fromColumns(@NonNull List<Column> columns) {
        return fromColumns(DEFAULT_TABLE_NAME, columns);
    }

    default <T extends Row> DraftTable fromRows(@NonNull List<T> listOfRows) {
        return fromRows(DEFAULT_TABLE_NAME, listOfRows);
    }

    default <T extends Mappable> DraftTable fromObjects(@NonNull List<T> objects) {
        return fromObjects(DEFAULT_TABLE_NAME, objects);
    }

}
