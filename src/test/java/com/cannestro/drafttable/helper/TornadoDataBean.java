package com.cannestro.drafttable.helper;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import com.cannestro.drafttable.supporting.utils.MapBuilder;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;


@Data
public class TornadoDataBean implements CsvBean, Mappable {

    @CsvBindByName(column = "Date")
    @CsvDate("yyyy-MM-dd")
    private LocalDate date;

    @CsvBindByName(column = "Time")
    @CsvDate("HH:mm:ss")
    private LocalTime time;

    @CsvBindByName(column = "State")
    private String state;

    @CsvBindByName(column = "State No")
    private String stateNumber;

    @CsvBindByName(column = "Scale")
    private double scale;

    @CsvBindByName(column = "Injuries")
    private double injuries;

    @CsvBindByName(column = "Fatalities")
    private double fatalities;

    @CsvBindByName(column = "Start Lat")
    private double startLat;

    @CsvBindByName(column = "Start Lon")
    private double startLon;

    @CsvBindByName(column = "Length")
    private double length;

    @CsvBindByName(column = "Width")
    private double width;

    @Override
    public Map<String, ?> asMap() {
        return MapBuilder.with()
                .entry("date", date)
                .entry("time", time)
                .entry("state", state)
                .entry("stateNumber", stateNumber)
                .entry("scale", scale)
                .entry("injuries", injuries)
                .entry("fatalities", fatalities)
                .entry("startLat", startLat)
                .entry("startLon", startLon)
                .entry("length", length)
                .entry("width", width)
                .asMap();
    }
}
