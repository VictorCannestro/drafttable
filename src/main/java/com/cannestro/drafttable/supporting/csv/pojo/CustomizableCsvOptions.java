package com.cannestro.drafttable.supporting.csv.pojo;

import com.cannestro.drafttable.core.inbound.CsvOptions;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import lombok.Builder;

import static java.util.Objects.isNull;


/**
 *
 * @param delimiter defaults to {@code ','}
 * @param escapeCharacter defaults to {@code '\n'}
 * @param quoteCharacter defaults to {@code '\"'}
 * @param useStrictQuotes defaults to {@code false}
 * @param ignoreLeadingWhiteSpace defaults to {@code false}
 * @param ignoreQuotations defaults to {@code false}
 * @param skipLines defaults to {@code 0}
 * @param type defaults to {@code null}
 */
@Builder
public record CustomizableCsvOptions(Character delimiter,
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


    public CustomizableCsvOptions {
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

    public static CustomizableCsvOptions allDefaults() {
        return CustomizableCsvOptions.builder().build();
    }

}
