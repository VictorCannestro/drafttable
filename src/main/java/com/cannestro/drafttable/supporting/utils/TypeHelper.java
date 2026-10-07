package com.cannestro.drafttable.supporting.utils;

import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.util.*;


public final class TypeHelper {

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

    private TypeHelper() {}


    /**
     * <p> <b>Requires</b>: The input type must not be null </p>
     * <p> <b>Guarantees</b>: True if {@code type} is a known immutable representation or value-based class with respect
     * to {@code TypeHelper} as a source of truth, and false otherwise. Makes no guarantee on being a comprehensive
     * authority across internal and external Java libraries. See implementation for details. </p>
     *
     * @param type Any type
     * @return true or false
     * @apiNote Value based classes outside select core Java libraries, such as Lombok generated {@code @Value} classes, are
     * not considered "known". A heuristic approach is taken to cast a wide net over commonly used types.
     */
    public static boolean isKnownImmutable(@NonNull Class<?> type) {
        return isKnownImmutableImplementation(type, new HashSet<>());
    }

    /**
     * Depth first traversal of a type graph: white node = not yet visited, grey = currently on the recursion stack (is
     * an ancestor), black = fully explored.
     *
     * @param type Class type
     * @param inProgress grey set for DFS
     * @return Whether type is a known immutable
     */
    static boolean isKnownImmutableImplementation(@NonNull Class<?> type, Set<Class<?>> inProgress) {
        if (!type.isRecord()) {
            return isEffectivelyValueBased(type);
        }
        if (!inProgress.add(type)) {
            return false; // Was already grey -> Back edge = Cycle found
        }
        try {
            return Arrays.stream(type.getRecordComponents())
                    .allMatch(component -> isKnownImmutableImplementation(component.getType(), inProgress));
        } finally {
            inProgress.remove(type); // Repaints a node black
        }
    }

    static boolean isEffectivelyValueBased(@NonNull Class<?> type) {
        if (type.isInterface()) {
            return false;
        }
        if (type.isAnonymousClass() && type.getSuperclass().isEnum()) {
            return true;
        }
        return type.isPrimitive() || type.isEnum() || KNOWN_IMMUTABLE_TYPES.contains(type);
    }

}
