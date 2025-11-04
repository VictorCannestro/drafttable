package com.cannestro.drafttable.supporting.csv.pojo;

import com.cannestro.drafttable.core.inbound.CsvOptions;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import lombok.Builder;

import static java.util.Objects.isNull;


@Builder
public record DefaultCsvOptions(Character delimiter,
                                Character escapeCharacter,
                                Character quoteCharacter,
                                boolean useStrictQuotes,
                                boolean ignoreLeadingWhiteSpace,
                                boolean ignoreQuotations,
                                int skipLines,
                                Class<? extends CsvBean> type) implements CsvOptions {

    public static final char COMMA = ',';
    public static final char NEW_LINE = '\n';
    public static final char QUOTE = '\"';


    public DefaultCsvOptions {
        if (isNull(delimiter)) {
            delimiter = COMMA;
        }
        if (isNull(escapeCharacter)) {
            escapeCharacter = NEW_LINE;
        }
        if (isNull(quoteCharacter)) {
            quoteCharacter = QUOTE;
        }
    }

}
