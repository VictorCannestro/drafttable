package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.supporting.csv.CsvParsingOptions;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.nio.file.Path;


/**
 * @author Victor Cannestro
 */
public interface CsvLoader {

    /**
     * <p><b>Requires</b>: The first row of the CSV must contain comma delimited headers/column names. Subsequent rows,
     *                     if present, must contain comma delimited values. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created. Columns names will be mapped
     *                       verbatim. Column order may not be preserved. It may be empty. </p>
     *
     * @param path A valid path to the CSV resource to be read, e.g., {@code "csv/data.csv"}
     * @return A new {@code DraftTable} instance with {@code String} data
     */
    DraftTable at(@NonNull Path path);

    /**
     * <p><b>Requires</b>: The CSV must contain delimited headers/column names before data rows. Subsequent rows,
     *                     if present, must contain delimited values. Preceding rows such as comment rows, if present,
     *                     are allowed but must be specified. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created. Columns names will be mapped
     *                       verbatim. Column order may not be preserved. It may be empty. Provided {@code CsvParsingOptions}
     *                       will take precedence over default values in the parser. </p>
     *
     * @param path A valid path to the CSV resource to be read, e.g., {@code "csv/data.csv"}
     * @param loadingOptions Explicit expectations to be given to the parser to increase parsing success
     * @return A new {@code DraftTable} instance with {@code String} data
     */
    DraftTable at(@NonNull Path path, @NonNull CsvParsingOptions loadingOptions);

    /**
     * <p><b>Requires</b>: The first row of the CSV must contain comma delimited headers/column names. Subsequent rows,
     *                     if present, must contain comma delimited values. The URI must exist and point to an accessible
     *                     CSV resource. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created. Columns names will be mapped
     *                       verbatim. Column order may not be preserved. It may be empty. </p>
     *
     * @param uri A valid URI to the CSV resource, e.g., {@code "http://foo.com/bar/data.csv"}
     * @return A new {@code DraftTable} instance with {@code String} data
     */
    DraftTable at(@NonNull URI uri);

    /**
     * <p><b>Requires</b>: The CSV must contain delimited headers/column names before data rows. Subsequent rows,
     *                     if present, must contain delimited values. Preceding rows such as comment rows, if present,
     *                     are allowed but must be specified. The URI must exist and point to an accessible CSV resource.</p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created. Columns names will be mapped
     *                       verbatim. Column order may not be preserved. It may be empty. Provided {@code CsvParsingOptions}
     *                       will take precedence over default values in the parser. </p>
     *
     * @param uri A valid URI to the CSV resource, e.g., {@code "http://foo.com/bar/data.csv"}
     * @param loadingOptions Explicit expectations to be given to the parser to increase parsing success
     * @return A new {@code DraftTable} instance with {@code String} data
     */
    DraftTable at(@NonNull URI uri, @NonNull CsvParsingOptions loadingOptions);

}
