package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.supporting.csv.CsvWritingOptions;
import com.cannestro.drafttable.supporting.csv.options.CustomizableWritingOptions;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.Iterator;


public interface DraftTableOutput {

    void toCsv(@NonNull File file, @NonNull CsvWritingOptions options);

    String toJsonString();

    void toJson(@NonNull File outputFile);

    Iterator<String> structure();

    /**
     * <p> Prints a highly readable table representation of the {@code DraftTable}. <b>Row order is preserved</b> when
     * pretty printing, however <b>column order is not guaranteed</b>. Non-primitive objects will be represented by
     * their {@code toString()} output. </p>
     *
     * @return An iterator observable containing the header, divider, and contents (if present)
     */
    Iterator<String> prettyPrint();

    /**
     * <p> Prints a highly readable table representation of the {@code DraftTable}. <b>Row order is preserved</b> when
     * pretty printing, however <b>column order is not guaranteed</b>. Non-primitive objects will be represented by
     * their {@code toString()} output.
     * <br><br>
     * Printed columns will be <b>at least</b> as wide as their column names, respectively. A
     * {@code characterLimitPerColumn} may be specified to truncate the displayed output of every column. This is useful
     * for columns whose values have large {@code toString()} outputs. </p>
     *
     * @param characterLimitPerColumn A non-negative integer.
     * @return An iterator observable containing the header, divider, and contents (if present)
     */
    Iterator<String> prettyPrint(int characterLimitPerColumn);

    /**
     * Exports the table to a user designated destination using comma delimiters and String values.
     *
     * @param file The destination file containing the filepath
     */
    default void toCsv(@NonNull File file) {
        toCsv(file, CustomizableWritingOptions.allDefaults());
    }

}
