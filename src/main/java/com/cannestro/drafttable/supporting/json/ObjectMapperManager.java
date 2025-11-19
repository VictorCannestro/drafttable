package com.cannestro.drafttable.supporting.json;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;


/**
 * <p> Manages a Singleton reference for the project's default ObjectMapper with the following overriding settings:
 *   <ol>
 *     <li> {@code JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS} = {@code true} </li>
 *     <li> {@code JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS} = {@code true} </li>
 *     <li> {@code JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS} = {@code true} </li>
 *     <li> {@code JsonReadFeature.ALLOW_MISSING_VALUES} = {@code true} </li>
 *     <li> {@code DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY} = {@code true} </li>
 *     <li> {@code DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT} = {@code true} </li>
 *     <li> {@code DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES} = {@code false} </li>
 *   </ol>
 * </p>
 */
public class ObjectMapperManager {

    private static class SingletonHelper {

        private static final ObjectMapperManager INSTANCE = new ObjectMapperManager();
        private static final ObjectMapper MAPPER;

        static {
            MAPPER = JsonMapper.builder()
                    .configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
                    .configure(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS, true)
                    .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
                    .configure(JsonReadFeature.ALLOW_MISSING_VALUES, true)
                    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                    .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                    .build();
        }
    }

    private ObjectMapperManager() {}

    public static ObjectMapperManager getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public ObjectMapper defaultMapper() {
        return SingletonHelper.MAPPER;
    }

}
