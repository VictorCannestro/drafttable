package com.cannestro.drafttable.utils;

import com.google.gson.*;
import lombok.NonNull;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.gson.JsonParser.parseString;
import static com.cannestro.drafttable.utils.ListUtils.firstElementOf;


/**
 * @author Victor Cannestro
 */
public class JsonUtils {

    private JsonUtils() {}

    public static String prettyJsonString(String unformattedJsonString) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = parseString(unformattedJsonString);
        return gson.toJson(jsonElement);
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
        if (!isJSONCompatible(objectCollection)) {
            throw new IllegalArgumentException("The String representation of each collection item must be supplied in a JSON format");
        }
        return objectCollection.stream()
                .map(Object::toString)
                .map(JsonUtils::prettyJsonString)
                .collect(Collectors.toList());
    }

    /**
     * Tests the requirement that the String representation of each collection item can be supplied as a Json format
     *
     * @param collection A collection of a single type
     * @return A boolean flag of whether the input is JSON compatible
     * @param <T> Any single type except for lists or collections
     */
    public static <T> boolean isJSONCompatible(@NonNull List<T> collection) {
        if (collection.isEmpty()) {
            return false;
        }
        try {
            // TODO: This usage of try/catch is a technical debt that should be refactored into a proper conditional
            parseString(String.valueOf(firstElementOf(collection))).getAsJsonObject();
        } catch (IllegalStateException | JsonParseException | NullPointerException e) {
            return false;
        }
        return true;
    }

}
