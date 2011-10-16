package org.apache.commons.ognl.internal.entry;

import java.lang.reflect.Method;

public class PermissionCacheEntry implements CacheEntry
{
    public Method method;

    public PermissionCacheEntry( Method method )
    {
        this.method = method;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof PermissionCacheEntry ) )
        {
            return false;
        }

        PermissionCacheEntry that = (PermissionCacheEntry) o;

        if ( method != null ? !method.equals( that.method ) : that.method != null )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode( )
    {
        return method != null ? method.hashCode( ) : 0;
    }
}
