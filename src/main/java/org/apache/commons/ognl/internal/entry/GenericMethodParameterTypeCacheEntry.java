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

package org.apache.commons.ognl.internal.entry;

import java.lang.reflect.Method;

public class GenericMethodParameterTypeCacheEntry
    implements CacheEntry
{
    Method method;

    Class<?> type;

    public GenericMethodParameterTypeCacheEntry( Method method, Class<?> type )
    {
        this.method = method;
        this.type = type;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof GenericMethodParameterTypeCacheEntry ) )
        {
            return false;
        }

        GenericMethodParameterTypeCacheEntry that = (GenericMethodParameterTypeCacheEntry) o;

        if ( !method.equals( that.method ) )
        {
            return false;
        }
        if ( !type.equals( that.type ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode( )
    {
        int result = method.hashCode( );
        result = 31 * result + type.hashCode( );
        return result;
    }
}
