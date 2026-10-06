package com.cannestro.drafttable.supporting.csv;

import com.cannestro.drafttable.core.rows.Mappable;


/**
 * Getter method contracts used in CSV reading to increase parsing success. Each represents either a hint or non-standard
 * assumption to be passed to the parser.
 *
 * @author Victor Cannestro
 */
public interface CsvParsingOptions extends CsvEssentials {

    /**
     * <p><b>Guarantees</b>: When true, characters outside the quotes will be ignored. </p>
     *
     * @return true or false
     */
    boolean useStrictQuotes();

    /**
     * <p><b>Guarantees</b>: When true, quotations will be ignored. </p>
     *
     * @return true or false
     */
    boolean ignoreQuotations();

    /**
     * <p><b>Guarantees</b>: When true, white space in front of a quote in a field is ignored. </p>
     *
     * @return true or false
     */
    boolean ignoreLeadingWhiteSpace();

    /**
     * <p><b>Guarantees</b>: A specified number of lines in the CSV that will be skipped, starting from the top.
     * Typically used when a dataset contains one or more comment rows. </p>
     *
     * @return An integer within [0, Integer.MAX)
     */
    int skipLines();

    /**
     * Enriches the parser with context around column-to-field name mappings and types.
     *
     * @return A schema class
     * @param <T> Any {@code CsvBean} that is {@code Mappable}
     */
    <T extends CsvBean & Mappable> Class<T> type();

}
