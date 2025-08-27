package com.cannestro.drafttable.core.implementations;

import com.cannestro.drafttable.core.Row;
import com.google.common.annotations.Beta;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.cannestro.drafttable.utils.MapUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

import static com.cannestro.drafttable.utils.mappers.GsonSupplier.DEFAULT_GSON;
import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@Beta
@Slf4j
public record HashMapRow(Map<String, ?> map) implements Row {

    private static volatile Gson defaultGson = DEFAULT_GSON;


    public HashMapRow {
        if (isNull(map)) {
            throw new IllegalArgumentException("Cannot create a null row");
        }
    }

    public static synchronized void setDefaultGson(@NonNull Gson gson) {
         defaultGson = gson;
    }

    public static HashMapRow from(List<String> keys, List<?> values) {
        return new HashMapRow(MapUtils.zip(keys, values));
    }

    public static <T> HashMapRow from(T object) {
        try {
            return new HashMapRow(
                    defaultGson.fromJson(
                            defaultGson.toJson(object),
                            new TypeToken<Map<String, ?>>() {}.getType()
                    )
            );
        } catch (JsonSyntaxException e) {
            log.debug("Cannot convert {} to a row", object.getClass());
            throw new IllegalArgumentException(String.format("Cannot convert %s to a row%nCause: %s", object.getClass(), e));
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean hasKey(String columnName) {
        return map().containsKey(columnName);
    }

    @Override
    public <T> T valueOf(String columnName) {
        if (isNull(map.get(columnName))) {
            return null;
        }
        return (T) map.get(columnName);
    }

    @Override
    public List<String> keys() {
        return new ArrayList<>(map.keySet());
    }

    @Override
    public Map<String, ?> valueMap() {
        return map();
    }

    @Override
    public Row deepCopy() {
        return from(map());
    }

    @Override
    public <T> T as(Class<T> target) {
        try {
            return defaultGson.fromJson(defaultGson.toJson(map()), target);
        } catch (JsonSyntaxException | UnsupportedOperationException e) {
            log.debug("Cannot convert the row to a {}", target);
            throw new IllegalArgumentException(String.format("Cannot convert the row to a%s%nCause: %s", target, e));
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
