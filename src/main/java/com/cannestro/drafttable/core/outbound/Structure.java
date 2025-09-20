package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.core.rows.Mappable;

import java.util.Map;


public record Structure(String columnName,
                        String type,
                        double nullCount) implements Mappable {

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "ColumnName", columnName,
                "Type", type,
                "NullCount", nullCount
        );
    }

}
