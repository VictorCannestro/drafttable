package com.cannestro.drafttable.supporting.utils.helper;

import com.cannestro.drafttable.supporting.csv.CsvBean;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;


@Data
public class TornadoDataBean implements CsvBean {

    @CsvBindByName(column = "Date")       private String date;
    @CsvBindByName(column = "Time")       private String time;
    @CsvBindByName(column = "State")      private String state;
    @CsvBindByName(column = "State No")   private String stateNumber;
    @CsvBindByName(column = "Scale")      private double scale;
    @CsvBindByName(column = "Injuries")   private double injuries;
    @CsvBindByName(column = "Fatalities") private double fatalities;
    @CsvBindByName(column = "Start Lat")  private double startLat;
    @CsvBindByName(column = "Start Lon")  private double startLon;
    @CsvBindByName(column = "Length")     private double length;
    @CsvBindByName(column = "Width")      private double width;

}
