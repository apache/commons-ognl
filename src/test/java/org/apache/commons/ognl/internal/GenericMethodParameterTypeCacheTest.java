/*
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

import org.apache.commons.ognl.internal.entry.GenericMethodParameterTypeCacheEntry;
import org.apache.commons.ognl.internal.entry.GenericMethodParameterTypeFactory;
import org.apache.commons.ognl.test.objects.GameGeneric;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * User: Maurizio Cucchiara
 * Date: 10/16/11
 * Time: 9:44 AM
 */
public class GenericMethodParameterTypeCacheTest
{
    private final ConcurrentHashMapCache<GenericMethodParameterTypeCacheEntry, Class<?>[]> cache =
        new ConcurrentHashMapCache<GenericMethodParameterTypeCacheEntry, Class<?>[]>( new GenericMethodParameterTypeFactory( ) );

    @Test
    public void testGet( )
        throws NoSuchMethodException, CacheException
    {
        Method m = GameGeneric.class.getMethod( "setIds", Serializable[].class );
        Class type = GameGeneric.class;
        Class<?>[] types = cache.get( new GenericMethodParameterTypeCacheEntry( m, type ) );

        Assert.assertEquals( 1, types.length );
        Assert.assertEquals( Long[].class, types[0] );
    }

}
