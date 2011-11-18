package org.apache.commons.ognl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 */
class PrimitiveDefaults {

    private final Map<Class<?>, Object> map = new HashMap<Class<?>, Object>( 20 );

    PrimitiveDefaults() {
        map.put( Boolean.TYPE, Boolean.FALSE );
        map.put( Boolean.class, Boolean.FALSE );
        map.put( Byte.TYPE, (byte) 0 );
        map.put( Byte.class, (byte) 0 );
        map.put( Short.TYPE, (short) 0 );
        map.put( Short.class, (short) 0 );
        map.put( Character.TYPE, (char) 0 );
        map.put( Integer.TYPE, 0 );
        map.put( Long.TYPE, 0L );
        map.put( Float.TYPE, 0.0f );
        map.put( Double.TYPE, 0.0 );

        map.put( BigInteger.class, new BigInteger( "0" ) );
        map.put( BigDecimal.class, new BigDecimal( 0.0 ) );
    }

    Object get( Class<?> cls ) {
        return map.get( cls );
    }
}
