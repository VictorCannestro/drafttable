package com.cannestro.drafttable.supporting.utils.helper;

import com.cannestro.drafttable.supporting.csv.CsvBean;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;


@Data
public class TornadoDataBean implements CsvBean {

    @CsvBindByName(column = "Date") String date;
    @CsvBindByName(column = "Time") String time;
    @CsvBindByName(column = "State") String state;
    @CsvBindByName(column = "State No") double stateNumber;
    @CsvBindByName(column = "Scale") double scale;
    @CsvBindByName(column = "Injuries") double injuries;
    @CsvBindByName(column = "Fatalities") double fatalities;
    @CsvBindByName(column = "Start Lat") double startLat;
    @CsvBindByName(column = "Start Lon") double startLon;
    @CsvBindByName(column = "Length") double length;
    @CsvBindByName(column = "Width") double width;

}
