package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.http.HttpExchanger;
import com.cannestro.drafttable.supporting.http.HttpRequestWrapper;
import com.cannestro.drafttable.supporting.http.HttpResponseWrapper;
import com.cannestro.drafttable.supporting.json.ObjectMapperManager;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Function;


/**
 * @author Victor Cannestro
 */
@AllArgsConstructor
public class DefaultHttpLoader implements HttpLoader {

    private final HttpClient client;


    @Override
    public <M extends Mappable> DraftTable getJsonArray(@NonNull Class<M> schema,
                                                        @NonNull HttpRequestWrapper requestWrapper,
                                                        @NonNull HttpResponseWrapper responseWrapper) {
        HttpExchanger requestSender = new HttpExchanger(requestWrapper, responseWrapper);
        HttpResponse<String> response = requestSender.sendSynchronouslyUsing(this.client);
        return FlexibleDraftTable.create().fromObjects(
                ObjectMapperManager.getInstance()
                        .defaultMapper()
                        .readerForListOf(schema)
                        .readValue(response.body())
        );
    }

    @Override
    public <A, M extends Mappable> DraftTable getAs(@NonNull Class<A> schema,
                                                    @NonNull Function<? super A, List<M>> selector,
                                                    @NonNull HttpRequestWrapper requestWrapper,
                                                    @NonNull HttpResponseWrapper responseWrapper) {
        HttpExchanger requestSender = new HttpExchanger(requestWrapper, responseWrapper);
        HttpResponse<String> response = requestSender.sendSynchronouslyUsing(this.client);
        return FlexibleDraftTable.create().fromObjects(
                selector.apply(ObjectMapperManager.getInstance().defaultMapper().readValue(response.body(), schema))
        );
    }

}
