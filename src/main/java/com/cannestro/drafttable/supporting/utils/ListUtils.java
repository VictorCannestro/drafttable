package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
public class ListUtils {

    private ListUtils() {}

    /**
     * Produces a new list padded with the provided fill value. Will throw an exception if the provided list is longer
     * than the target length.
     *
     * @param list Any collection
     * @param targetLength The desired length of the list
     * @param fillValue An object or primitive to pad the list
     * @return A new list of target length, padded with the provided fill value if necessary
     * @param <T> Any type
     */
    public static <T> List<T> fillToTargetLength(@NonNull List<@Nullable T> list, int targetLength, @Nullable T fillValue) {
        if (list.isEmpty()) {
            return Collections.nCopies(targetLength, fillValue);
        }
        if (list.size() > targetLength) {
            throw new IllegalArgumentException(String.format("The length of the provided list must be less than or equal to the non-negative target length: %s", targetLength));
        }
        List<T> paddedList = new ArrayList<>(list);
        if (list.size() < targetLength) {
            int gapSize = targetLength - list.size();
            for (int i = 0; i < gapSize; i++) {
                paddedList.add(fillValue);
            }
        }
        return paddedList;
    }

    public static <T> boolean containsMultipleTypes(@NonNull List<T> list) {
        return 1L != list.stream().map(Object::getClass).distinct().count();
    }

    public static <T> List<@NonNull T> copyWithoutNulls(@NonNull List<@Nullable T> list) {
        return list.stream().filter(value -> !isNull(value)).toList();
    }

    /**
     * <p> <b>Requires</b>: The list must not be empty </p>
     * <p> <b>Guarantees</b>: The first element of the list is returned regardless of parameter type </p>
     *
     * @param list A list containing objects of type T
     * @return The first element of the list
     * @param <T> Any object type
     * @throws IllegalArgumentException If list is empty
     */
    public static <T> T firstElementOf(@NonNull List<@Nullable T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Input list cannot be empty");
        }
        return list.get(0);
    }

    /**
     * <p> <b>Requires</b>: The array must not be empty </p>
     * <p> <b>Guarantees</b>: The first element of the array is returned regardless of parameter type </p>
     *
     * @param array An array containing objects of type T
     * @return The first element of the array
     * @param <T> Any object type
     * @throws IllegalArgumentException If array is empty
     */
    public static <T> T firstElementOf(@NonNull T[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Input array cannot be empty");
        }
        return array[0];
    }

    /**
     * <p> <b>Requires</b>: The list must not be empty </p>
     * <p> <b>Guarantees</b>: The last element of the list will be returned </p>
     *
     * @param list A list containing objects of type T
     * @return The last element of the list
     * @param <T> Any object type
     * @throws IllegalArgumentException If list is empty
     */
    public static <T> T lastElementOf(@NonNull List<@Nullable T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Input list cannot be empty");
        }
        return list.get(list.size() - 1);
    }

    /**
     * <p <b>Requires</b>: The array must not be empty </p>
     * <p> <b>Guarantees</b>: The last element of the input array will be returned </p>
     *
     * @param array An array containing objects of type T
     * @return The last element of the array
     * @param <T> Any object type
     * @throws IllegalArgumentException If array is empty
     */
    public static <T> T lastElementOf(@NonNull T[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Input array cannot be empty");
        }
        return array[array.length - 1];
    }

}
