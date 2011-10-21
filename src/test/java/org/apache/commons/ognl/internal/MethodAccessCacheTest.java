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

import org.apache.commons.ognl.internal.entry.MethodAccessCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.MethodAccessEntryValue;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * User: Maurizio Cucchiara
 * Date: 10/17/11
 * Time: 12:59 AM
 */
public class MethodAccessCacheTest
{
    private Cache<Method, MethodAccessEntryValue> cache =
        new ConcurrentHashMapCache<Method, MethodAccessEntryValue>( new MethodAccessCacheEntryFactory( ) );

    @Test
    public void testGetAccessibleNonPublicMethod( )
        throws Exception
    {
        Method method = Root.class.getDeclaredMethod( "getPrivateAccessorIntValue3" );
        MethodAccessEntryValue methodAccessValue = cache.get( method );
        Assert.assertTrue( methodAccessValue.isNotPublic( ) );
        Assert.assertFalse( methodAccessValue.isAccessible());
    }

    @Test
    public void testGetNotAccessibleNonPublicMethod( )
        throws Exception
    {
        Method method = Root.class.getDeclaredMethod( "getPrivateAccessorIntValue3" );
        method.setAccessible( true );
        MethodAccessEntryValue methodAccessValue = cache.get( method );
        Assert.assertTrue( methodAccessValue.isNotPublic( ) );
        Assert.assertTrue( methodAccessValue.isAccessible());
    }

    @Test
    public void testGetPublicMethod( )
        throws NoSuchMethodException, CacheException
    {
        Method method = Root.class.getDeclaredMethod( "getArray" );
        MethodAccessEntryValue methodAccessValue = cache.get( method );
        Assert.assertFalse( methodAccessValue.isNotPublic( ) );
        Assert.assertTrue( methodAccessValue.isAccessible());
    }

}
