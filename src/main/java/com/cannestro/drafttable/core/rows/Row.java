package com.cannestro.drafttable.core.rows;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;


/**
 * @author Victor Cannestro
 */
public interface Row {

    /**
     * <p> <b>Guarantees</b>: The number of keys within the {@code Row} </p>
     *
     * @return A non-negative integer
     */
    int size();


    /**
     * <p> <b>Guarantees</b>: Queries the current state of the {@code Row} to determine if it does not contain any values </p>
     *
     * @return True if and only the {@code Row} has no contents
     */
    boolean isEmpty();

    /**
     * <p> <b>Guarantees</b>: Queries the current state of the {@code Row} to determine if it contains the provided key. </p>
     *
     * @param columnName A non-null string
     * @return True if and only if the {@code Row} contains the provided key
     */
    boolean hasKey(@NonNull String columnName);

    /**
     * <p> <b>Guarantees</b>: The value associated with provided key is returned, given they both exist. </p>
     *
     * @param columnName A non-null string
     * @param <T> Any nullable value
     * @return The value associated with the provided key
     */
    <T> @Nullable T valueOf(@NonNull String columnName);

    /**
     * <p> <b>Guarantees</b>: A collection of every key contained within the {@code Row} is returned. It may be empty. </p>
     *
     * @return The value associated with the provided key
     */
    Set<String> keys();

    /**
     * <p> <b>Guarantees</b>: The map containing every key-value pairing associated with contents of the {@code Row} is
     *                       returned. It may be empty. </p>
     *
     * @return A {@code Map}
     */
    Map<String, ?> valueMap();

    /**
     * <p> <b>Guarantees</b>:  A deep copy of the {@code Row} will be created. </p>
     *
     * @return A new {@code Row}
     */
    Row deepCopy();

    /**
     * <p> <b>Requires</b>: The keys of the {@code Row} have a 1-1 mapping onto the fields of the target class. </p>
     * <p> <b>Guarantees</b>:  An object of the target class will be instantiated based on the key-value pairings of the
     *                         {@code Row}. </p>
     *
     * @param target The target Type class
     * @param <T> The target Type
     * @return A user defined object
     */
    <T> T as(@NonNull Class<T> target);

}
