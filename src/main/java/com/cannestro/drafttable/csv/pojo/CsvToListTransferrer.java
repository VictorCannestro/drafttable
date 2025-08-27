package com.cannestro.drafttable.csv.pojo;

import com.cannestro.drafttable.csv.beans.CsvBean;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;


@Setter
public class CsvToListTransferrer {

    private List<String[]> csvStringList;
    private List<CsvBean> csvList;


    public List<CsvBean> getCsvList() {
        if (!isNull(csvList)) {
            return csvList;
        }
        return new ArrayList<>();
    }

    public List<String[]> getCsvStringList() {
        if (!isNull(csvStringList)) {
            return csvStringList;
        }
        return new ArrayList<>();
    }

    public void addLine(String[] line) {
        if (isNull(this.csvList)) {
            this.csvStringList = new ArrayList<>();
        }
        this.csvStringList.add(line);
    }

}
