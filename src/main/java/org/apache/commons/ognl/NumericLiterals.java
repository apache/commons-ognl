package org.apache.commons.ognl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Numeric primitive literal string expressions.
 */
class NumericLiterals {

    private final Map<Class<? extends Number>, String> map = new HashMap<Class<? extends Number>, String>();

    NumericLiterals() {
        map.put( Integer.class, "" );
        map.put( Integer.TYPE, "" );
        map.put( Long.class, "l" );
        map.put( Long.TYPE, "l" );
        map.put( BigInteger.class, "d" );
        map.put( Float.class, "f" );
        map.put( Float.TYPE, "f" );
        map.put( Double.class, "d" );
        map.put( Double.TYPE, "d" );
        map.put( BigInteger.class, "d" );
        map.put( BigDecimal.class, "d" );
    }

    String get( Class<? extends Number> clazz ) {
        return map.get( clazz );
    }
}
