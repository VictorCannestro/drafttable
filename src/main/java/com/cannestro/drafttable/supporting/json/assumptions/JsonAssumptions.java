package com.cannestro.drafttable.supporting.json.assumptions;

import com.cannestro.drafttable.supporting.options.SupportedExtension;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static com.cannestro.drafttable.supporting.options.SupportedExtension.*;


/**
 * @author Victor Cannestro
 */
public class JsonAssumptions {

    public static final List<SupportedExtension> SUPPORTED_EXTENSIONS = List.of(JSON, GEOJSON);


    private JsonAssumptions() {}

    public static void assumeFilenameIsJsonCompatible(@NonNull String filename) {
        assumeExtensionIsJsonCompatible(SupportedExtension.valueOf(FilenameUtils.getExtension(filename).toUpperCase()));
    }

    public static void assumeExtensionIsJsonCompatible(@NonNull SupportedExtension extension) {
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(String.format("Assumption broken - The input did not end with a supported JSON extension - [%s] not in %s", extension, SUPPORTED_EXTENSIONS));
        }
    }

}
