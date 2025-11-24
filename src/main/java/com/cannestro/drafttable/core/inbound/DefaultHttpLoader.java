package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.core.tables.DraftTable;
import com.cannestro.drafttable.core.tables.FlexibleDraftTable;
import com.cannestro.drafttable.supporting.http.HttpRequestSender;
import com.cannestro.drafttable.supporting.http.HttpRequestWrapper;
import com.cannestro.drafttable.supporting.json.ObjectMapperManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Function;


/**
 * @author Victor Cannestro
 */
@Getter
@AllArgsConstructor
public class DefaultHttpLoader implements HttpLoader {

    private final HttpClient client;


    @Override
    public <M extends Mappable> DraftTable getJsonArray(@NonNull Class<M> schema, @NonNull HttpRequestWrapper options) {
        HttpResponse<String> response = HttpRequestSender.sendSynchronously().apply(getClient(), options.constructGetRequest());
        return FlexibleDraftTable.create().fromObjects(
                ObjectMapperManager.getInstance()
                        .defaultMapper()
                        .readerForListOf(schema)
                        .readValue(response.body())
        );
    }

    @Override
    public <A, M extends Mappable> DraftTable getAs(@NonNull Class<A> schema,
                                                    @NonNull Function<A, List<M>> selector,
                                                    @NonNull HttpRequestWrapper options) {
        HttpResponse<String> response = HttpRequestSender.sendSynchronously().apply(getClient(), options.constructGetRequest());
        A object = ObjectMapperManager.getInstance().defaultMapper().readValue(response.body(), schema);
        return FlexibleDraftTable.create().fromObjects(
                selector.apply(object)
        );
    }

}
