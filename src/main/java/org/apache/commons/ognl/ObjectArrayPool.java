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

/**
 * This class was previously intended to produce performance improvement.<br>
 * This hand-made object pooling is now a bottleneck under high load.<br>
 * We now rely on the new jvm garbage collection improvements to handle object allocation efficiently.
 *
 * @deprecated object-pooling now relies on the jvm garbage collection
 */
public final class ObjectArrayPool
{
    public ObjectArrayPool()
    {
        super();
    }

    public Object[] create( int arraySize )
    {
        return new Object[arraySize];
    }

    public Object[] create( Object singleton )
    {
        Object[] result = create( 1 );

        result[0] = singleton;
        return result;
    }

    public Object[] create( Object object1, Object object2 )
    {
        Object[] result = create( 2 );

        result[0] = object1;
        result[1] = object2;
        return result;
    }

    public Object[] create( Object object1, Object object2, Object object3 )
    {
        Object[] result = create( 3 );

        result[0] = object1;
        result[1] = object2;
        result[2] = object3;
        return result;
    }

    public Object[] create( Object object1, Object object2, Object object3, Object object4 )
    {
        Object[] result = create( 4 );

        result[0] = object1;
        result[1] = object2;
        result[2] = object3;
        result[3] = object4;
        return result;
    }

    public Object[] create( Object object1, Object object2, Object object3, Object object4, Object object5 )
    {
        Object[] result = create( 5 );

        result[0] = object1;
        result[1] = object2;
        result[2] = object3;
        result[3] = object4;
        result[4] = object5;
        return result;
    }

    /**
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public void recycle( Object[] value )
    {
        // no need of recycling, we rely on the garbage collection efficiency
    }
}
