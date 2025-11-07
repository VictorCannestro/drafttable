package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.paumard.streams.StreamsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Victor Cannestro
 */
public class MapUtils {

    private MapUtils() {}

    public static <K, V> Map<K, V> zip(List<K> keys, List<V> values)  {
        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("Each key must have a corresponding value and vice versa");
        }
        Map<K, V> map = new HashMap<>();
        StreamsUtils.zip(keys.stream(), values.stream(), Entry::new).forEach(kvEntry -> map.put(kvEntry.key(), kvEntry.value()));
        return map;
    }

}
