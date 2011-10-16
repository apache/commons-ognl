package org.apache.commons.ognl.internal.entry;

import org.apache.commons.ognl.internal.CacheException;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/**
 * User: Maurizio Cucchiara
 * Date: 10/16/11
 * Time: 8:51 AM
 */
public class ConstructorCacheEntryFactory
    implements CacheEntryFactory<Class<?>, List<Constructor<?>>>
{
    public List<Constructor<?>> create( Class<?> key )
        throws CacheException
    {
        return Arrays.asList( key.getConstructors( ) );
    }
}

