package com.cannestro.drafttable.supporting.utils;

import com.cannestro.drafttable.supporting.map.Entry;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.paumard.streams.StreamsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeSizesMatch;


/**
 * @author Victor Cannestro
 */
public class MapUtils {

    private MapUtils() {}

    public static <K, V> Map<K, V> zip(@NonNull List<@NonNull K> keys, @NonNull List<@Nullable V> values)  {
        assumeSizesMatch(keys, values);
        Map<K, V> map = new HashMap<>(keys.size());
        StreamsUtils.zip(keys.stream(), values.stream(), Entry::new).forEach(entry -> map.put(entry.key(), entry.value()));
        return map;
    }

    public static <K, V> Map<@NonNull K, @Nullable V> toMap(@NonNull List<@NonNull Entry<K, V>> entries)  {
        Map<K, V> map = new HashMap<>();
        entries.forEach(entry -> map.putIfAbsent(entry.key(), entry.value()));
        return map;
    }

}
