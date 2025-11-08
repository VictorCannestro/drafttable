package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record Entry<K, V>(@NonNull K key, @Nullable V value) {}