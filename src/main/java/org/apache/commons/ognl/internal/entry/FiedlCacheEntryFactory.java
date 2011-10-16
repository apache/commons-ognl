package org.apache.commons.ognl.internal.entry;

import org.apache.commons.ognl.internal.CacheException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Maurizio Cucchiara
 * Date: 10/15/11
 * Time: 9:47 AM
 */
public class FiedlCacheEntryFactory
    implements ClassCacheEntryFactory<Map<String, Field>>
{
    public Map<String, Field> create( Class<?> key )
        throws CacheException
    {
        Field[] declaredFields = key.getDeclaredFields( );
        HashMap<String, Field> result = new HashMap<String, Field>( declaredFields.length );
        for ( Field field : declaredFields )
        {
            result.put( field.getName( ), field );
        }
        return result;
    }
}

