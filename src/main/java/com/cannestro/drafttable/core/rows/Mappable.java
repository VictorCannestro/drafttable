package com.cannestro.drafttable.core.rows;

import org.jspecify.annotations.NonNull;

import java.util.Map;


/**
 * A {@code Mappable} object is intended to be an object that exposes a user designated subset of its state using a map.
 *
 *  @author Victor Cannestro
 */
@FunctionalInterface
public interface Mappable {

    /**
     * When building the map, it's recommended to pass defensive copies of non-primitive objects. Values may be null so
     * exercise caution when choosing constructs. Example usage:
     * <pre>{@code
     * public record Foo(int n, Bar bar) implements Mappable {
     *     @Override
     *     public Map<String, ?> asMap() {
     *         return MapBuilder.with().entry("n", n).entry("bar", bar.copy()).asMap();
     *     }
     * }
     * }</pre>
     * @return A {@code Map}
     */
    Map<@NonNull String, ?> asMap();

}
