package com.cannestro.drafttable.supporting.json.assumptions;

import com.cannestro.drafttable.supporting.options.SupportedExtension;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static com.cannestro.drafttable.supporting.options.SupportedExtension.*;


/**
 * @author Victor Cannestro
 */
public final class JsonAssumptions {

    public static final List<SupportedExtension> SUPPORTED_EXTENSIONS = List.of(JSON, GEOJSON);


    private JsonAssumptions() {}

    /**
     * <p><b>Requires</b>: The textual part of the file name after the last period must not include a directory
     * separator, e.g., {@code "desktop/data.json/etc"}. The input must not end at a directory, e.g.,
     * {@code "desktop/"}. </p>
     * <p><b>Guarantees</b>: The filename extension declares a supported value (i.e., JSON or GeoJSON), otherwise, an
     * exception will be thrown </p>
     *
     * @param filename A string, e.g., {@code "desktop/data.json"} or {@code "data.json"}
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static void assumeFilenameIsJsonCompatible(@NonNull String filename) {
        assumeExtensionIsJsonCompatible(SupportedExtension.valueOf(FilenameUtils.getExtension(filename).toUpperCase()));
    }

    /**
     * <p><b>Requires</b>: The input {@code SupportedExtension} must not be null </p>
     * <p><b>Guarantees</b>: The filename extension declares a supported value (i.e., JSON or GeoJSON), otherwise, an
     * exception will be thrown </p>
     *
     * @param extension Any explicitly {@code SupportedExtension}
     * @throws IllegalArgumentException when guarantee cannot be made
     */
    public static void assumeExtensionIsJsonCompatible(@NonNull SupportedExtension extension) {
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(String.format(
                    "Assumption broken - The input did not end with a supported JSON extension - [%s] not in %s",
                    extension, SUPPORTED_EXTENSIONS
            ));
        }
    }

}
