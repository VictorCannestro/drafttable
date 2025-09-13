package com.cannestro.drafttable.supporting.utils.mappers;

import com.cannestro.drafttable.supporting.utils.mappers.typeadapters.LocalDateTimeTypeAdapter;
import com.cannestro.drafttable.supporting.utils.mappers.typeadapters.LocalDateTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * @author Victor Cannestro
 */
public class GsonSupplier {

    private GsonSupplier() {}

    public static final GsonBuilder DEFAULT_GSON_BUILDER = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .enableComplexMapKeySerialization()
            .serializeNulls();

    public static final Gson DEFAULT_GSON = DEFAULT_GSON_BUILDER.create();

}
