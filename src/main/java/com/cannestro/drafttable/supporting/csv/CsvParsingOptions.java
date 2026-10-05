package com.cannestro.drafttable.supporting.csv;

import com.cannestro.drafttable.core.rows.Mappable;


/**
 * Getter method contracts used in CSV reading to increase parsing success. Each represents either a hint or non-standard
 * assumption to be passed to the parser.
 *
 * @author Victor Cannestro
 */
public interface CsvParsingOptions extends CsvEssentials {

    boolean useStrictQuotes();

    boolean ignoreQuotations();

    boolean ignoreLeadingWhiteSpace();

    int skipLines();

    <T extends CsvBean & Mappable> Class<T> type();

}
