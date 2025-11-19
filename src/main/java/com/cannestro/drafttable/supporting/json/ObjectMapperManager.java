package com.cannestro.drafttable.supporting.json;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;



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
