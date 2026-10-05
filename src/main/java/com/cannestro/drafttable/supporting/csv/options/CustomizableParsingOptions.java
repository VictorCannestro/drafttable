package com.cannestro.drafttable.supporting.csv.options;

import com.cannestro.drafttable.supporting.csv.CsvEssentials;
import com.cannestro.drafttable.supporting.csv.CsvParsingOptions;
import com.cannestro.drafttable.supporting.csv.CsvBean;
import lombok.Builder;

import java.nio.charset.Charset;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 *
 * @param delimiter defaults to {@code ','}
 * @param escapeCharacter defaults to {@code '\n'}
 * @param quoteCharacter defaults to {@code '\"'}
 * @param charset defaults to {@code StandardCharsets.UTF_8}
 * @param useStrictQuotes defaults to {@code false}
 * @param ignoreLeadingWhiteSpace defaults to {@code false}
 * @param ignoreQuotations defaults to {@code false}
 * @param skipLines defaults to {@code 0}
 * @param type defaults to {@code null}
 */
@Builder
public record CustomizableParsingOptions(Character delimiter,
                                         Character escapeCharacter,
                                         Character quoteCharacter,
                                         Charset charset,
                                         boolean useStrictQuotes,
                                         boolean ignoreLeadingWhiteSpace,
                                         boolean ignoreQuotations,
                                         int skipLines,
                                         Class<? extends CsvBean> type) implements CsvParsingOptions {

    public CustomizableParsingOptions {
        if (isNull(delimiter)) {
            delimiter = CsvEssentials.DEFAULT_DELIMITER;
        }
        if (isNull(escapeCharacter)) {
            escapeCharacter = CsvEssentials.DEFAULT_ESCAPE_CHAR;
        }
        if (isNull(quoteCharacter)) {
            quoteCharacter = CsvEssentials.DEFAULT_QUOTE_CHAR;
        }
        if (isNull(charset)) {
            charset = CsvEssentials.DEFAULT_CHARSET;
        }
    }

    public static CustomizableParsingOptions allDefaults() {
        return CustomizableParsingOptions.builder().build();
    }

}
