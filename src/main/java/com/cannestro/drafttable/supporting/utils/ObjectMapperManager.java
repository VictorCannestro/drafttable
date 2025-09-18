package com.cannestro.drafttable.supporting.utils;

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
