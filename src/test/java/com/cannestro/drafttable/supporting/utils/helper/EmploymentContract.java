package com.cannestro.drafttable.supporting.utils.helper;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.opencsv.bean.CsvBindByName;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;
import java.util.Map;


@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@RequiredArgsConstructor
@JsonPropertyOrder({"type", "exemptInd", "payDetails", "effectiveDate"})
public class EmploymentContract implements CsvBean, Mappable {

    @CsvBindByName(column = "type") String type;
    @CsvBindByName(column = "exempt") String exemptInd;
    @CsvBindByName(column = "pay_details") PayDetails payDetails;
    @CsvBindByName(column = "effective_date") LocalDate effectiveDate;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    @Override
    public Map<String, ?> asMap() {
        return Map.of(
                "type", type,
                "exempt", exemptInd,
                "payDetails", payDetails,
                "effectiveDate", effectiveDate
        );
    }
}
