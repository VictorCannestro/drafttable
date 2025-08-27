package com.cannestro.drafttable.core.options;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum StatisticName {

    PERCENTILE_25("25%"),      // Estimated 25th percentile
    PERCENTILE_50("50%"),      // Estimated median
    PERCENTILE_75("75%"),      // Estimated 75th percentile
    MAX("max"),
    MEAN("mean"),              // Arithmetic mean
    MIN("min"),
    N("n"),
    STANDARD_DEVIATION("std"),
    VARIANCE("var");           // Bias-corrected sample variance (using n - 1 in the denominator)

    public final String shortHand;

}
