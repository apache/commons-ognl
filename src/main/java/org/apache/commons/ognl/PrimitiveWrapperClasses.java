package org.apache.commons.ognl;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Used to provide primitive type equivalent conversions into and out of native / object types.
 */
class PrimitiveWrapperClasses {

    private Map<Class<?>, Class<?>> map = new IdentityHashMap<Class<?>, Class<?>>();

    PrimitiveWrapperClasses() {
        map.put( Boolean.TYPE, Boolean.class );
        map.put( Boolean.class, Boolean.TYPE );
        map.put( Byte.TYPE, Byte.class );
        map.put( Byte.class, Byte.TYPE );
        map.put( Character.TYPE, Character.class );
        map.put( Character.class, Character.TYPE );
        map.put( Short.TYPE, Short.class );
        map.put( Short.class, Short.TYPE );
        map.put( Integer.TYPE, Integer.class );
        map.put( Integer.class, Integer.TYPE );
        map.put( Long.TYPE, Long.class );
        map.put( Long.class, Long.TYPE );
        map.put( Float.TYPE, Float.class );
        map.put( Float.class, Float.TYPE );
        map.put( Double.TYPE, Double.class );
        map.put( Double.class, Double.TYPE );
    }

    Class<?> get( Class<?> cls ) {
        return map.get( cls );
    }
}
