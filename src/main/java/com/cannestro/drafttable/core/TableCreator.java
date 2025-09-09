package com.cannestro.drafttable.core;

import com.cannestro.drafttable.csv.beans.CsvBean;
import lombok.NonNull;

import java.util.List;


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
     * @param columns Any list of {@code Column} objects
     * @return A new {@code DraftTable}
     */
    DraftTable fromColumns(@NonNull List<Column> columns);

    /**
     * Stacks the provided rows vertically into a table-like structure, referred to as a {@code DraftTable}
     *
     * @param listOfRows Any list of {@code Row} objects
     * @return A new {@code DraftTable}
     */
    DraftTable fromRows(@NonNull List<Row> listOfRows);

    /**
     * Splits a homogenous list of objects into a new {@code DraftTable} in which each field in a given object is mapped
     * to a corresponding column. Items in the list will be converted into rows in a 1-1 mapping.
     *
     * @param objects A homogeneous list of objects
     * @return A new {@code DraftTable}
     * @param <T> Any arbitrary, non-primitive object
     */
    <T> DraftTable fromObjects(@NonNull List<T> objects);

    /**
     * <p><b>Requires</b>: This method assumes that the inner collection {@code List<?>} represents the <u>ROWS</u>. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created </p>
     *
     * @param table A collection of collections of arbitrary, yet homogenous type
     * @param columnNames The column names to associate with the {@code DraftTable}
     * @return A new {@code DraftTable} instance
     */
    DraftTable fromRowValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table);

    /**
     * <p><b>Requires</b>: This method assumes that the inner collection {@code List<?>} represents the <u>COLUMNS</u>. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created </p>
     *
     * @param table A collection of collections of arbitrary, yet homogenous type
     * @param columnNames The column names to associate with the {@code DraftTable}
     * @return A new {@code DraftTable} instance
     */
    DraftTable fromColumnValues(@NonNull List<String> columnNames, @NonNull List<List<?>> table);

    /**
     * Entry point for creating a {@code DraftTable} instance. Is a static factory method that will return a new
     * instance upon each call. The columns will be stored <b>verbatim and in the order they appear</b> in the CSV. The
     * first row of the CSV is required to contain the headers/column names.
     *
     * @param filePath A valid Path to the CSV resource file to be read, e.g., {@code "csv/data.csv"}
     * @return A new {@code DraftTable} instance with {@code String} data
     */
    DraftTable fromCSV(@NonNull String filePath);

    /**
     * Entry point for creating a {@code DraftTable} instance. Is a static factory method that will return a new
     * instance upon each call. The columns will be mapped to their representations within {@code csvSchema}</b>.
     *
     * @param filePath A valid Path to the CSV resource file to be read, e.g., {@code "csv/data.csv"}
     * @param csvSchema The {@code CsvBean} type representation of the CSV file located at filePath
     * @return A new {@code DraftTable} instance with {@code String} data
     */
    DraftTable fromCSV(@NonNull String filePath, @NonNull Class<? extends CsvBean> csvSchema);

}
