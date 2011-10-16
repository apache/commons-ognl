package org.apache.commons.ognl.internal.entry;

import org.apache.commons.ognl.OgnlRuntime;
import org.apache.commons.ognl.internal.CacheException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Maurizio Cucchiara
 * Date: 10/15/11
 * Time: 4:42 PM
 */
public abstract class MethodCacheEntryFactory<T extends MethodCacheEntry>
    implements CacheEntryFactory<T,Map<String, List<Method>>>
{
    public Map<String, List<Method>> create( T key )
        throws CacheException
    {
        Map<String, List<Method>> result = new HashMap<String, List<Method>>( 23 );

        Class<?> c = key.targetClass;
        while ( c != null )
        {
            Method[] ma = c.getDeclaredMethods( );

            for ( Method method : ma )
            {
                // skip over synthetic methods

                if ( !OgnlRuntime.isMethodCallable( method ) )
                {
                    continue;
                }

                if ( shouldCache( key, method ) )
                {
                    List<Method> ml = result.get( method.getName( ) );

                    if ( ml == null )
                    {
                        ml = new ArrayList<Method>( );
                        result.put( method.getName( ), ml );
                    }

                    ml.add( method );
                }
            }
            c = c.getSuperclass( );
        }
        return result;
    }

    protected abstract boolean shouldCache( T key, Method method );
}
