package com.cannestro.drafttable.core.options;

import lombok.EqualsAndHashCode;


/**
 * @author Victor Cannestro
 */
@EqualsAndHashCode
public class Item<T> {

    private final T value;


    private Item(T value) {
        this.value = value;
    }

    public T value() {
        return this.value;
    }

    public static <T> Item<T> as(T value) {
        return new Item<>(value);
    }

    public static <T> Item<T> into(T value) {
        return new Item<>(value);
    }

}
