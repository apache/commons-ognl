package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.internal.entry.DeclaredMethodCacheEntry;
import org.apache.commons.ognl.internal.entry.DeclaredMethodCacheEntryFactory;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * User: Maurizio Cucchiara
 * Date: 10/15/11
 * Time: 9:18 AM
 */
public class DeclaredMethodCacheTest
{
    Cache<DeclaredMethodCacheEntry, Map<String, List<Method>>> cache =
        new ConcurrentHashMapCache<DeclaredMethodCacheEntry, Map<String, List<Method>>>(new DeclaredMethodCacheEntryFactory( ) );

    @Test
    public void testStaticGet( )
        throws Exception
    {
        Map<String, List<Method>> methods = cache.get( new DeclaredMethodCacheEntry( Root.class, DeclaredMethodCacheEntry.MethodType.STATIC) );
        assertNotNull( methods );
        assertTrue( methods.containsKey( "getStaticInt" ) );
    }

    @Test
    public void testNonStaticGet( )
        throws Exception
    {
        Map<String, List<Method>> methods = cache.get( new DeclaredMethodCacheEntry( Root.class, DeclaredMethodCacheEntry.MethodType.NON_STATIC ) );
        assertNotNull( methods );
        assertTrue( methods.containsKey( "format" ) );
    }

}
