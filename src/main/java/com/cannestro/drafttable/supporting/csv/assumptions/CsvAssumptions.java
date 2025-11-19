package com.cannestro.drafttable.supporting.csv.assumptions;

import com.cannestro.drafttable.supporting.options.SupportedExtension;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static com.cannestro.drafttable.supporting.options.SupportedExtension.*;


public class CsvAssumptions {

    public static final List<SupportedExtension> SUPPORTED_EXTENSIONS = List.of(CSV, TXT, TSV);


    private CsvAssumptions() {}

    public static void assumeFilenameIsCsvCompatible(@NonNull String filename) {
        assumeExtensionIsCsvCompatible(SupportedExtension.valueOf(FilenameUtils.getExtension(filename).toUpperCase()));
    }

    public static void assumeExtensionIsCsvCompatible(@NonNull SupportedExtension extension) {
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(String.format("Assumption broken - The input did not end with a supported CSV extension - [%s] not in %s", extension, SUPPORTED_EXTENSIONS));
        }
    }

}
