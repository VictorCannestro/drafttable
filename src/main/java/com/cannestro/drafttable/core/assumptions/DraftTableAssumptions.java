package com.cannestro.drafttable.core.assumptions;

import com.cannestro.drafttable.core.columns.Column;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.rows.Row;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;


/**
 * @author Victor Cannestro
 */
public final class DraftTableAssumptions {

    private DraftTableAssumptions() {}

    /**
     * <p> <b>Requires</b>: Each type reference must not be null </p>
     * <p> <b>Guarantees</b>: That the inputs types are equal, otherwise, an exception will be thrown </p>
     *
     * @param type Any type
     * @param otherType Any type
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static void assumeDataTypesMatch(@NonNull Type type, @NonNull Type otherType) {
        if (!type.equals(otherType)) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - The data types of the source (%s) and receiver (%s) are mismatched.",
                    otherType,
                    type
            ));
        }
    }

    /**
     * <p> <b>Requires</b>: Each type reference must not be null </p>
     * <p> <b>Guarantees</b>: The input {@code DraftTable} contains a column name matching {@code columnName} exactly,
     * otherwise, an exception will be thrown </p>
     *
     * @param columnName Any valid column name
     * @param draftTable The target of the assumption validation
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static void assumeColumnExists(@NonNull String columnName, @NonNull DraftTable draftTable) {
        if (!draftTable.hasColumn(columnName)) {
            throw new IllegalArgumentException("Assumption broken - Column name not recognized: " + columnName);
        }
    }

    /**
     * <p> <b>Requires</b>: Each type reference must not be null </p>
     * <p> <b>Guarantees</b>: The input {@code DraftTable} does not contain a column name matching {@code columnName}
     * exactly, otherwise, an exception will be thrown </p>
     *
     * @param columnName Any valid column name
     * @param draftTable The target of the assumption validation
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static void assumeColumnDoesNotExist(@NonNull String columnName, @NonNull DraftTable draftTable) {
        if (draftTable.hasColumn(columnName)) {
            throw new IllegalArgumentException("Assumption broken - Column name already exists: " + columnName);
        }
    }

    /**
     * <p> <b>Requires</b>: Each type reference must not be null </p>
     * <p> <b>Guarantees</b>: The input {@code DraftTable} has every column name in {@code listOfColumnNames}
     * irrespective of order and has the name number of column names, otherwise, an exception will be thrown </p>
     *
     * @param listOfColumnNames A list of valid column names
     * @param draftTable The target of the assumption validation
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static void assumeColumnNamesAreExactMatchesOf(@NonNull List<String> listOfColumnNames, @NonNull DraftTable draftTable) {
        if (draftTable.columnNames().size() != listOfColumnNames.size()
            || !new HashSet<>(draftTable.columnNames()).equals(new HashSet<>(listOfColumnNames))) {
            throw new IllegalArgumentException(String.format("""
                Assumption broken - Column names of input table must match those provided:
                Provided: %s
                Actual: %s
                """, listOfColumnNames, draftTable.columnNames()
            ));
        }
    }

    /**
     * <p> <b>Requires</b>: The input collection and each {@code Row} reference must not be null </p>
     * <p> <b>Guarantees</b>: Each {@code Row} in the collection has the same key set, otherwise, an exception will be
     * thrown </p>
     *
     * @param listOfRows A collection of {@code Row} objects
     * @param <T> Any {@code Row}
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static <T extends Row> void assumeRowsHaveEquivalentKeySets(@NonNull List<T> listOfRows) {
        long distinctKeyLists =  listOfRows.stream()
                .map(Row::keys)
                .distinct()
                .count();
        if (1 != distinctKeyLists) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - The provided collection of rows must all use the same key set, " +
                                        "but contained %s distinct key sets.",
                    distinctKeyLists
            ));
        }
    }

    /**
     * <p> <b>Requires</b>: The input collection and each {@code Row} reference must not be null </p>
     * <p> <b>Guarantees</b>: Each {@code Column} in the collection has the same size and that size matches the row
     * count in the target {@code DraftTable}, otherwise, an exception will be thrown </p>
     *
     * @param listOfColumns A collection of {@code Column} objects
     * @param draftTable The target of the assumption validation
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static void assumeColumnsHaveCompatibleSize(@NonNull List<@NonNull Column> listOfColumns, @NonNull DraftTable draftTable) {
        assumeColumnsHaveUniformSize(listOfColumns);
        if (listOfColumns.get(0).size() != draftTable.rowCount()) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - The length of the target columns must be equal to the number of rows in the DraftTable: %s",
                    draftTable.rowCount()
            ));
        }
    }

    /**
     * <p> <b>Requires</b>: The collection and each {@code Column} reference must not be null </p>
     * <p> <b>Guarantees</b>: Each {@code Column} in the collection has the same size, otherwise, an exception will be
     * thrown </p>
     *
     * @param listOfColumns A collection of {@code Column} objects
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static void assumeColumnsHaveUniformSize(@NonNull List<@NonNull Column> listOfColumns) {
        if (1 != listOfColumns.stream().map(Column::size).distinct().count()) {
            throw new IllegalArgumentException("Assumption broken - The list of provided columns must not be jagged.");
        }
    }

    /**
     * <p> <b>Requires</b>: The input collection, it's elements, and the {@code DraftTable} reference must not be null </p>
     * <p> <b>Guarantees</b>: Each element in the input collection is a valid row index in the target {@code DraftTable},
     * otherwise, an exception will be thrown. </p>
     *
     * @param indices A list of indices to query {@code draftTable}
     * @param draftTable The target of the assumption validation
     * @throws IllegalArgumentException when guarantee cannot be made
     * @apiNote Clients are responsible for handling any duplicate values within the input collection
     */
    public static void assumeIndicesBoundedByRowCount(@NonNull List<@NonNull Integer> indices, @NonNull DraftTable draftTable) {
        if (indices.stream().anyMatch(idx -> idx < 0 || draftTable.rowCount() <= idx)) {
            throw new IllegalArgumentException("Assumption broken - Indices must be bounded by the row count");
        }
    }

}
