package com.cannestro.drafttable.utils.helper;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.opencsv.bean.CsvBindByName;
import com.cannestro.drafttable.csv.beans.CsvBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonPropertyOrder({"type", "rate", "period", "workHours"})
public class Pay implements CsvBean {

    @CsvBindByName(column = "type") String type;
    @CsvBindByName(column = "rate") String rate;
    @CsvBindByName(column = "period") String period;
    @CsvBindByName(column = "workHours") String workHours;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
