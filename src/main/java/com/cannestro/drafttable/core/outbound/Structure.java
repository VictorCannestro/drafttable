package com.cannestro.drafttable.core.outbound;

import com.cannestro.drafttable.core.rows.Mappable;
import org.jspecify.annotations.NonNull;

import java.util.Map;


public record Structure(@NonNull String columnName,
                        @NonNull String type,
                        int nullCount) implements Mappable {

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "ColumnName", columnName,
                "Type", type,
                "NullCount", nullCount
        );
    }

}
