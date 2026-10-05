package com.cannestro.drafttable.supporting.csv;

import com.cannestro.drafttable.core.rows.Mappable;


/**
 * @author Victor Cannestro
 */
public interface CsvParsingOptions extends CsvEssentials {

    boolean useStrictQuotes();

    boolean ignoreQuotations();

    boolean ignoreLeadingWhiteSpace();

    int skipLines();

    <T extends CsvBean & Mappable> Class<T> type();

}
