package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.csv.beans.CsvBean;


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
