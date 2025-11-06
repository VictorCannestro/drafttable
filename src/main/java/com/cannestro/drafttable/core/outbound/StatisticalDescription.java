package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.core.rows.Mappable;
import org.jspecify.annotations.NonNull;

import java.util.Map;


public record StatisticalDescription(@NonNull String metric, @NonNull Double value) implements Mappable {

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "METRIC", metric,
                "VALUE", value
        );
    }

}
