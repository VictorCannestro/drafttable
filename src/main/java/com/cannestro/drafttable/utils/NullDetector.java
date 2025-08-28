package com.cannestro.drafttable.utils;

import java.util.List;
import java.util.Objects;


/**
 * @author Victor Cannestro
 */
public class NullDetector {

    private NullDetector() {}

    public static boolean noNullsIn(List<?> objectList) {
        return objectList.stream().noneMatch(Objects::isNull);
    }

    public static boolean hasNullIn(List<?> objectList) {
        return objectList.stream().anyMatch(Objects::isNull);
    }

}
