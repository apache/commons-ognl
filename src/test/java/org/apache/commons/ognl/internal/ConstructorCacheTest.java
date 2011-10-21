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

import org.apache.commons.ognl.internal.entry.CacheEntryFactory;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class ConstructorCacheTest
{
    private int count;
    ClassCache<List<Constructor<?>>> cache = new ConcurrentHashMapClassCache<List<Constructor<?>>>(new CacheEntryFactory<Class<?>, List<Constructor<?>>>( )
    {
        public List<Constructor<?>> create( Class<?> key )
            throws CacheException
        {
            count++;
            return Arrays.asList( key.getConstructors( ) );
        }
    });

    @Test
    public void testGet()
        throws CacheException
    {
        List<Constructor<?>> constructors = cache.get( Root.class );
        assertNotNull( constructors );
        cache.get( Root.class );
        assertEquals( 1, count);
    }
}
