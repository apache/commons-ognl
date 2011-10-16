package org.apache.commons.ognl.internal.entry;

import org.apache.commons.ognl.OgnlRuntime;
import org.apache.commons.ognl.internal.CacheException;

import java.lang.reflect.Method;

/**
* User: Maurizio Cucchiara
* Date: 10/17/11
* Time: 12:46 AM
*/
public class MethodPermCacheEntryFactory
    implements CacheEntryFactory<Method, Boolean>
{
    private SecurityManager securityManager;

    public MethodPermCacheEntryFactory( SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }

    public Boolean create( Method key )
        throws CacheException
    {
        try
        {
            securityManager.checkPermission( OgnlRuntime.getPermission( key ) );
            return true;
        }
        catch ( SecurityException ex )
        {
            return false;
        }

    }
}
