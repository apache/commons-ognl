package org.apache.commons.ognl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Constant strings for casting different primitive types.
 */
class NumericCasts {

    private final Map<Class<? extends Number>, String> map = new HashMap<Class<? extends Number>, String>();

    NumericCasts() {
        map.put( Double.class, "(double)" );
        map.put( Float.class, "(float)" );
        map.put( Integer.class, "(int)" );
        map.put( Long.class, "(long)" );
        map.put( BigDecimal.class, "(double)" );
        map.put( BigInteger.class, "" );
    }

    String get( Class<? extends Number> cls ) {
        return map.get( cls );
    }
}
