/*
 * $Id$
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

    private Entry[] _table;

    private ClassCacheInspector _classInspector;

    private int _size = 0;

    public ClassCacheImpl()
    {
        _table = new Entry[TABLE_SIZE];
    }

    public void setClassInspector( ClassCacheInspector inspector )
    {
        _classInspector = inspector;
    }

    public void clear()
    {
        for ( int i = 0; i < _table.length; i++ )
        {
            _table[i] = null;
        }

        _size = 0;
    }

    public int getSize()
    {
        return _size;
    }

    public final Object get( Class key )
    {
        Object result = null;
        int i = key.hashCode() & TABLE_SIZE_MASK;

        for ( Entry entry = _table[i]; entry != null; entry = entry.next )
        {
            if ( entry.key == key )
            {
                result = entry.value;
                break;
            }
        }

        return result;
    }

    public final Object put( Class key, Object value )
    {
        if ( _classInspector != null && !_classInspector.shouldCache( key ) )
            return value;

        Object result = null;
        int i = key.hashCode() & TABLE_SIZE_MASK;
        Entry entry = _table[i];

        if ( entry == null )
        {
            _table[i] = new Entry( key, value );
            _size++;
        }
        else
        {
            if ( entry.key == key )
            {
                result = entry.value;
                entry.value = value;
            }
            else
            {
                while ( true )
                {
                    if ( entry.key == key )
                    {
                        /* replace value */
                        result = entry.value;
                        entry.value = value;
                        break;
                    }
                    else
                    {
                        if ( entry.next == null )
                        {
                            /* add value */
                            entry.next = new Entry( key, value );
                            break;
                        }
                    }
                    entry = entry.next;
                }
            }
        }

        return result;
    }

    public String toString()
    {
        return "ClassCacheImpl[" + "_table=" + ( _table == null ? null : Arrays.asList( _table ) ) + '\n'
            + ", _classInspector=" + _classInspector + '\n' + ", _size=" + _size + '\n' + ']';
    }
}
