package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.internal.entry.CacheEntryFactory;
import org.apache.commons.ognl.internal.entry.ClassCacheEntryFactory;

/**
 * User: Maurizio Cucchiara
 * Date: 10/22/11
 * Time: 1:35 AM
 */
public interface CacheFactory
{
    <K, V> Cache<K, V> createCache( CacheEntryFactory<K, V> entryFactory );

    <V> ClassCache<V> createClassCache();

    <V> ClassCache<V> createClassCache( ClassCacheEntryFactory<V> entryFactory );
}
