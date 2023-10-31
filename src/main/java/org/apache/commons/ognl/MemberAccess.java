package org.apache.commons.ognl;

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

import java.lang.reflect.Member;
import java.util.Map;

/**
 * This interface provides a hook for preparing for accessing members of objects. The Java2 version of this method can
 * allow access to otherwise inaccessible members, such as private fields.
 */
public interface MemberAccess
{
    /**
     * Sets the member up for accessibility
     */
    Object setup( Map<String, Object> context, Object target, Member member, String propertyName );

    /**
     * Restores the member from the previous setup call.
     */
    void restore( Map<String, Object> context, Object target, Member member, String propertyName, Object state );

    /**
     * Returns true if the given member is accessible or can be made accessible by this object.
     */
    boolean isAccessible( Map<String, Object> context, Object target, Member member, String propertyName );
}
