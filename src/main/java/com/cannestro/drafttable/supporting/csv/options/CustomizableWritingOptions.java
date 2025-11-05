package com.cannestro.drafttable.supporting.csv.options;

import com.cannestro.drafttable.supporting.csv.CsvEssentials;
import com.cannestro.drafttable.supporting.csv.CsvWritingOptions;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.isNull;


/**
 *
 * @param delimiter defaults to {@code ','}
 * @param escapeCharacter defaults to {@code '\n'}
 * @param quoteCharacter defaults to {@code '\"'}
 * @param lineEnder defaults to {@code "\n"}
 * @param fillerValue defaults to {@code ""}
 */
@Builder
public record CustomizableWritingOptions(Character delimiter,
                                         Character escapeCharacter,
                                         Character quoteCharacter,
                                         String lineEnder,
                                         String fillerValue) implements CsvWritingOptions {

    public CustomizableWritingOptions {
        if (isNull(delimiter)) {
            delimiter = CsvEssentials.DEFAULT_DELIMITER;
        }
        if (isNull(escapeCharacter)) {
            escapeCharacter = CsvEssentials.DEFAULT_ESCAPE_CHAR;
        }
        if (isNull(quoteCharacter)) {
            quoteCharacter = CsvEssentials.DEFAULT_QUOTE_CHAR;
        }
        if (isNull(lineEnder)) {
            lineEnder = String.valueOf(CsvEssentials.DEFAULT_ESCAPE_CHAR);
        }
        if (isNull(fillerValue)) {
            fillerValue = StringUtils.EMPTY;
        }
    }

    public static CustomizableWritingOptions allDefaults() {
        return CustomizableWritingOptions.builder().build();
    }

}
