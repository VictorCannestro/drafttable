package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
public final class StringHelper {

    private StringHelper(){}

    /**
     * <p><b>Guarantees</b>: The input {@code string} will never be null. If a null input is detected, then an empty
     * string will be returned. Otherwise, the input will be passed along. </p>
     *
     * @param string Any String
     * @return A non-null String
     */
    public static @NonNull String nullToEmpty(@Nullable String string) {
        return isNull(string) ? "" : string;
    }

    /**
     * <p><b>Guarantees</b>: If the {@code condition} is true, then the input will be passed along, otherwise, an empty
     * String will be returned </p>
     *
     * @param condition A toggle
     * @param string Any String
     * @return A nullable String
     */
    public static @Nullable String passWhen(boolean condition, @Nullable String string) {
        if (condition) {
            return string;
        }
        return "";
    }

    /**
     * <p><b>Guarantees</b>: The {@code strings} will be concatenated in the order provided. If a null input is detected,
     * then an empty string will be concatenated. Otherwise, the input will be concatenated. For example: <pre>{@code
     * Foo f = new Foo(); // Assume POJO with String fooField
     * safeConcat("foo", null, "bar", f.getFooField()) // Returns "foobar"
     * }</pre></p>
     *
     * @param strings One or more nullable Strings
     * @return A non-null String
     */
    public static @NonNull String safeConcat(@Nullable String... strings) {
        return Arrays.stream(strings).map(StringHelper::nullToEmpty).collect(Collectors.joining());
    }

}
