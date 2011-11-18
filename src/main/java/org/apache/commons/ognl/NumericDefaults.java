package org.apache.commons.ognl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 */
class NumericDefaults {

    private final Map<Class<?>, Object> NUMERIC_DEFAULTS = new HashMap<Class<?>, Object>();

    NumericDefaults() {
        NUMERIC_DEFAULTS.put( Boolean.class, Boolean.FALSE );
        NUMERIC_DEFAULTS.put( Byte.class, (byte) 0 );
        NUMERIC_DEFAULTS.put( Short.class, (short) 0 );
        NUMERIC_DEFAULTS.put( Character.class, (char) 0 );
        NUMERIC_DEFAULTS.put( Integer.class, 0 );
        NUMERIC_DEFAULTS.put( Long.class, 0L );
        NUMERIC_DEFAULTS.put( Float.class, 0.0f );
        NUMERIC_DEFAULTS.put( Double.class, 0.0 );

        NUMERIC_DEFAULTS.put( BigInteger.class, new BigInteger( "0" ) );
        NUMERIC_DEFAULTS.put( BigDecimal.class, new BigDecimal( 0.0 ) );
    }

    Object get( Class<?> cls ) {
        return NUMERIC_DEFAULTS.get( cls );
    }
}
