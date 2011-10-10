package org.apache.commons.ognl.internal;

/**
 * User: Maurizio Cucchiara
 * Date: 10/8/11
 * Time: 7:58 PM
 */
public interface ClassCacheEntryFactory<V>
    extends CacheEntryFactory<Class<?>, V>
{
    V create( Class<?> key )
        throws CacheException;

}
