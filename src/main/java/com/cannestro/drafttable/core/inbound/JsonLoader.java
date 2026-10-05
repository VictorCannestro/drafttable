package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.nio.file.Path;


/**
 * @author Victor Cannestro
 */
public interface JsonLoader {

    /**
     * <p><b>Requires</b>: The path must exist and point to an accessible, well formed JSON resource. A schema class
     * representing the type and field name expectations of the JSON must be provided. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created. Columns names will be mapped
     *                       to the root level JSON fields. It may be empty. </p>
     *
     * @param path A valid path to the CSV resource, e.g., {@code "desktop/data.json"}
     * @param schema Constitutes type and name expectations
     * @return A new {@code DraftTable} instance with typed data
     */
    <M extends Mappable> DraftTable at(@NonNull Path path, @NonNull Class<M> schema);

    /**
     * <p><b>Requires</b>: The URI must exist and point to an accessible, well formed JSON resource. A schema class
     * representing the type and field name expectations of the JSON must be provided. </p>
     * <p><b>Guarantees</b>: A new {@code DraftTable} instance will be created. Columns names will be mapped
     *                       to the root level JSON fields. It may be empty. </p>
     *
     * @param uri A valid URI to the CSV resource, e.g., {@code "http://foo.com/bar/data.json"}
     * @param schema Constitutes type and name expectations
     * @return A new {@code DraftTable} instance with typed data
     */
    <M extends Mappable> DraftTable at(@NonNull URI uri, @NonNull Class<M> schema);

}
