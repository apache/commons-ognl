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

import org.apache.commons.ognl.ClassCacheInspector;

import java.util.Arrays;

/**
 * Implementation of {@link ClassCache}.
 */
public class ClassCacheImpl
    implements ClassCache
{

    /* this MUST be a power of 2 */
    private static final int TABLE_SIZE = 512;

    /* ...and now you see why. The table size is used as a mask for generating hashes */
    private static final int TABLE_SIZE_MASK = TABLE_SIZE - 1;

    private Entry<?>[] _table;

    private ClassCacheInspector _classInspector;

    private int _size = 0;

    public ClassCacheImpl()
    {
        _table = new Entry[TABLE_SIZE];
    }

    /**
     * {@inheritDoc}
     */
    public void setClassInspector( ClassCacheInspector inspector )
    {
        _classInspector = inspector;
    }

    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        for ( int i = 0; i < _table.length; i++ )
        {
            _table[i] = null;
        }

        _size = 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getSize()
    {
        return _size;
    }

    /**
     * {@inheritDoc}
     */
    public final <T> T get( Class<?> key )
    {
        int i = key.hashCode() & TABLE_SIZE_MASK;

        Entry<?> entry = _table[i];

        while ( entry != null )
        {
            if ( key == entry.getKey() )
            {
                @SuppressWarnings( "unchecked" ) // guaranteed by key == entry.getKey()
                T result = (T) entry.getValue();
                return result;
            }

            entry = entry.getNext();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final <T> T put( Class<?> key, T value )
    {
        if ( _classInspector != null && !_classInspector.shouldCache( key ) )
        {
            return value;
        }

        T result = null;
        int i = key.hashCode() & TABLE_SIZE_MASK;

        Entry<?> entry = _table[i];

        if ( entry == null )
        {
            _table[i] = new Entry<T>( key, value );
            _size++;
        }
        else
        {
            if ( key == entry.getKey() )
            {
                @SuppressWarnings( "unchecked" ) // guaranteed by key == entry.getKey()
                Entry<T> current = (Entry<T>) entry;
                result = current.getValue();
                current.setValue( value );
            }
            else
            {
                while ( true )
                {
                    if ( key == entry.getKey() )
                    {
                        /* replace value */
                        @SuppressWarnings( "unchecked" ) // guaranteed by key == entry.getKey()
                        Entry<T> current = (Entry<T>) entry;
                        result = current.getValue();
                        current.setValue( value );
                        break;
                    }

                    if ( entry.getNext() == null )
                    {
                        /* add value */
                        entry.setNext( new Entry<T>( key, value ) );
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
        return "ClassCacheImpl[" + "_table=" + ( _table == null ? null : Arrays.asList( _table ) ) + '\n'
            + ", _classInspector=" + _classInspector + '\n' + ", _size=" + _size + '\n' + ']';
    }

}
