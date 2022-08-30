package org.apache.commons.ognl.internal.entry;

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

/*
 */


import org.apache.commons.ognl.OgnlRuntime;
import org.apache.commons.ognl.internal.CacheException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MethodCacheEntryFactory<T extends MethodCacheEntry>
    implements CacheEntryFactory<T, Map<String, List<Method>>>
{
    public Map<String, List<Method>> create( T key )
        throws CacheException
    {
        Map<String, List<Method>> result = new HashMap<String, List<Method>>( 23 );

        Class<?> c = key.targetClass;
        while ( c != null )
        {
            for ( Method method : c.getDeclaredMethods() )
            {
                // skip over synthetic methods

                if ( !OgnlRuntime.isMethodCallable( method ) )
                {
                    continue;
                }

                if ( shouldCache( key, method ) )
                {
                    List<Method> ml = result.computeIfAbsent(method.getName(), k -> new ArrayList<Method>());

                    ml.add( method );
                }
            }
            c = c.getSuperclass();
        }
        return result;
    }

    protected abstract boolean shouldCache( T key, Method method );
}
