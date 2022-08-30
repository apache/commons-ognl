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
 */

import org.apache.commons.ognl.internal.entry.CacheEntryFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockCache<K, V>
    implements Cache<K, V>
{
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Lock readLock = lock.readLock();

    private final Lock writeLock = lock.writeLock();

    final Map<K, V> cache = new HashMap<K, V>();

    private CacheEntryFactory<K, V> cacheEntryFactory;

    public ReentrantReadWriteLockCache()
    {
    }

    public ReentrantReadWriteLockCache( CacheEntryFactory<K, V> cacheEntryFactory )
    {
        this.cacheEntryFactory = cacheEntryFactory;
    }

    public void clear()
    {
        synchronized ( cache )
        {
            cache.clear();
        }
    }

    public int getSize()
    {
        synchronized ( cache )
        {
            return cache.size();
        }
    }


    public V get( K key )
        throws CacheException
    {
        V v;
        boolean shouldCreate;
        readLock.lock();
        try
        {
            v = cache.get( key );
            shouldCreate = shouldCreate( cacheEntryFactory, v );
        }
        finally
        {
            readLock.unlock();
        }
        if ( shouldCreate )
        {
            try
            {
                writeLock.lock();
                v = cache.get( key );
                if ( !shouldCreate( cacheEntryFactory, v ) )
                {
                    return v;
                }
                v = cacheEntryFactory.create( key );
                cache.put( key, v );
                return v;
            }
            finally
            {
                writeLock.unlock();
            }

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
        writeLock.lock();
        try
        {
            cache.put( key, value );
            return value;
        }
        finally
        {
            writeLock.unlock();
        }
    }
}