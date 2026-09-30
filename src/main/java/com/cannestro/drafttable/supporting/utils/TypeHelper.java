package com.cannestro.drafttable.supporting.utils;

import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.util.*;


public class TypeHelper {

    public static final Set<Class<?>> KNOWN_IMMUTABLE_TYPES = Set.of(
            BigDecimal.class,
            BigInteger.class,
            Boolean.class,
            Byte.class,
            Character.class,
            Double.class,
            Duration.class,
            Float.class,
            Instant.class,
            Integer.class,
            LocalDate.class,
            LocalDateTime.class,
            LocalTime.class,
            Long.class,
            MonthDay.class,
            OffsetDateTime.class,
            OffsetTime.class,
            Optional.class,
            Period.class,
            Short.class,
            String.class,
            URI.class,
            UUID.class,
            Year.class,
            YearMonth.class,
            ZoneOffset.class,
            ZonedDateTime.class
    );


    private TypeHelper() {}

    public static boolean isKnownImmutable(@NonNull Class<?> type) {
        if (type.isInterface()) {
            return false;
        }
        if (type.isRecord()) {
            return Arrays.stream(type.getRecordComponents()).allMatch(component -> isKnownImmutable(component.getType()));
        }
        return type.isPrimitive() || type.isEnum() || KNOWN_IMMUTABLE_TYPES.contains(type);
    }

}
