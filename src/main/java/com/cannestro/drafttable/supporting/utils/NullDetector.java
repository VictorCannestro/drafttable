package com.cannestro.drafttable.supporting.utils;

import java.util.List;
import java.util.Objects;


/**
 * @author Victor Cannestro
 */
public final class NullDetector {

    private NullDetector() {}

    /**
     * <p><b>Guarantees</b>: True if the input {@code objectList} contains 1 or more null values, and false otherwise </p>
     *
     * @param objectList Any list of values
     * @return true or false
     */
    public static boolean noNullsIn(List<?> objectList) {
        return objectList.stream().noneMatch(Objects::isNull);
    }

    /**
     * <p><b>Guarantees</b>: True if the input {@code objectList} contains at least 1 null value, and false otherwise </p>
     *
     * @param objectList Any list of values
     * @return true or false
     */
    public static boolean hasNullIn(List<?> objectList) {
        return objectList.stream().anyMatch(Objects::isNull);
    }

}
