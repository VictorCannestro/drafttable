package com.cannestro.drafttable.supporting.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public interface CsvEssentials {

    char DEFAULT_DELIMITER = ',';
    char DEFAULT_ESCAPE_CHAR = '\n';
    char DEFAULT_QUOTE_CHAR = '\"';
    Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;


    Character delimiter();

    Character escapeCharacter();

    Character quoteCharacter();

    Charset charset();

}
