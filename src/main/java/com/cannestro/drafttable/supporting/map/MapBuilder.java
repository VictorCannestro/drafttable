package com.cannestro.drafttable.supporting.map;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.supporting.utils.MapHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Victor Cannestro
 */
public class MapBuilder implements Mappable {

    private final List<Object> keys = new ArrayList<>();
    private final List values = new ArrayList<>();


    /**
     * <p><b>Guarantees</b>: A new {@code MapBuilder} instance will be created from the fluent entry point of this API </p>
     *
     * @return A {@code MapBuilder}
     */
    public static MapBuilder with() {
        return new MapBuilder();
    }

    /**
     * <p><b>Requires</b>: The {@code key} must not be null, however, the {@code value} may be null. When populating the
     * map, it's recommended to pass defensive copies of non-primitive objects. </p>
     * <p><b>Guarantees</b>: The key-value pair will be stored in the {@code MapBuilder}. This is a repeatable action.
     * Access to the underlying data requires clients to call {@code asMap()}. </p>
     *
     * @param key The key component of a key-value pair
     * @param value The value component of a key-value pair
     * @return The chainable {@code MapBuilder}
     * @param <K> Any type
     * @param <V> Any type
     */
    @SuppressWarnings("unchecked")
    public <K, V> MapBuilder entry(@NonNull K key, @Nullable V value) {
        keys.add(key);
        values.add(value);
        return this;
    }

    /**
     * <p><b>Requires</b>: The {@code entry} and its key component must not be null, however, the value component may be.
     * When populating the map, it's recommended to pass defensive copies of non-primitive objects. </p>
     * <p><b>Guarantees</b>: The key-value pair will be stored in the {@code MapBuilder}. This is a repeatable action.
     * Access to the underlying data requires clients to call {@code asMap()}. </p>
     *
     * @param entry Any {@code Entry}
     * @return The chainable {@code MapBuilder}
     */
    @SuppressWarnings("unchecked")
    public MapBuilder entry(@NonNull Entry<@NonNull Object, ?> entry) {
        keys.add(entry.key());
        values.add(entry.value());
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, ?> asMap() {
        return MapHelper.zip(keys, values);
    }

}
