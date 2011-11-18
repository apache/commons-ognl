package org.apache.commons.ognl;

import java.util.HashMap;
import java.util.Map;

/**
 */
class PrimitiveTypes {

    private final Map<String, Class<?>> map = new HashMap<String, Class<?>>( 101 );

    PrimitiveTypes() {
        map.put( "boolean", Boolean.TYPE );
        map.put( "byte", Byte.TYPE );
        map.put( "short", Short.TYPE );
        map.put( "char", Character.TYPE );
        map.put( "int", Integer.TYPE );
        map.put( "long", Long.TYPE );
        map.put( "float", Float.TYPE );
        map.put( "double", Double.TYPE );
    }

    Class<?> get(String className) {
        return map.get(className);
    }
}
