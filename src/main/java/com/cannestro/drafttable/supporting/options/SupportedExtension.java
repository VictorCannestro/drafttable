package com.cannestro.drafttable.supporting.options;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum SupportedExtension {

    CSV("csv"),
    GEOJSON("geojson"),
    JSON("json"),
    TSV("tsv"),
    TXT("txt");

    public final String type;


    @Override
    public String toString() {
        return type;
    }

}
