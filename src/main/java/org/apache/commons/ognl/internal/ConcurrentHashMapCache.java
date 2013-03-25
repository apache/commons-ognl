package org.apache.commons.ognl.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information       
 * regarding copyright ownership.  The ASF licenses this file  
 * to you under the Apache License, Version 2.0 (the           
 * "License"); you may not use this file except in compliance  
 * with the License.  You may obtain a copy of the License at  
 *                                                             
 *   http://www.apache.org/licenses/LICENSE-2.0                
 *                                                             
 * Unless required by applicable law or agreed to in writing,  
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY      
 * KIND, either express or implied.  See the License for the   
 * specific language governing permissions and limitations     
 * under the License.
 */

/*
 * $Id$
 */
import org.apache.commons.ognl.internal.entry.CacheEntryFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapCache<K, V>
    implements Cache<K, V>
{
    private ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<K, V>();

    private CacheEntryFactory<K, V> cacheEntryFactory;

    public ConcurrentHashMapCache()
    {
    }

    public ConcurrentHashMapCache( CacheEntryFactory<K, V> cacheEntryFactory )
    {
        this.cacheEntryFactory = cacheEntryFactory;
    }

    public void clear()
    {
        cache.clear();
    }

    public int getSize()
    {
        return cache.size();
    }

    public V get( K key )
        throws CacheException
    {
        V v = cache.get( key );
        if ( shouldCreate( cacheEntryFactory, v ) )
        {
            return put( key, cacheEntryFactory.create( key ) );
        }
        return v;
    }

    protected boolean shouldCreate( CacheEntryFactory<K, V> cacheEntryFactory, V v )
        throws CacheException
    {
        return cacheEntryFactory != null && v == null;
    }

    public V put( K key, V value )
    {
        V collision = cache.putIfAbsent( key, value );
        if ( collision != null )
        {
            return collision;
        }
        return value;
    }

    public boolean contains( K key )
    {
        return this.cache.contains( key );
    }
}
