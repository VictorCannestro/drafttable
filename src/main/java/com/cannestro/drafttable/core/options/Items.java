package com.cannestro.drafttable.core.options;

import com.cannestro.drafttable.supporting.utils.ArrayUtils;

import java.util.Arrays;
import java.util.List;


/**
 * @author Victor Cannestro
 */
public record Items<T>(List<T> params) {

    @SafeVarargs
    public static <T> Items<T> of(T... params) {
        return createOptionsFrom(params);
    }

    public static <T> Items<T> of(List<T> params) {
        return createOptionsFrom(params);
    }

    @SafeVarargs
    public static <T> Items<T> with(T... params) {
        return createOptionsFrom(params);
    }

    public static <T> Items<T> with(List<T> params) {
        return createOptionsFrom(params);
    }

    @SafeVarargs
    public static <T> Items<T> from(T... params) {
        return createOptionsFrom(params);
    }

    public static <T> Items<T> from(List<T> params) {
        return createOptionsFrom(params);
    }

    @SafeVarargs
    public static <T> Items<T> to(T... params) {
        return createOptionsFrom(params);
    }

    public static <T> Items<T> to(List<T> params) {
        return createOptionsFrom(params);
    }

    @SafeVarargs
    public static <T> Items<T> these(T... params) {
        return createOptionsFrom(params);
    }

    public static <T> Items<T> these(List<T> params) {
        return createOptionsFrom(params);
    }

    @SafeVarargs
    public static <T> Items<T> using(T... params) {
        return createOptionsFrom(params);
    }

    public static <T> Items<T> using(List<T> params) {
        return createOptionsFrom(params);
    }

    @SafeVarargs
    public static <T> Items<T> named(T... params) {
        return createOptionsFrom(params);
    }

    public static <T> Items<T> named(List<T> params) {
        return createOptionsFrom(params);
    }

    public T[] paramsArray() {
       return ArrayUtils.asArray(params());
    }

    @SafeVarargs
    static <T> Items<T> createOptionsFrom(T... params) {
        return new Items<>(Arrays.stream(params).toList());
    }

    static <T> Items<T> createOptionsFrom(List<T> params) {
        return new Items<>(params);
    }

}
