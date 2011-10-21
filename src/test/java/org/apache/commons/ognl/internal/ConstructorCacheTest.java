package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.internal.entry.CacheEntryFactory;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class ConstructorCacheTest
{
    private int count;
    ClassCache<List<Constructor<?>>> cache = new ConcurrentHashMapClassCache<List<Constructor<?>>>(new CacheEntryFactory<Class<?>, List<Constructor<?>>>( )
    {
        public List<Constructor<?>> create( Class<?> key )
            throws CacheException
        {
            count++;
            return Arrays.asList( key.getConstructors( ) );
        }
    });

    @Test
    public void testGet()
        throws CacheException
    {
        List<Constructor<?>> constructors = cache.get( Root.class );
        assertNotNull( constructors );
        cache.get( Root.class );
        assertEquals( 1, count);
    }
}
