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

import org.apache.commons.ognl.ClassCacheInspector;

import java.util.Arrays;

/**
 * Implementation of {@link ClassCache}.
 */
public class ClassCacheImpl<V>
    implements ClassCache<V>
{

    /* this MUST be a power of 2 */
    private static final int TABLE_SIZE = 512;

    /* ...and now you see why. The table size is used as a mask for generating hashes */
    private static final int TABLE_SIZE_MASK = TABLE_SIZE - 1;

    private final Entry<Class<?>, V>[] table = new Entry[TABLE_SIZE];

    private ClassCacheInspector classInspector;

    private int size = 0;

    /**
     * {@inheritDoc}
     */
    public void setClassInspector( ClassCacheInspector inspector )
    {
        classInspector = inspector;
    }

    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        Arrays.fill(table, null);

        size = 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getSize()
    {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    public final V get( Class<?> key )
        throws CacheException
    {
        int i = key.hashCode() & TABLE_SIZE_MASK;

        Entry<Class<?>, V> entry = table[i];

        while ( entry != null )
        {
            if ( key == entry.getKey() )
            {
                return entry.getValue();
            }

            entry = entry.getNext();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final V put( Class<?> key, V value )
    {
        if ( classInspector != null && !classInspector.shouldCache( key ) )
        {
            return value;
        }

        V result = null;
        int i = key.hashCode() & TABLE_SIZE_MASK;

        Entry<Class<?>, V> entry = table[i];

        if ( entry == null )
        {
            table[i] = new Entry<Class<?>, V>( key, value );
            size++;
        }
        else
        {
            if ( key == entry.getKey() )
            {
                result = entry.getValue();
                entry.setValue( value );
            }
            else
            {
                while ( true )
                {
                    if ( key == entry.getKey() )
                    {
                        /* replace value */
                        result = entry.getValue();
                        entry.setValue( value );
                        break;
                    }

                    if ( entry.getNext() == null )
                    {
                        /* add value */
                        entry.setNext( new Entry<Class<?>, V>( key, value ) );
                        break;
                    }

                    entry = entry.getNext();
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "ClassCacheImpl[" + "_table=" + ( table == null ? null : Arrays.asList( table ) ) + '\n'
            + ", _classInspector=" + classInspector + '\n' + ", _size=" + size + '\n' + ']';
    }

}
