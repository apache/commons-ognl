package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.internal.entry.FiedlCacheCacheEntryFactory;
import org.apache.commons.ognl.test.objects.Bean2;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * User: Maurizio Cucchiara
 * Date: 10/15/11
 * Time: 8:00 AM
 */
public class FieldCacheTest
{
    ConcurrentHashMapCache<Class<?>, Map<String,Field>> cache =
        new ConcurrentHashMapCache<Class<?>, Map<String,Field>>( new FiedlCacheCacheEntryFactory());

    @Test
    public void testGet( )
        throws Exception
    {
        Map<String, Field> d = getFields( Bean2.class );
        assertTrue( d.containsKey( "bean3" ) );
        assertTrue( d.containsKey( "_pageBreakAfter" ) );
    }

    private Map<String, Field> getFields( Class<?> entry )
        throws CacheException
    {
        Map<String, Field> descriptor = cache.get( entry );
        assertNotNull( descriptor );
        return descriptor;
    }
}
