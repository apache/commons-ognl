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

import org.apache.commons.ognl.internal.entry.PermissionCacheEntry;
import org.apache.commons.ognl.internal.entry.PermissionCacheEntryFactory;
import org.apache.commons.ognl.test.objects.Bean2;
import org.junit.Test;

import java.security.Permission;

import static junit.framework.Assert.assertNotNull;

/**
 * User: Maurizio Cucchiara
 * Date: 10/15/11
 * Time: 8:00 AM
 */
public class PermissionCacheTest
{
    ConcurrentHashMapCache<PermissionCacheEntry, Permission> cache =
        new ConcurrentHashMapCache<PermissionCacheEntry, Permission>( new PermissionCacheEntryFactory());

    @Test
    public void testGet( )
        throws Exception
    {

        getPermission( new PermissionCacheEntry( Bean2.class.getMethod( "getBean3" ) ) );
        getPermission( new PermissionCacheEntry( Bean2.class.getMethod( "getId" ) ) );
    }

    private void getPermission( PermissionCacheEntry entry )
        throws CacheException
    {
        Permission permission = cache.get( entry );
        assertNotNull( permission );
    }
}
