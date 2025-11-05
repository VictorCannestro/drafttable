package com.cannestro.drafttable.supporting.csv;


public interface CsvEssentials {

    char DEFAULT_DELIMITER = ',';
    char DEFAULT_ESCAPE_CHAR = '\n';
    char DEFAULT_QUOTE_CHAR = '\"';


    Character delimiter();

    Character escapeCharacter();

    Character quoteCharacter();

}
