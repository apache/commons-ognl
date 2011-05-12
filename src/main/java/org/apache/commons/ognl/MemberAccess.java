/*
 * $Id$
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.ognl;

import java.lang.reflect.Member;
import java.util.Map;

/**
 * This interface provides a hook for preparing for accessing members
 * of objects.  The Java2 version of this method can allow access
 * to otherwise inaccessable members, such as private fields.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 * @version 15 October 1999
 */
public interface MemberAccess
{
    /**
        Sets the member up for accessibility
     */
    public Object setup(Map context, Object target, Member member, String propertyName);

    /**
        Restores the member from the previous setup call.
     */
    public void restore(Map context, Object target, Member member, String propertyName, Object state);

    /**
        Returns true if the given member is accessible or can be made accessible
        by this object.
     */
	public boolean isAccessible(Map context, Object target, Member member, String propertyName);
}
