package com.cannestro.drafttable.supporting.csv.pojo;

import com.cannestro.drafttable.core.inbound.CsvOptions;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import lombok.Builder;


@Builder
public record DefaultCsvOptions(Character delimiter,
                                Character escapeCharacter,
                                Character quoteCharacter,
                                Boolean useStrictQuotes,
                                Boolean ignoreLeadingWhiteSpace,
                                Boolean ignoreQuotations,
                                Boolean ignoreEmptyLines,
                                Class<? extends CsvBean> type) implements CsvOptions {}
