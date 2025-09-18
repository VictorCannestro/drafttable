package com.cannestro.drafttable.supporting.utils;

import com.cannestro.drafttable.core.rows.Mappable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cannestro.drafttable.supporting.utils.MapUtils.zip;


/**
 * @author Victor Cannestro
 */
public class MapBuilder implements Mappable {

    private final List<String> keys = new ArrayList<>();
    private final List values = new ArrayList<>();


    public static MapBuilder with() {
        return new MapBuilder();
    }

    public <V> MapBuilder entry(String key, V value) {
        keys.add(key);
        values.add(value);
        return this;
    }

    @Override
    public Map<String, ?> asMap() {
        return zip(keys, values);
    }

}
