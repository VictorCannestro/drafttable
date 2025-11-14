package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.cannestro.drafttable.supporting.utils.ObjectMapperManager;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;


/**
 * @author Victor Cannestro
 */
public class DefaultJsonLoader implements JsonLoader {

    @Override
    public <T extends Mappable> DraftTable at(@NonNull Path path, @NonNull Class<T> schema) {
        return load(path.toFile(), schema, ObjectMapperManager.getInstance().defaultMapper());
    }

    @Override
    public <T extends Mappable> DraftTable at(@NonNull URL url, @NonNull Class<T> schema) {
        File file = FileUtils.copyToTempDirectory(url);
        DraftTable draftTable = load(file, schema, ObjectMapperManager.getInstance().defaultMapper());
        FileUtils.cleanUpTemporaryFiles(file);
        return draftTable;
    }

    public <T extends Mappable> DraftTable load(@NonNull File file,
                                                @NonNull Class<T> schema,
                                                @NonNull ObjectMapper mapper) {
        try {
            return FlexibleDraftTable.create().fromObjects(
                    file.getName(),
                    mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, schema))
            );
        } catch (DatabindException databindException) {
            throw new IllegalArgumentException("The input JSON structure does not match structure expected for result type (or has other mismatch).", databindException);
        } catch (IOException ioException) {
            throw new IllegalArgumentException("A low-level I/ O problem (unexpected end-of-input, network error) occurred.", ioException);
        }
    }

}
