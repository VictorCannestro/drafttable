package com.cannestro.drafttable.core.assumptions;

import java.util.HashSet;
import java.util.List;


/**
 * @author Victor Cannestro
 */
public class ListAssumptions {

    private ListAssumptions() {}


    public static <T> void assumeUnique(List<T> items) {
        if (items.size() != new HashSet<>(items).size()) {
            throw new IllegalArgumentException("Assumption broken--items must be unique");
        }
    }

    public static void assumeUniformityOf(List<List<?>> collection) {
        if (1 != collection.stream().map(List::size).distinct().count()) {
            throw new IllegalArgumentException("Assumption broken: The collection of collections must not be jagged");
        }
    }

}
