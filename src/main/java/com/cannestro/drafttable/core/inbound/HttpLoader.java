package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.supporting.http.HttpRequestWrapper;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Function;


public interface HttpLoader {

    <M extends Mappable> DraftTable getJsonArray(@NonNull Class<M> schema, @NonNull HttpRequestWrapper options);

    <A, M extends Mappable> DraftTable getAs(@NonNull Class<A> schema,
                                             @NonNull Function<A, List<M>> selector,
                                             @NonNull HttpRequestWrapper options);

}
