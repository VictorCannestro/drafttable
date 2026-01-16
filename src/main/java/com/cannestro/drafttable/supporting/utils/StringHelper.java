package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;


public class StringHelper {

    private StringHelper(){}

    public static @NonNull String nullToEmpty(@Nullable String string) {
        return isNull(string) ? "" : string;
    }

    public static @Nullable String passWhen(boolean condition, @Nullable String string) {
        if (condition) {
            return string;
        }
        return "";
    }

    public static @NonNull String safeConcat(@Nullable String... strings) {
        return Arrays.stream(strings).map(StringHelper::nullToEmpty).collect(Collectors.joining());
    }

}
