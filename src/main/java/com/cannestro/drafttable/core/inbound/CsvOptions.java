package com.cannestro.drafttable.core.inbound;

import com.cannestro.drafttable.core.rows.Mappable;
import com.cannestro.drafttable.supporting.csv.CsvBean;


public interface CsvOptions {

    Character delimiter();

    Character escapeCharacter();

    Character quoteCharacter();

    boolean useStrictQuotes();

    boolean ignoreQuotations();

    boolean ignoreLeadingWhiteSpace();

    int skipLines();

    <T extends CsvBean & Mappable> Class<T> type();

}
