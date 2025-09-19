package com.cannestro.drafttable.supporting.utils;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
public class ListUtils {

    private ListUtils() {}

    /**
     * Performs the transpose operation on a 2D collection. Jagged collections are not allowed. Time complexity is
     * O(N^2) so be wary of using with very large collections. For example:
     * <pre>{@code
     *         List<List<Integer>> table = Arrays.asList(
     *                 Arrays.asList(0, 1, 2, 3),
     *                 Arrays.asList(4, 5, 6, 7),
     *                 Arrays.asList(8, 9, 10, 11)
     *         );
     *         Assert.assertEquals(
     *                 ListUtils.transpose(table),
     *                 Arrays.asList(
     *                         Arrays.asList(0, 4, 8),
     *                         Arrays.asList(1, 5, 9),
     *                         Arrays.asList(2, 6, 10),
     *                         Arrays.asList(3, 7, 11)
     *                 )
     *         );
     *
     * }</pre>
     *
     * @param list Any 2D collection
     * @return A new 2D collection in which the rows and column values are swapped
     * @param <T> Any object type
     */
    public static <T> List<List<T>> transpose(@NonNull List<List<T>> list) {
        if (list.isEmpty()) {
            return list;
        }
        if (!list.stream().map(List::size).allMatch(rowSize -> rowSize == firstElementOf(list).size())) {
            throw new IllegalArgumentException("Collections cannot be jagged, they must be of the same length");
        }
        return IntStream.range(0, firstElementOf(list).size())
                .mapToObj(rowIndex -> list.stream()
                        .map(column -> column.get(rowIndex))
                        .toList())
                .toList();
    }

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
    public static <T> List<T> fillToTargetLength(@NonNull List<T> list, int targetLength, T fillValue) {
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

    public static <T> List<T> copyWithoutNulls(@NonNull List<T> list) {
        return list.stream().filter(value -> !isNull(value)).toList();
    }

    /**
     * <p><b>Requires</b>: Both the list and the element are not null</p>
     * <p><b>Guarantees</b>: The first element of the list is returned regardless of parameter type</p>
     *
     * @param list A list containing objects of type T
     * @return The first element of the list, or a thrown exception if the value is null
     * @param <T> Any object type
     * @throws IllegalArgumentException If list is empty
     */
    public static <T> T firstElementOf(@NonNull List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Input list cannot be empty");
        }
        return list.get(0);
    }

    /**
     * <p><b>Requires</b>: Both the array and the element are not null</p>
     * <p><b>Guarantees</b>: The first element of the array is returned regardless of parameter type</p>
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
     * <p><b>Requires</b>: Both the list and the element are not null</p>
     * <p><b>Guarantees</b>: Returns the last element of the input list with the requirement that both the list and the
     *                       element are not null.</p>
     *
     * @param list A list containing objects of type T
     * @return The last element of the list, or a thrown exception if the value is null
     * @param <T> Any object type
     * @throws IllegalArgumentException If list is empty
     */
    public static <T> T lastElementOf(@NonNull List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Input list cannot be empty");
        }
        return list.get(list.size() - 1);
    }

    /**
     * <p><b>Requires</b>: Both the list and the element are not null</p>
     * <p><b>Guarantees</b>: Returns the last element of the input array with the requirement that the array is not
     *                       empty. </p>
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
        return array[array.length-1];
    }

}
