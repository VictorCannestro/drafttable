package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.nio.file.Path;


public interface JsonLoader {

    <T extends Mappable> DraftTable at(@NonNull Path path, @NonNull Class<T> schema);

    <T extends Mappable> DraftTable at(@NonNull URL url, @NonNull Class<T> schema);

}
