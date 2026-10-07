package com.cannestro.drafttable.supporting.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * Essentially getter method contracts that constitute fundamental building blocks that are shared across CSV reading
 * and writing.
 *
 * @author Victor Cannestro
 */
public interface CsvEssentials {

    char DEFAULT_DELIMITER = ',';
    char DEFAULT_ESCAPE_CHAR = '\n';
    char DEFAULT_QUOTE_CHAR = '\"';
    Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;


    /**
     * <p><b>Guarantees</b>: The character representing the delimiter will be returned. For example: {@code ','} or {@code '|'}.
     *
     * @return A {@code Character}
     */
    Character delimiter();

    /**
     * <p><b>Guarantees</b>: The character representing a break to the next line will be returned. For example: {@code '\n'} or {@code ';'}.
     *
     * @return A {@code Character}
     */
    Character escapeCharacter();

    /**
     * <p><b>Guarantees</b>: The character representing the beginning/end of quotations will be returned. For example:
     * {@code '\''} or {@code '\"'}.
     *
     * @return A {@code Character}
     */
    Character quoteCharacter();

    /**
     * <p><b>Guarantees</b>: The charset to be used in encoding or decoding operations will be returned. For example:
     * {@code StandardCharsets.UTF_8} or {@code StandardCharsets.UTF_16}.
     *
     * @return A {@code Character}
     */
    Charset charset();

}
