package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;


/**
 * @author Victor Cannestro
 */
public class DraftTableHelper {

    private DraftTableHelper(){}

    /**
     * Calculates the allowable slice size. Will return either the desired number of rows
     * or the total row count, whichever is smaller.
     *
     * @param nRows A non-negative integer
     * @param rowCount A non-negative integer
     * @return A non-negative integer within the bounds of rowCount
     */
    public static int calculateEndpoint(int nRows, int rowCount) {
        if (nRows < 0 || rowCount < 0) {
            throw new IllegalArgumentException("Input must be non-negative");
        }
        return Math.min(nRows, rowCount);
    }

    /**
     * Calculates the list of indices within a given range that match a provided condition
     *
     * @param size An integer within [0,n) representing the maximum range boundary
     * @param indexToValueMapping A function to select an arbitrary collection's value based on a given index
     * @param matcher The condition to evaluate against
     * @return A list of integers
     */
    public static List<Integer> findMatchingIndices(int size, @NonNull IntFunction<?> indexToValueMapping, @NonNull Matcher<?> matcher) {
        return IntStream.range(0, size)
                .filter(idx -> matcher.matches(indexToValueMapping.apply(idx)))
                .boxed()
                .toList();
    }

}
