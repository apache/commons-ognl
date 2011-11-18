package org.apache.commons.ognl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Constant strings for getting the primitive value of different native types on the generic {@link Number} object
 * interface. (or the less generic BigDecimal/BigInteger types)
 */
class NumericValues {

    private final Map<Class<?>, String> map = new HashMap<Class<?>, String>();

    NumericValues() {
        map.put( Double.class, "doubleValue()" );
        map.put( Float.class, "floatValue()" );
        map.put( Integer.class, "intValue()" );
        map.put( Long.class, "longValue()" );
        map.put( Short.class, "shortValue()" );
        map.put( Byte.class, "byteValue()" );
        map.put( BigDecimal.class, "doubleValue()" );
        map.put( BigInteger.class, "doubleValue()" );
        map.put( Boolean.class, "booleanValue()" );
    }

    String get( Class<?> cls ) {
        return map.get( cls );
    }
}
