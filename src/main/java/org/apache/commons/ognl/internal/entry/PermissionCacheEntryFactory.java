package org.apache.commons.ognl.internal.entry;

import org.apache.commons.ognl.OgnlInvokePermission;
import org.apache.commons.ognl.internal.CacheException;

import java.security.Permission;

/**
 * User: Maurizio Cucchiara
 * Date: 10/14/11
 * Time: 9:53 PM
 */
public class PermissionCacheEntryFactory
    implements CacheEntryFactory<PermissionCacheEntry,Permission>
{

    public Permission create( PermissionCacheEntry key )
        throws CacheException
    {
        return new OgnlInvokePermission( "invoke." + key.method.getDeclaringClass( ).getName() + "." + key.method.getName( ) );
    }
}

