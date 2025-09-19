package com.cannestro.drafttable.supporting.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;

import java.util.List;

/**
 * @author Victor Cannestro
 */
public class JsonUtils {

    private JsonUtils() {}

    public static String prettyJsonString(@NonNull String uglyJsonString) {
        try {
            return ObjectMapperManager.getInstance().defaultMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(ObjectMapperManager.getInstance().defaultMapper().readTree(uglyJsonString));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(String.format("Could not parse input: %s", uglyJsonString), e);
        }
    }

    /**
     * <p><b>Requires</b>: This method assumes that the String representation of each collection item is supplied as a JSON format.</p>
     * <br>
     * <p><b>Guarantees</b>: The {@code objectCollection} has been converted to a list of their corresponding pretty JSON strings.</p>
     * <br>
     * @param objectCollection A homogeneous list
     * @return A collection of Json strings corresponding to each object in {@code objectCollection}
     * @param <T> Any single type
     */
    public static <T> List<String> jsonStringListFrom(List<T> objectCollection) {
        return objectCollection.stream()
                .map(Object::toString)
                .map(JsonUtils::prettyJsonString)
                .toList();
    }

}
