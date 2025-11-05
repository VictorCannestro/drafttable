package com.cannestro.drafttable.helper;

import com.cannestro.drafttable.core.rows.Mappable;

import java.time.LocalDate;
import java.util.Map;


public record DailyHireCount(int n, LocalDate timeStamp) implements Mappable {

    @Override
    public Map<String, ?> asMap() {
        return Map.of("n", n, "timeStamp", timeStamp);
    }

}
