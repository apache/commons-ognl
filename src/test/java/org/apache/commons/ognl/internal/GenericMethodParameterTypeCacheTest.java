package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.internal.entry.GenericMethodParameterTypeCacheEntry;
import org.apache.commons.ognl.internal.entry.GenericMethodParameterTypeFactory;
import org.apache.commons.ognl.test.objects.GameGeneric;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * User: Maurizio Cucchiara
 * Date: 10/16/11
 * Time: 9:44 AM
 */
public class GenericMethodParameterTypeCacheTest
{
    private ConcurrentHashMapCache<GenericMethodParameterTypeCacheEntry, Class<?>[]> cache =
        new ConcurrentHashMapCache<GenericMethodParameterTypeCacheEntry, Class<?>[]>( new GenericMethodParameterTypeFactory( ) );

    @Test
    public void testGet( )
        throws NoSuchMethodException, CacheException
    {
        Method m = GameGeneric.class.getMethod( "setIds", Serializable[].class );
        Class type = GameGeneric.class;
        Class<?>[] types = cache.get( new GenericMethodParameterTypeCacheEntry( m, type ) );

        Assert.assertEquals( 1, types.length );
        Assert.assertEquals( Long[].class, types[0] );
    }

}
