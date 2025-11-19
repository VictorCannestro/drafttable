package com.cannestro.drafttable.supporting.map;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.supporting.utils.MapUtils;
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


    public static MapBuilder with() {
        return new MapBuilder();
    }

    public <K, V> MapBuilder entry(@NonNull K key, @Nullable V value) {
        keys.add(key);
        values.add(value);
        return this;
    }

    public MapBuilder entry(@NonNull Entry<@NonNull Object, ?> entry) {
        keys.add(entry.key());
        values.add(entry.value());
        return this;
    }

    @Override
    public Map<String, ?> asMap() {
        return MapUtils.zip(keys, values);
    }

}
