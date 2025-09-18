package com.cannestro.drafttable.core.rows;

import java.util.Map;


/**
 * @author Victor Cannestro
 */
@FunctionalInterface
public interface Mappable {

    Map<String, ?> asMap();

}
