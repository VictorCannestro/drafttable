package com.cannestro.drafttable.core.assumptions;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Victor Cannestro
 */
public class ListAssumptions {

    private ListAssumptions() {}


    public static <T> void assumeUniquenessOf(@NonNull List<@Nullable T> items) {
        Set<T> uniqueItems = new HashSet<>(items);
        if (items.size() != uniqueItems.size()) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - Items must be unique. Expected %d unique items, but received %d unique items (delta: %d)",
                    items.size(),
                    uniqueItems.size(),
                    items.size() - uniqueItems.size()
            ));
        }
    }

    public static void assumeUniformityOf(@NonNull List<@NonNull List<?>> collection) {
        if (1L != collection.stream().map(List::size).distinct().count()) {
            throw new IllegalArgumentException("Assumption broken - The collection of collections must not be jagged.");
        }
    }

    public static <K, V> void assumeSizesMatch(@NonNull List<@NonNull K> collection1, @NonNull List<@Nullable V> collection2) {
        if (collection1.size() != collection2.size()) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - The size of the collections do not match (delta: %d).",
                    Math.abs(collection1.size() - collection2.size())
            ));
        }
    }

}
