package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.ClassCacheInspector;

import java.util.concurrent.ConcurrentHashMap;

/**
 * User: Maurizio Cucchiara
 * Date: 10/8/11
 * Time: 8:04 PM
 */
public class ConcurrentClassCache<V>
    implements ClassCache<V>
{
    private ClassCacheInspector inspector;

    private ConcurrentHashMap<Class<?>, V> cache = new ConcurrentHashMap<Class<?>, V>();

    public ConcurrentClassCache()
    {
    }


    public void setClassInspector( ClassCacheInspector inspector )
    {
        this.inspector = inspector;
    }

    public void clear()
    {
        cache.clear();
    }

    public int getSize()
    {
        return cache.size();
    }

    public V get( Class<?> key )
        throws CacheException
    {
        return get( key, null );
    }

    public V get( Class<?> key, CacheEntryFactory<Class<?>, V> cacheEntryFactory )
        throws CacheException
    {
        V v = cache.get( key );
        if ( v == null && cacheEntryFactory != null )
        {
            return put( key, cacheEntryFactory.create( key ) );
        }
        return v;
    }

    public V put( Class<?> key, V value )
    {
        if ( inspector != null && !inspector.shouldCache( key ) )
        {
            return value;
        }
        V collision = cache.putIfAbsent( key, value );
        if ( collision != null )
        {
            return collision;
        }
        return value;
    }
}
