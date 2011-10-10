package org.apache.commons.ognl.internal;

/**
 * User: Maurizio Cucchiara
 * Date: 10/8/11
 * Time: 7:57 PM
 */
public interface CacheEntryFactory<T, V>
{
    public V create( T key )
        throws CacheException;
}
