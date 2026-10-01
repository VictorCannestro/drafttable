package com.cannestro.drafttable.supporting.utils;

import lombok.NonNull;

import java.lang.reflect.RecordComponent;
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

    private final Set<Class<?>> memo = new HashSet<>();

    private TypeHelper() {}


    public static TypeHelper instance() {
        return new TypeHelper();
    }

    public boolean isKnownImmutable(@NonNull Class<?> type) {
        if (!type.isRecord()) {
            return isBasicImmutable(type);
        } else if (memo.contains(type)) {
            return false;
        } else {
            memo.add(type);
        }
        return Arrays.stream(type.getRecordComponents())
                .allMatch(component -> isNotSelfReferential(component, type) && isKnownImmutable(component.getType()));
    }

    static boolean isBasicImmutable(@NonNull Class<?> type) {
        if (type.isInterface()) {
            return false;
        }
        if (type.isAnonymousClass() && type.getSuperclass().isEnum()) {
            return true;
        }
        return type.isPrimitive() || type.isEnum() || KNOWN_IMMUTABLE_TYPES.contains(type);
    }

    static boolean isNotSelfReferential(@NonNull RecordComponent component, @NonNull Class<?> parentType) {
        return !Objects.equals(parentType, component.getType());
    }

}
