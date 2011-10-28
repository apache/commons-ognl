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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.ognl.performance.runtime;

import org.apache.commons.ognl.OgnlRuntime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * User: Maurizio Cucchiara
 * Date: 10/22/11
 * Time: 12:38 AM
 */
public class CommonsRuntimeWrapper
    implements RuntimeWrapper
{


    public void getFields( Class<?> c )
        throws Exception
    {
        OgnlRuntime.getFields( c );
    }


    public void findParameterTypes( Class<?> targetClass, Method method )
        throws Exception
    {
        OgnlRuntime.findParameterTypes( targetClass, method );
    }


    public void getConstructors( Class<?> c )
        throws Exception
    {
        OgnlRuntime.getConstructors( c );
    }


    public void getMethods( Class<?> c, boolean staticMethods )
        throws Exception
    {
        OgnlRuntime.getMethods( c, staticMethods );
    }


    public void getDeclaredMethods( Class<?> c, String propertyName, boolean setters )
        throws Exception
    {
        OgnlRuntime.getDeclaredMethods( c, propertyName, setters );
    }


    public void getParameterTypes( Method method )
        throws Exception
    {
        OgnlRuntime.getParameterTypes( method );
    }


    public void getParameterTypes( Constructor<?> constructor )
        throws Exception
    {
        OgnlRuntime.getParameterTypes( constructor );
    }


    public void getPermission( Method method )
        throws Exception
    {
        OgnlRuntime.getPermission( method );
    }


    public void getPrimitiveDefaultValue( Class<?> type )
        throws Exception
    {
        OgnlRuntime.getPrimitiveDefaultValue( type );
    }


    public void invokeMethod( Object o, Method method, Object[] args )
        throws Exception
    {
        OgnlRuntime.invokeMethod( o, method, args );
    }


    public void clearCache()
    {
        OgnlRuntime.clearCache();
    }

    public void getCompiler()
    {
        OgnlRuntime.getCompiler( null );
    }
}
