package com.cannestro.drafttable.core.options;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;


/**
 * Wrapper class API used to simulate named parameters.
 * 
 * @author Victor Cannestro
 */
public record Items<T>(@NonNull List<@Nullable T> params) {

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    @SafeVarargs
    public static <T> Items<T> of(@Nullable T... params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    public static <T> Items<T> of(@NonNull List<@Nullable T> params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    @SafeVarargs
    public static <T> Items<T> with(@Nullable T... params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    public static <T> Items<T> with(@NonNull List<@Nullable T> params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    @SafeVarargs
    public static <T> Items<T> from(@Nullable T... params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    public static <T> Items<T> from(@NonNull List<@Nullable T> params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    @SafeVarargs
    public static <T> Items<T> to(@Nullable T... params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    public static <T> Items<T> to(@NonNull List<@Nullable T> params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    @SafeVarargs
    public static <T> Items<T> these(@Nullable T... params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    public static <T> Items<T> these(@NonNull List<@Nullable T> params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    @SafeVarargs
    public static <T> Items<T> using(@Nullable T... params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    public static <T> Items<T> using(@NonNull List<@Nullable T> params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    @SafeVarargs
    public static <T> Items<T> named(@Nullable T... params) {
        return createOptionsFrom(params);
    }

    /**
     * Wrapper method to store nullable values.
     *
     * @param params The values to be wrapped
     * @return A new {@code Items} instance
     * @param <T> Any type
     */
    public static <T> Items<T> named(@NonNull List<@Nullable T> params) {
        return createOptionsFrom(params);
    }


    /**
     * Unwraps the underlying nullable values into a constructed array.
     * 
     * @param arrayFactory A typed constructor method reference, e.g., {@code String[]::new}
     * @return An array
     */
    public T[] paramsArray(IntFunction<T[]> arrayFactory) {
       return params().toArray(arrayFactory);
    }

    
    @SafeVarargs
    static <T> Items<T> createOptionsFrom(@Nullable T... params) {
        return new Items<>(Arrays.stream(params).toList());
    }

    static <T> Items<T> createOptionsFrom(@NonNull List<@Nullable T> params) {
        return new Items<>(params);
    }

}
