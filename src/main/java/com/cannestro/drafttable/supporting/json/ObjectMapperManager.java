package com.cannestro.drafttable.supporting.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


public class ObjectMapperManager {

    private static class SingletonHelper {

        private static final ObjectMapperManager INSTANCE = new ObjectMapperManager();
        private static final ObjectMapper MAPPER;

        static {
            MAPPER = new ObjectMapper();
            MAPPER.registerModule(new JavaTimeModule());
            MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            MAPPER.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
            MAPPER.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
            MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
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
