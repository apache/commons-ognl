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
package org.apache.commons.ognl.test;

import org.apache.commons.ognl.NullHandler;

import java.util.Map;

public class CorrectedObjectNullHandler extends Object implements NullHandler
{
    private String          defaultValue;

    /*===================================================================
        Constructors
      ===================================================================*/
    public CorrectedObjectNullHandler(String defaultValue)
    {
        super();
        this.defaultValue = defaultValue;
    }

    /*===================================================================
        TypeConverter interface (overridden)
      ===================================================================*/
    public Object nullMethodResult(Map context, Object target, String methodName, Object[] args)
    {
        if (methodName.equals("getStringValue")) {
            return defaultValue;
        }
        return null;
    }

    public Object nullPropertyValue(Map context, Object target, Object property)
    {
        Object      result = null;

        if (property.equals("stringValue")) {
            return defaultValue;
        }
        return null;
    }
}
