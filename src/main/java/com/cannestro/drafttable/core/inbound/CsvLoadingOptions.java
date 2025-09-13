package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.supporting.csv.CsvBean;


public interface CsvLoadingOptions {

    Character delimiter();

    Character escapeCharacter();

    Character quoteCharacter();

    Boolean useStrictQuotes();

    Boolean ignoreQuotations();

    Boolean ignoreLeadingWhiteSpace();

    Boolean ignoreEmptyLines();

    Class<? extends CsvBean> type();

}
