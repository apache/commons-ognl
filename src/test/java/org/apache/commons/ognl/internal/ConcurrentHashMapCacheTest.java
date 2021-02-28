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
import org.apache.commons.ognl.test.objects.Bean2;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

public class ConcurrentHashMapCacheTest
{
    private final ConcurrentHashMapCacheTest.DummyEntryFactory entryFactory=new DummyEntryFactory( );
    private final Cache<CacheEntry, List<Method>> cache = new ConcurrentHashMapCache<CacheEntry, List<Method>>( entryFactory );


    @Test
    public void testGet( )
        throws Exception
    {

        getMethods( new CacheEntry( Bean2.class, "bean3" ) );
        getMethods( new CacheEntry( Bean2.class, "id" ) );
    }

    private void getMethods( CacheEntry entry )
        throws CacheException
    {
        List<Method> methods = cache.get( entry);
        assertNotNull( methods );
        assertFalse( methods.isEmpty( ) );
    }

    private class CacheEntry
    {
        private final Class<?> clazz;

        private final String methodName;

        private CacheEntry( Class<?> clazz, String methodName )
        {
            this.clazz = clazz;
            this.methodName = methodName;
        }

        public Class<?> getClazz( )
        {
            return clazz;
        }

        public String getMethodName( )
        {
            return methodName;
        }
    }

    private class DummyEntryFactory
        implements CacheEntryFactory<CacheEntry, List<Method>>
    {
        public List<Method> create( CacheEntry key )
            throws CacheException
        {
            Method[] methods = key.getClazz( ).getMethods( );
            List<Method> list = new ArrayList<Method>( );
            for ( Method method : methods )
            {
                String name = method.getName( );
                boolean isGet = name.substring( 3 ).equalsIgnoreCase( key.getMethodName( ) );
                if ( isGet )
                {
                    list.add( method );
                }
            }
            return list;
        }
    }
}
