package com.cannestro.drafttable.supporting.utils.mappers;

import com.cannestro.drafttable.supporting.utils.mappers.typeadapters.LocalDateTimeTypeAdapter;
import com.cannestro.drafttable.supporting.utils.mappers.typeadapters.LocalDateTypeAdapter;
import com.cannestro.drafttable.supporting.utils.mappers.typeadapters.LocalTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


/**
 * @author Victor Cannestro
 */
public class GsonSupplier {

    private GsonSupplier() {}

    public static final GsonBuilder DEFAULT_GSON_BUILDER = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter())
            .enableComplexMapKeySerialization()
            .serializeNulls();

    public static final Gson DEFAULT_GSON = DEFAULT_GSON_BUILDER.create();

}
