package com.cannestro.drafttable.supporting.utils.mappers.typeadapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


/**
 * @author Victor Cannestro
 */
public class LocalTimeTypeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

    @Override
    public JsonElement serialize(final LocalTime time,
                                 final Type typeOfSrc,
                                 final JsonSerializationContext context) {
        return new JsonPrimitive(time.format(DateTimeFormatter.ISO_LOCAL_TIME));
    }

    @Override
    public LocalTime deserialize(final JsonElement json,
                                 final Type typeOfT,
                                 final JsonDeserializationContext context) throws JsonParseException {
        return LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_TIME);
    }

}
