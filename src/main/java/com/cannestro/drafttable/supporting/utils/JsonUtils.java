package com.cannestro.drafttable.supporting.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * @author Victor Cannestro
 */
public class JsonUtils {

    private JsonUtils() {}

    public static String makePretty(@NonNull String uglyJsonString) {
        try {
            return ObjectMapperManager.getInstance().defaultMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(ObjectMapperManager.getInstance().defaultMapper().readTree(uglyJsonString));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(String.format("Could not parse input: %s", uglyJsonString), e);
        }
    }

    /**
     * <p><b>Requires</b>: The objects within the provided collection are able to be mapped to JSON.</p>
     * <p><b>Guarantees</b>: The {@code objectCollection} has been converted to a list of their corresponding pretty JSON strings.</p>
     *
     * @param objectCollection A homogeneous list
     * @return A collection of Json strings corresponding to each object in {@code objectCollection}
     * @param <T> Any single type
     */
    public static <T> List<String> jsonStringListFrom(List<T> objectCollection) {
        return objectCollection.stream()
                .map((T object) -> {
                    try {
                        return ObjectMapperManager.getInstance().defaultMapper().writeValueAsString(object);
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .map(JsonUtils::makePretty)
                .toList();
    }

}
