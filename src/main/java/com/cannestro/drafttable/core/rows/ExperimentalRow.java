package com.cannestro.drafttable.core.rows;

import com.cannestro.drafttable.supporting.utils.MapUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.annotations.Beta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cannestro.drafttable.supporting.utils.MapUtils.zip;
import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@Beta
@Slf4j
public record ExperimentalRow(Map<String, ?> map) implements Row {

    private static final ObjectMapper DEFAULT_MAPPER;

    static {
        DEFAULT_MAPPER = new ObjectMapper();
        DEFAULT_MAPPER.registerModule(new JavaTimeModule());
        DEFAULT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public ExperimentalRow {
        if (isNull(map)) {
            throw new IllegalArgumentException("Cannot create a null row");
        }
    }

    public static ExperimentalRow from(List<String> keys, List<?> values) {
        return new ExperimentalRow(zip(keys, values));
    }

    public static ExperimentalRow from(Mappable object) {
        return new ExperimentalRow(object.asMap());
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
        try {
            return new ExperimentalRow(
                    DEFAULT_MAPPER.readValue(
                            DEFAULT_MAPPER.writeValueAsString(map()),
                            new TypeReference<>() {}
                    )
            );
        } catch (JsonProcessingException | UnsupportedOperationException e) {
            throw new IllegalArgumentException(String.format("Cannot create a copy the row%nCause: %s", e));
        }
    }

    @Override
    public <T> T as(Class<T> target) {
        try {
            return DEFAULT_MAPPER.readValue(DEFAULT_MAPPER.writeValueAsString(map()), target);
        } catch (JsonProcessingException | UnsupportedOperationException e) {
            log.debug("Cannot convert the row to a {}", target);
            throw new IllegalArgumentException(String.format("Cannot convert the row to a%s%nCause: %s", target, e));
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
