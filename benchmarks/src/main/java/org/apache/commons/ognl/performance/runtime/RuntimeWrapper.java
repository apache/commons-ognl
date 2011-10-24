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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * User: mcucchiara
 * Date: 19/10/11
 * Time: 9.37
 */
public interface RuntimeWrapper {
    void getFields(Class<?> c) throws Exception;

    void findParameterTypes(Class<?> targetClass, Method method) throws Exception;

    void getConstructors(Class<?> c) throws Exception;

    void getMethods(Class<?> c, boolean staticMethods) throws Exception;

    void getDeclaredMethods(Class<?> c, String propertyName, boolean setters) throws Exception;

    void getParameterTypes(Method method) throws Exception;

    void getParameterTypes(Constructor<?> constructor) throws Exception;

    void getPermission(Method method) throws Exception;

    void getPrimitiveDefaultValue(Class<?> type) throws Exception;

    void invokeMethod(Object o, Method method, Object[] args) throws Exception;

    void clearCache();
}
