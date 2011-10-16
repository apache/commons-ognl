package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.internal.entry.MethodPermCacheEntryFactory;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.security.Permission;

/**
 * User: Maurizio Cucchiara
 * Date: 10/17/11
 * Time: 12:13 AM
 */
public class MethodPermCacheTest
{
    private SecurityManager securityManager =new DummySecurityManager();

    Cache<Method, Boolean> cache =
        new ConcurrentHashMapCache<Method, Boolean>( new MethodPermCacheEntryFactory( securityManager ) );
    @Test
    public void testGetPublicMethod( )
        throws CacheException, NoSuchMethodException
    {
        Method method = Root.class.getMethod( "getArray");
        Assert.assertTrue( cache.get( method ) );
    }

    private class DummySecurityManager
        extends SecurityManager
    {
        @Override
        public void checkPermission( Permission perm )
        {
        }
    }
}
