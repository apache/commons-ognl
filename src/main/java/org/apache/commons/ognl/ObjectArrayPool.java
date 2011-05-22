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
import java.util.Arrays;
import java.util.List;

public final class ObjectArrayPool
{
    private final IntHashMap<Integer, SizePool> pools = new IntHashMap<Integer, SizePool>( 23 );

    public static class SizePool
        extends Object
    {
        private List<Object[]> arrays = new ArrayList<Object[]>();

        private int arraySize;

        private int size;

        private int created = 0;

        private int recovered = 0;

        private int recycled = 0;

        public SizePool( int arraySize )
        {
            this( arraySize, 0 );
        }

        public SizePool( int arraySize, int initialSize )
        {
            super();
            this.arraySize = arraySize;
            for ( int i = 0; i < initialSize; i++ )
            {
                arrays.add( new Object[arraySize] );
            }
            created = size = initialSize;
        }

        public int getArraySize()
        {
            return arraySize;
        }

        public Object[] create()
        {
            Object[] result;

            if ( size > 0 )
            {
                result = arrays.remove( size - 1 );
                size--;
                recovered++;
            }
            else
            {
                result = new Object[arraySize];
                created++;
            }
            return result;
        }

        public synchronized void recycle( Object[] value )
        {
            if ( value != null )
            {
                if ( value.length != arraySize )
                {
                    throw new IllegalArgumentException( "recycled array size " + value.length
                        + " inappropriate for pool array size " + arraySize );
                }
                Arrays.fill( value, null );
                arrays.add( value );
                size++;
                recycled++;
            }
            else
            {
                throw new IllegalArgumentException( "cannot recycle null object" );
            }
        }

        /**
         * Returns the number of items in the pool
         */
        public int getSize()
        {
            return size;
        }

        /**
         * Returns the number of items this pool has created since it's construction.
         */
        public int getCreatedCount()
        {
            return created;
        }

        /**
         * Returns the number of items this pool has recovered from the pool since its construction.
         */
        public int getRecoveredCount()
        {
            return recovered;
        }

        /**
         * Returns the number of items this pool has recycled since it's construction.
         */
        public int getRecycledCount()
        {
            return recycled;
        }
    }

    public ObjectArrayPool()
    {
        super();
    }

    public IntHashMap<Integer, SizePool> getSizePools()
    {
        return pools;
    }

    public synchronized SizePool getSizePool( int arraySize )
    {
        SizePool result = pools.get( arraySize );

        if ( result == null )
        {
            pools.put( arraySize, result = new SizePool( arraySize ) );
        }
        return result;
    }

    public synchronized Object[] create( int arraySize )
    {
        return getSizePool( arraySize ).create();
    }

    public synchronized Object[] create( Object singleton )
    {
        Object[] result = create( 1 );

        result[0] = singleton;
        return result;
    }

    public synchronized Object[] create( Object object1, Object object2 )
    {
        Object[] result = create( 2 );

        result[0] = object1;
        result[1] = object2;
        return result;
    }

    public synchronized Object[] create( Object object1, Object object2, Object object3 )
    {
        Object[] result = create( 3 );

        result[0] = object1;
        result[1] = object2;
        result[2] = object3;
        return result;
    }

    public synchronized Object[] create( Object object1, Object object2, Object object3, Object object4 )
    {
        Object[] result = create( 4 );

        result[0] = object1;
        result[1] = object2;
        result[2] = object3;
        result[3] = object4;
        return result;
    }

    public synchronized Object[] create( Object object1, Object object2, Object object3, Object object4, Object object5 )
    {
        Object[] result = create( 5 );

        result[0] = object1;
        result[1] = object2;
        result[2] = object3;
        result[3] = object4;
        result[4] = object5;
        return result;
    }

    public synchronized void recycle( Object[] value )
    {
        if ( value != null )
        {
            getSizePool( value.length ).recycle( value );
        }
    }
}
