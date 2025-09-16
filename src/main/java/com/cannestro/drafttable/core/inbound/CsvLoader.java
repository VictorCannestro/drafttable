package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.tables.DraftTable;
import lombok.NonNull;


public interface CsvLoader {

    /**
     * <p><b>Requires</b>: The first row of the CSV must contain comma delimited headers/column names. Subsequent rows,
     *                     if present, must contain comma delimited values. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created. Columns names will be mapped
     *                       verbatim. Column order may not be preserved. It may be empty. </p>
     *
     * @param filePath A valid path to the CSV resource to be read, e.g., {@code "csv/data.csv"}
     * @return A new {@code DraftTable} instance with {@code String} data
     */
    DraftTable at(@NonNull String filePath);

    DraftTable at(@NonNull String filePath, @NonNull CsvOptions loadingOptions);

}
