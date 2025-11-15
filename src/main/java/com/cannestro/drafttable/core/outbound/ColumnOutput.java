package com.cannestro.drafttable.core.outbound;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.Iterator;


public interface ColumnOutput {

    String toJsonString();

    void toJson(@NonNull File outputFile);

    /**
     * <p> Prints a highly readable table representation of the {@code Column} to the console alongside an index value.
     * <b> Row order is preserved </b> when pretty printing. Non-primitive objects will be represented by their
     * {@code toString()} output. </p>
     */
    Iterator<String> prettyPrint();

    /**
     * <p> Prints a highly readable table representation of the descriptive statistics of the {@code Column} to the
     * console. Statistical measures included:
     * <ul>
     *     <li> Maximum </li>
     *     <li> Arithmetic Mean </li>
     *     <li> Minimum </li>
     *     <li> Standard deviation </li>
     *     <li> Variance </li>
     *     <li> 25th, 50th, and 75th percentile estimates </li>
     * </ul> </p>
     */
    Iterator<String> describe();

}
