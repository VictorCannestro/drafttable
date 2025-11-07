package com.cannestro.drafttable.core.aggregations;

import com.cannestro.drafttable.core.tables.DraftTable;


import static java.util.Objects.isNull;


/**
 * Cross-Tabulation ("Crosstab" for short) or Contingency Tables
 *
 * @author Victor Cannestro
 */
public record Crosstab(DraftTable draftTable) {

    public Crosstab {
        if (isNull(draftTable)) {
            throw new IllegalArgumentException("Cannot cross tabulate a null object");
        }
    }

}
