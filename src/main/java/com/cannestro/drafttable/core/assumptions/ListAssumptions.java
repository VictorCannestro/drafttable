package com.cannestro.drafttable.core.assumptions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Victor Cannestro
 */
public final class ListAssumptions {

    private ListAssumptions() {}


    /**
     * <p> <b>Requires</b>: The list reference must not be null </p>
     * <p> <b>Guarantees</b>: That the input list exclusively has unique items, otherwise, and exception will be thrown </p>
     *
     * @param items A list that may contain nulls
     * @param <T> Any homogeneous type
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static <T> void assumeUniquenessOf(@NonNull List<@Nullable T> items) {
        Set<T> uniqueItems = new HashSet<>(items);
        if (items.size() != uniqueItems.size()) {
            throw new IllegalArgumentException(String.format("""
                    Assumption broken - Items must be unique
                    Expected: %d unique items
                    Actual: %d unique items (delta: %d)""",
                    items.size(),
                    uniqueItems.size(),
                    items.size() - uniqueItems.size()
            ));
        }
    }

    /**
     * <p> <b>Requires</b>: The outer and inner list references must not be null. Inner lists may contain null values.
     * Uniformity is only tested in 2D. </p>
     * <p> <b>Guarantees</b>: That the input list exclusively has unique items, otherwise, and exception will be thrown </p>
     *
     * @param collection A list of lists
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static void assumeUniformityOf(@NonNull List<@NonNull List<?>> collection) {
        if (1L != collection.stream().map(List::size).distinct().count()) {
            throw new IllegalArgumentException("Assumption broken - The collection of collections must not be jagged.");
        }
    }

    /**
     * <p> <b>Requires</b>: Each list reference must not be null </p>
     * <p> <b>Guarantees</b>: That the inputs lists have the same size, otherwise, and exception will be thrown </p>
     *
     * @param collection1 A list that may contain nulls
     * @param collection2 A list that may contain nulls
     * @param <K> Any type
     * @param <V> Any type
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static <K, V> void assumeSizesMatch(@NonNull List<@Nullable K> collection1, @NonNull List<@Nullable V> collection2) {
        if (collection1.size() != collection2.size()) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - The size of the collections do not match (delta: %d).",
                    Math.abs(collection1.size() - collection2.size())
            ));
        }
    }

}
