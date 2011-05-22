package org.apache.commons.ognl;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A Map that uses ints as the keys.
 * <p>
 * Use just like any java.util.Map, except that the keys must be ints. This is much faster than creating a new Integer
 * for each access.
 * </p>
 * <p>
 * For non-Map access (faster) use the put(int, Object) method.
 * </p>
 * <p>
 * This class implements Map for convenience, but this is not the most efficient usage.
 * </p>
 * 
 * @see java.util.HashMap
 * @see java.util.Map
 */
public class IntHashMap<K extends Number, V>
    implements Map<K, V>
{
    private Entry<V> table[];

    private int count;

    private int threshold;

    private float loadFactor;

    /*
     * =================================================================== Private static classes
     * ===================================================================
     */
    private static class IntHashMapIterator
        implements Iterator
    {
        boolean keys;

        int index;

        Entry table[];

        Entry entry;

        IntHashMapIterator( Entry table[], boolean keys )
        {
            super();
            this.table = table;
            this.keys = keys;
            this.index = table.length;
        }

        /*
         * =================================================================== Iterator interface
         * ===================================================================
         */
        public boolean hasNext()
        {
            if ( entry != null )
            {
                return true;
            }
            while ( index-- > 0 )
            {
                if ( ( entry = table[index] ) != null )
                {
                    return true;
                }
            }
            return false;
        }

        public Object next()
        {
            if ( entry == null )
            {
                while ( ( index-- > 0 ) && ( ( entry = table[index] ) == null ) )
                {
                    /* do nothing */
                }
            }
            if ( entry != null )
            {
                Entry e = entry;

                entry = e.next;
                return keys ? Integer.valueOf( e.getKey() ) : e.getValue();
            }
            throw new NoSuchElementException( "IntHashMapIterator" );
        }

        public void remove()
        {
            throw new UnsupportedOperationException( "remove" );
        }
    }

    /*
     * =================================================================== Public static classes
     * ===================================================================
     */
    public static class Entry<T>
    {

        private final int hash;

        private final int key;

        private T value;

        private Entry<T> next;

        public Entry( int hash, int key, T value )
        {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        public T getValue()
        {
            return value;
        }

        public void setValue( T value )
        {
            this.value = value;
        }

        public Entry<T> getNext()
        {
            return next;
        }

        public void setNext( Entry<T> next )
        {
            this.next = next;
        }

        public int getHash()
        {
            return hash;
        }

        public int getKey()
        {
            return key;
        }

    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public IntHashMap( int initialCapacity, float loadFactor )
    {
        super();
        if ( initialCapacity <= 0 || loadFactor <= 0.0 )
        {
            throw new IllegalArgumentException();
        }
        this.loadFactor = loadFactor;
        table = new Entry[initialCapacity];
        threshold = (int) ( initialCapacity * loadFactor );
    }

    public IntHashMap( int initialCapacity )
    {
        this( initialCapacity, 0.75f );
    }

    public IntHashMap()
    {
        this( 101, 0.75f );
    }

    /*
     * =================================================================== Protected methods
     * ===================================================================
     */
    protected void rehash()
    {
        int oldCapacity = table.length;
        Entry<V> oldTable[] = table;
        int newCapacity = oldCapacity * 2 + 1;
        Entry<V> newTable[] = new Entry[newCapacity];

        threshold = (int) ( newCapacity * loadFactor );
        table = newTable;
        for ( int i = oldCapacity; i-- > 0; )
        {
            for ( Entry<V> old = oldTable[i]; old != null; )
            {
                Entry<V> e = old;
                int index = ( e.getHash() & 0x7FFFFFFF ) % newCapacity;

                old = old.next;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }
    }

    /*
     * =================================================================== Public methods
     * ===================================================================
     */
    public final boolean containsKey( int key )
    {
        int index = ( key & 0x7FFFFFFF ) % table.length;

        for ( Entry<V> e = table[index]; e != null; e = e.next )
        {
            if ( ( e.getHash() == key ) && ( e.getKey() == key ) )
            {
                return true;
            }
        }
        return false;
    }

    public final V get( int key )
    {
        int index = ( key & 0x7FFFFFFF ) % table.length;

        for ( Entry<V> e = table[index]; e != null; e = e.getNext() )
        {
            if ( ( e.getHash() == key ) && ( e.getKey() == key ) )
            {
                return e.getValue();
            }
        }
        return null;
    }

    public final V put( int key, V value )
    {
        int index = ( key & 0x7FFFFFFF ) % table.length;

        if ( value == null )
        {
            throw new IllegalArgumentException();
        }
        for ( Entry<V> e = table[index]; e != null; e = e.next )
        {
            if ( ( e.getHash() == key ) && ( e.getKey() == key ) )
            {
                V old = e.getValue();

                e.setValue( value );
                return old;
            }
        }

        if ( count >= threshold )
        {
            // Rehash the table if the threshold is exceeded.
            rehash();
            return put( key, value );
        }

        Entry<V> e = new Entry<V>( key, key, value );

        e.setNext( table[index] );
        table[index] = e;
        ++count;
        return null;
    }

    public final V remove( int key )
    {
        int index = ( key & 0x7FFFFFFF ) % table.length;

        for ( Entry<V> e = table[index], prev = null; e != null; prev = e, e = e.next )
        {
            if ( ( e.getHash() == key ) && ( e.getKey() == key ) )
            {
                if ( prev != null )
                {
                    prev.next = e.next;
                }
                else
                {
                    table[index] = e.next;
                }
                --count;
                return e.getValue();
            }
        }
        return null;
    }

    /*
     * =================================================================== Map interface
     * ===================================================================
     */
    /**
     * {@inheritDoc}
     */
    public int size()
    {
        return count;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        return count == 0;
    }

    /**
     * {@inheritDoc}
     */
    public V get( Object key )
    {
        if ( !( key instanceof Number ) )
        {
            throw new IllegalArgumentException( "key is not an Number subclass" );
        }
        return get( ( (Number) key ).intValue() );
    }

    /**
     * {@inheritDoc}
     */
    public V put( K key, V value )
    {
        return put( ( (Number) key ).intValue(), value );
    }

    /**
     * {@inheritDoc}
     */
    public void putAll( Map<? extends K, ? extends V> otherMap )
    {
        for ( Iterator<? extends K> it = otherMap.keySet().iterator(); it.hasNext(); )
        {
            K k = it.next();

            put( k, otherMap.get( k ) );
        }
    }

    /**
     * {@inheritDoc}
     */
    public V remove( Object key )
    {
        if ( !( key instanceof Number ) )
        {
            throw new IllegalArgumentException( "key cannot be null" );
        }
        return remove( ( (Number) key ).intValue() );
    }

    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        Entry<V> tab[] = table;

        for ( int index = tab.length; --index >= 0; )
        {
            tab[index] = null;
        }
        count = 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey( Object key )
    {
        if ( !( key instanceof Number ) )
        {
            throw new InternalError( "key is not an Number subclass" );
        }
        return containsKey( ( (Number) key ).intValue() );
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue( Object value )
    {
        Entry<V> tab[] = table;

        if ( value == null )
        {
            throw new IllegalArgumentException();
        }
        for ( int i = tab.length; i-- > 0; )
        {
            for ( Entry<V> e = tab[i]; e != null; e = e.next )
            {
                if ( e.getValue().equals( value ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Set<K> keySet()
    {
        Set<K> result = new HashSet<K>();

        for ( Iterator<K> it = new IntHashMapIterator( table, true ); it.hasNext(); )
        {
            result.add( it.next() );
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<V> values()
    {
        List<V> result = new ArrayList<V>();

        for ( Iterator<V> it = new IntHashMapIterator( table, false ); it.hasNext(); )
        {
            result.add( it.next() );
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Map.Entry<K, V>> entrySet()
    {
        throw new UnsupportedOperationException( "entrySet" );
    }
}
