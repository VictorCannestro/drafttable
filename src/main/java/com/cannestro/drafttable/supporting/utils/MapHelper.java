package com.cannestro.drafttable.supporting.utils;

import com.cannestro.drafttable.supporting.map.Entry;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.paumard.streams.StreamsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.cannestro.drafttable.core.assumptions.ListAssumptions.assumeSizesMatch;


/**
 * @author Victor Cannestro
 */
public final class MapHelper {

    private MapHelper() {}

    /**
     * <p> <b>Requires</b>: Each type reference and each element of {@code keys} must not be null. Order matters in this
     * operation so clients must ensure key-value paris align with their expectations via indices. </p>
     * <p> <b>Guarantees</b>: A map created by zipping together the keys and values via index will be returned </p>
     *
     * @param keys A list of unique keys
     * @param values A list that may contain nulls or duplicates
     * @return A map made from the input key-value pairs
     * @param <K> The key type
     * @param <V> the value type
     */
    public static <K, V> Map<K, V> zip(@NonNull List<@NonNull K> keys, @NonNull List<@Nullable V> values)  {
        assumeSizesMatch(keys, values);
        Map<K, V> map = new HashMap<>(keys.size());
        StreamsUtils.zip(keys.stream(), values.stream(), Entry::new).forEach(entry -> map.put(entry.key(), entry.value()));
        return map;
    }

    /**
     * <p> <b>Requires</b>: The list and each {@code Entry} reference must not be null </p>
     * <p> <b>Guarantees</b>: A map created using the key-value entries will be returned </p>
     *
     * @param entries Key-value pairs
     * @return A map made from the input key-value entries
     * @param <K> The key type
     * @param <V> the value type
     */
    public static <K, V> Map<@NonNull K, @Nullable V> toMap(@NonNull List<@NonNull Entry<K, V>> entries)  {
        Map<K, V> map = new HashMap<>();
        entries.forEach(entry -> map.putIfAbsent(entry.key(), entry.value()));
        return map;
    }

    /**
     * <p> <b>Requires</b>: The supplied map and function reference must not be null </p>
     * <p> <b>Guarantees</b>: A new map in which the provided function has been applied to the value entries of the
     * input map will be returned </p>
     *
     * @param map The operation target
     * @param function An operation to apply to the map values
     * @return A new map
     */
    public static Map<String, String> applyToKeysAndValuesOf(@NonNull Map<String, String> map, @NonNull UnaryOperator<String> function) {
        Map<String, String> processedParams = new HashMap<>();
        MapIterator<String, String> mapperator = MapUtils.iterableMap(map).mapIterator();
        while (mapperator.hasNext()) {
            processedParams.putIfAbsent(function.apply(mapperator.next()), function.apply(mapperator.getValue()));
        }
        return processedParams;
    }
}
