/*
 * $Id$
 *
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

package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.ClassCacheInspector;

import java.util.concurrent.ConcurrentHashMap;

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
