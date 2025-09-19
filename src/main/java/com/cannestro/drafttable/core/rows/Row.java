package com.cannestro.drafttable.core.rows;

import com.google.common.annotations.Beta;

import java.util.List;
import java.util.Map;


/**
 * @author Victor Cannestro
 */
@Beta
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
     * @return True if and only if the {@code Row} contains the provided key
     */
    boolean hasKey(String columnName);

    /**
     * <p> <b>Guarantees</b>: The value associated with provided key is returned, given they both exist. </p>
     *
     * @return The value associated with the provided key
     */
    <T> T valueOf(String columnName);

    /**
     * <p> <b>Guarantees</b>: A collection of every key contained within the {@code Row} is returned. It may be empty. </p>
     *
     * @return The value associated with the provided key
     */
    List<String> keys();

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
     * @return A user defined object
     */
    <T> T as(Class<T> target);

}
