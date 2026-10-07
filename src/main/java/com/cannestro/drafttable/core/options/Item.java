package com.cannestro.drafttable.core.options;

import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;


/**
 * Wrapper class API used to simulate named parameters.
 *
 * @author Victor Cannestro
 */
@EqualsAndHashCode
public final class Item<T> {

    private final T value;


    private Item(T value) {
        this.value = value;
    }

    /**
     * Unwraps the underlying nullable value.
     *
     * @return The underlying value
     */
    public @Nullable T value() {
        return this.value;
    }

    /**
     * Wrapper method to store a nullable value.
     *
     * @param value The value to be wrapped
     * @return A new {@code Item}
     * @param <T> Any type
     */
    public static <T> Item<T> as(@Nullable T value) {
        return new Item<>(value);
    }

    /**
     * Wrapper method to store a nullable value.
     *
     * @param value The value to be wrapped
     * @return A new {@code Item}
     * @param <T> Any type
     */
    public static <T> Item<T> into(@Nullable T value) {
        return new Item<>(value);
    }

}
