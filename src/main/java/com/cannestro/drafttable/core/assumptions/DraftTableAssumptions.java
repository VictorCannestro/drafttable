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
public class DraftTableAssumptions {

    private DraftTableAssumptions() {}

    public static void assumeDataTypesMatch(Type type, Type otherType) {
        if (!type.equals(otherType)) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - The data types of the source (%s) and receiver (%s) are mismatched.",
                    otherType,
                    type
            ));
        }
    }

    public static void assumeColumnExists(@NonNull String columnName, @NonNull DraftTable draftTable) {
        if (!draftTable.hasColumn(columnName)) {
            throw new IllegalArgumentException("Assumption broken - Column name not recognized: " + columnName);
        }
    }

    public static void assumeColumnDoesNotExist(@NonNull String columnName, @NonNull DraftTable draftTable) {
        if (draftTable.hasColumn(columnName)) {
            throw new IllegalArgumentException("Assumption broken - Column name already exists: " + columnName);
        }
    }

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

    public static <T extends Row> void assumeRowsHaveEquivalentKeySets(@NonNull List<T> listOfRows) {
        long distinctKeyLists =  listOfRows.stream().map(Row::keys).distinct().count();
        if (1 != distinctKeyLists) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - The provided collection of rows must all use the same key set, but contained %s distinct key sets.",
                    distinctKeyLists
            ));
        }
    }

    public static void assumeColumnsHaveCompatibleSize(@NonNull List<Column> listOfColumns, @NonNull DraftTable draftTable) {
        assumeColumnsHaveUniformSize(listOfColumns);
        if (listOfColumns.get(0).size() != draftTable.rowCount()) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - The length of the target columns must be equal to the number of rows in the DraftTable: %s",
                    draftTable.rowCount()
            ));
        }
    }

    public static void assumeColumnsHaveUniformSize(@NonNull List<Column> listOfColumns) {
        if (1 != listOfColumns.stream().map(Column::size).distinct().count()) {
            throw new IllegalArgumentException("Assumption broken - The list of provided columns must not be jagged.");
        }
    }

    public static void assumeIndicesBoundedByRowCount(@NonNull List<Integer> indices, @NonNull DraftTable draftTable) {
        if (indices.stream().anyMatch(idx -> idx < 0 || draftTable.rowCount() <= idx)
                || indices.stream().distinct().toList().size() != indices.size()) {
            throw new IllegalArgumentException("Assumption broken - Indices must be bounded by the row count and not contain duplicates");
        }
    }

}
