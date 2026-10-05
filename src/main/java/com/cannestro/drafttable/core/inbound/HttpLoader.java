package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.supporting.http.HttpRequestWrapper;
import com.cannestro.drafttable.supporting.http.HttpResponseWrapper;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Function;


/**
 * @author Victor Cannestro
 */
public interface HttpLoader {

    <M extends Mappable> DraftTable getJsonArray(@NonNull Class<M> schema,
                                                 @NonNull HttpRequestWrapper requestWrapper,
                                                 @NonNull HttpResponseWrapper responseWrapper);

    <A, M extends Mappable> DraftTable getAs(@NonNull Class<A> schema,
                                             @NonNull Function<? super A, List<M>> selector,
                                             @NonNull HttpRequestWrapper requestWrapper,
                                             @NonNull HttpResponseWrapper responseWrapper);

    default <M extends Mappable> DraftTable getJsonArray(@NonNull Class<M> schema, @NonNull HttpRequestWrapper requestWrapper) {
        return getJsonArray(schema, requestWrapper, HttpResponseWrapper.allDefaults());
    }
    
    default <A, M extends Mappable> DraftTable getAs(@NonNull Class<A> schema,
                                                     @NonNull Function<? super A, List<M>> selector,
                                                     @NonNull HttpRequestWrapper requestWrapper) {
        return getAs(schema, selector, requestWrapper, HttpResponseWrapper.allDefaults());
    }

}
