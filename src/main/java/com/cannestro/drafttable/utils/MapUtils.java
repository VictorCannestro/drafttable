package com.cannestro.drafttable.utils;

import com.google.common.collect.Streams;

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
        Streams.forEachPair(keys.stream(), values.stream(), map::put);
        return map;
    }

}
