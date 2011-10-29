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
 * $Id$
 */

import org.apache.commons.ognl.internal.CacheException;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class GenericMethodParameterTypeFactory
    implements CacheEntryFactory<GenericMethodParameterTypeCacheEntry, Class<?>[]>
{
    public Class<?>[] create( GenericMethodParameterTypeCacheEntry entry )
        throws CacheException
    {
        Class<?>[] types;

        ParameterizedType param = (ParameterizedType) entry.type.getGenericSuperclass();
        Type[] genTypes = entry.method.getGenericParameterTypes();
        TypeVariable<?>[] declaredTypes = entry.method.getDeclaringClass().getTypeParameters();

        types = new Class[genTypes.length];

        for ( int i = 0; i < genTypes.length; i++ )
        {
            TypeVariable<?> paramType = null;

            if ( TypeVariable.class.isInstance( genTypes[i] ) )
            {
                paramType = (TypeVariable<?>) genTypes[i];
            }
            else if ( GenericArrayType.class.isInstance( genTypes[i] ) )
            {
                paramType = (TypeVariable<?>) ( (GenericArrayType) genTypes[i] ).getGenericComponentType();
            }
            else if ( ParameterizedType.class.isInstance( genTypes[i] ) )
            {
                types[i] = (Class<?>) ( (ParameterizedType) genTypes[i] ).getRawType();
                continue;
            }
            else if ( Class.class.isInstance( genTypes[i] ) )
            {
                types[i] = (Class<?>) genTypes[i];
                continue;
            }

            Class<?> resolved = resolveType( param, paramType, declaredTypes );

            if ( resolved != null )
            {
                if ( GenericArrayType.class.isInstance( genTypes[i] ) )
                {
                    resolved = Array.newInstance( resolved, 0 ).getClass();
                }

                types[i] = resolved;
                continue;
            }
            types[i] = entry.method.getParameterTypes()[i];
        }

        return types;
    }

    private Class<?> resolveType( ParameterizedType param, TypeVariable<?> var, TypeVariable<?>[] declaredTypes )
    {
        if ( param.getActualTypeArguments().length < 1 )
        {
            return null;
        }

        for ( int i = 0; i < declaredTypes.length; i++ )
        {
            if ( !TypeVariable.class.isInstance( param.getActualTypeArguments()[i] )
                && declaredTypes[i].getName().equals( var.getName() ) )
            {
                return (Class<?>) param.getActualTypeArguments()[i];
            }
        }

        return null;
    }
}
