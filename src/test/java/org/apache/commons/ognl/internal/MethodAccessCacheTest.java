package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.internal.entry.MethodAccessCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.MethodAccessEntryValue;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * User: Maurizio Cucchiara
 * Date: 10/17/11
 * Time: 12:59 AM
 */
public class MethodAccessCacheTest
{
    private Cache<Method, MethodAccessEntryValue> cache =
        new ConcurrentHashMapCache<Method, MethodAccessEntryValue>( new MethodAccessCacheEntryFactory( ) );

    @Test
    public void testGetAccessibleNonPublicMethod( )
        throws Exception
    {
        Method method = Root.class.getDeclaredMethod( "getPrivateAccessorIntValue3" );
        MethodAccessEntryValue methodAccessValue = cache.get( method );
        Assert.assertTrue( methodAccessValue.isNotPublic( ) );
        Assert.assertFalse( methodAccessValue.isAccessible());
    }

    @Test
    public void testGetNotAccessibleNonPublicMethod( )
        throws Exception
    {
        Method method = Root.class.getDeclaredMethod( "getPrivateAccessorIntValue3" );
        method.setAccessible( true );
        MethodAccessEntryValue methodAccessValue = cache.get( method );
        Assert.assertTrue( methodAccessValue.isNotPublic( ) );
        Assert.assertTrue( methodAccessValue.isAccessible());
    }

    @Test
    public void testGetPublicMethod( )
        throws NoSuchMethodException, CacheException
    {
        Method method = Root.class.getDeclaredMethod( "getArray" );
        MethodAccessEntryValue methodAccessValue = cache.get( method );
        Assert.assertFalse( methodAccessValue.isNotPublic( ) );
        Assert.assertTrue( methodAccessValue.isAccessible());
    }

}
