package com.cannestro.drafttable.helper;

import com.cannestro.drafttable.core.rows.Mappable;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.opencsv.bean.CsvBindByName;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;


@Getter
@EqualsAndHashCode
@AllArgsConstructor
@RequiredArgsConstructor
@JsonPropertyOrder({"type", "rate", "period", "workHours"})
public class PayDetails implements CsvBean, Mappable {

    @CsvBindByName(column = "type") String type;
    @CsvBindByName(column = "rate") String rate;
    @CsvBindByName(column = "period") String period;
    @CsvBindByName(column = "workHours") String workHours;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "type", type,
                "rate", rate,
                "period", period,
                "workHours", workHours
        );
    }
}
