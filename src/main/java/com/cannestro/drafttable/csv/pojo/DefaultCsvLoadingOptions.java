package com.cannestro.drafttable.csv.pojo;

import com.cannestro.drafttable.core.inbound.CsvLoadingOptions;
import com.cannestro.drafttable.csv.beans.CsvBean;
import lombok.Builder;


@Builder
public record DefaultCsvLoadingOptions(Character delimiter,
                                       Character escapeCharacter,
                                       Character quoteCharacter,
                                       Boolean useStrictQuotes,
                                       Boolean ignoreLeadingWhiteSpace,
                                       Boolean ignoreQuotations,
                                       Boolean ignoreEmptyLines,
                                       Class<? extends CsvBean> type) implements CsvLoadingOptions {}
