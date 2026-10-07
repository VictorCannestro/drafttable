package com.cannestro.drafttable.core.rows;

import com.cannestro.drafttable.supporting.json.ObjectMapperManager;

import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;

import java.util.*;

import static com.cannestro.drafttable.supporting.utils.MapHelper.zip;
import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@Slf4j
public record HashMapRow(Map<String, ?> map) implements Row {

    public HashMapRow {
        if (isNull(map)) {
            throw new IllegalArgumentException("Cannot create a null row");
        }
    }

    /**
     * <p><b>Requires</b>: This method assumes that the provided values may be of, arbitrary, possibly heterogeneous
     * types. For example: {@code List.of(1, "x", true, new Pojo())} or {@code List.of(1.0, 2.5, 3.9)}. List values may
     * be null. The values of {@code keys} effectively refer to column labels and must not be null. </p>
     * <p><b>Guarantees</b>: A new instance of {@code HashMapRow} from the provided input </p>
     *
     * @param keys A list of non-null values
     * @param values A list of an arbitrary, heterogeneous values and type
     * @return A new instance of {@code HashMapRow}
     */
    public static HashMapRow from(@NonNull List<@NonNull String> keys, @NonNull List<?> values) {
        return new HashMapRow(zip(keys, values));
    }

    /**
     * <p><b>Requires</b>: This method assumes that the provided input object is not null and implements {@code Mappable}. </p>
     * <p><b>Guarantees</b>: A new instance of {@code HashMapRow} from the provided input. Object fields defined through
     * the {@code Mappable} implementation will be mapped into type-preserved, key-value pairs based on the specific
     * instantiation provided. Field values may be null. </p>
     *
     * @param object Any {@code Mappable} object
     * @return A new instance of {@code HashMapRow}
     */
    public static HashMapRow from(@NonNull Mappable object) {
        return new HashMapRow(object.asMap());
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
    public boolean hasKey(@NonNull String columnName) {
        return map().containsKey(columnName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T valueOf(@NonNull String columnName) {
        if (isNull(map.get(columnName))) {
            return null;
        }
        return (T) map.get(columnName);
    }

    @Override
    public Set<String> keys() {
        return map.keySet();
    }

    @Override
    public Map<String, ?> valueMap() {
        return map();
    }

    @Override
    public Row deepCopy() {
        try {
            return new HashMapRow(
                    ObjectMapperManager.getInstance().defaultMapper().readValue(
                            ObjectMapperManager.getInstance().defaultMapper().writeValueAsString(map()),
                            new TypeReference<>() {}
                    )
            );
        } catch (JacksonException | UnsupportedOperationException e) {
            throw new IllegalArgumentException(String.format("Cannot create a copy the row%nCause: %s", e));
        }
    }

    @Override
    public <T> T as(@NonNull Class<T> target) {
        try {
            return ObjectMapperManager.getInstance().defaultMapper().readValue(
                    ObjectMapperManager.getInstance().defaultMapper().writeValueAsString(map()),
                    target
            );
        } catch (JacksonException | UnsupportedOperationException e) {
            log.debug("Cannot convert the row to a {}", target);
            throw new IllegalArgumentException(String.format("Cannot convert the row to a%s%nCause: %s", target, e));
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
