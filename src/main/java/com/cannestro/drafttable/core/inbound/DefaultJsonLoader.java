package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.utils.FileUtils;
import com.cannestro.drafttable.supporting.json.ObjectMapperManager;
import org.jspecify.annotations.NonNull;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;


/**
 * @author Victor Cannestro
 */
public class DefaultJsonLoader implements JsonLoader {

    @Override
    public <M extends Mappable> DraftTable at(@NonNull Path path, @NonNull Class<M> schema) {
        return load(path.toFile(), schema, ObjectMapperManager.getInstance().defaultMapper());
    }

    @Override
    public <M extends Mappable> DraftTable at(@NonNull URL url, @NonNull Class<M> schema) {
        File file = FileUtils.copyToTempDirectory(url);
        DraftTable draftTable = load(file, schema, ObjectMapperManager.getInstance().defaultMapper());
        FileUtils.cleanUpTemporaryFiles(file);
        return draftTable;
    }

    public <M extends Mappable> DraftTable load(@NonNull File file,
                                                @NonNull Class<M> schema,
                                                @NonNull ObjectMapper mapper) {
        try {
            return FlexibleDraftTable.create().fromObjects(
                    file.getName(),
                    mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, schema))
            );
        } catch (DatabindException databindException) {
            throw new IllegalArgumentException("The input JSON structure does not match structure expected for result type (or has other mismatch).", databindException);
        } catch (JacksonIOException ioException) {
            throw new IllegalArgumentException("A low-level I/ O problem (unexpected end-of-input, network error) occurred (passed through as-is without additional wrapping -- note that this is one case where DeserializationFeature. WRAP_EXCEPTIONS does NOT result in wrapping of exception even if enabled).", ioException);
        }
    }

}
