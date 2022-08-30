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

import org.apache.commons.ognl.internal.entry.MethodPermCacheEntryFactory;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.security.Permission;

/**
 * User: Maurizio Cucchiara
 * Date: 10/17/11
 * Time: 12:13 AM
 */
public class MethodPermCacheTest {
    private final SecurityManager allowAllSecurityManager = new AllowAllSecurityManager();
    private final SecurityManager denyAllSecurityManager = new DenyAllSecurityManager();

    private Cache<Method, Boolean> createCache(SecurityManager securityManager) {
        return new ConcurrentHashMapCache<Method, Boolean>(new MethodPermCacheEntryFactory(securityManager) );
    }

    @Test
    public void testGetPublicMethod_returnsTrue( )
        throws CacheException, NoSuchMethodException
    {
        Cache<Method, Boolean> cache = createCache(allowAllSecurityManager);

        Method method = Root.class.getMethod( "getArray");
        Assert.assertTrue( cache.get( method ) );
    }

    @Test
    public void testGetPublicMethod_returnsFalse( )
            throws CacheException, NoSuchMethodException
    {
        Cache<Method, Boolean> cache = createCache(denyAllSecurityManager);
        Method method = Root.class.getMethod( "getArray");

        Assert.assertFalse( cache.get( method ) );
    }

    private class AllowAllSecurityManager
        extends SecurityManager
    {
        @Override
        public void checkPermission( Permission perm )
        {
        }
    }

    private class DenyAllSecurityManager
            extends SecurityManager
    {
        @Override
        public void checkPermission( Permission perm )
        {
            throw new SecurityException("Denied.");
        }
    }
}
