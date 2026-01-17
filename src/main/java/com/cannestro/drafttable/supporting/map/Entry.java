package com.cannestro.drafttable.supporting.map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * @author Victor Cannestro
 */
public record Entry<K, V>(@NonNull K key, @Nullable V value) {}