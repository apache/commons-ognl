package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.internal.entry.PermissionCacheEntry;
import org.apache.commons.ognl.internal.entry.PermissionCacheEntryFactory;
import org.apache.commons.ognl.test.objects.Bean2;
import org.junit.Test;

import java.security.Permission;

import static junit.framework.Assert.assertNotNull;

/**
 * User: Maurizio Cucchiara
 * Date: 10/15/11
 * Time: 8:00 AM
 */
public class PermissionCacheTest
{
    ConcurrentHashMapCache<PermissionCacheEntry, Permission> cache =
        new ConcurrentHashMapCache<PermissionCacheEntry, Permission>( new PermissionCacheEntryFactory());

    @Test
    public void testGet( )
        throws Exception
    {

        getPermission( new PermissionCacheEntry( Bean2.class.getMethod( "getBean3" ) ) );
        getPermission( new PermissionCacheEntry( Bean2.class.getMethod( "getId" ) ) );
    }

    private void getPermission( PermissionCacheEntry entry )
        throws CacheException
    {
        Permission permission = cache.get( entry );
        assertNotNull( permission );
    }
}
