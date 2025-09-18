package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.core.rows.Mappable;

import java.util.Map;


public record StatisticalDescription(String metric, Double value) implements Mappable {

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "METRIC", metric,
                "VALUE", value
        );
    }

}
