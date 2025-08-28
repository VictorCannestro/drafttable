package com.cannestro.drafttable.core;

import com.cannestro.drafttable.core.options.SortingOrderType;

import java.util.function.Function;
import java.util.stream.Collector;


public interface ColumnGrouping {

    DraftTable byValueCounts(SortingOrderType orderType);

    <B, R> DraftTable byCountsOf(Function<? super B, ? extends R> mapping);

    <R, A, D> DraftTable byValuesUsing(Collector<? super R, A, D> aggregation);

    <B, R, A, D> DraftTable by(Function<? super B, ? extends R> mapping, Collector<? super B, A, D> aggregation);


    default DraftTable byValueCounts() {
        return byCountsOf(Function.identity());
    }

}
