/*
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
package org.apache.commons.ognl.test.util;

public class NameFactory
    extends Object
{
    private final String classBaseName;

    private int classNameCounter;

    private final String variableBaseName;

    private int variableNameCounter;

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public NameFactory( String classBaseName, String variableBaseName )
    {
        this.classBaseName = classBaseName;
        this.variableBaseName = variableBaseName;
    }

    /*
     * =================================================================== Public methods
     * ===================================================================
     */
    public String getNewClassName()
    {
        return classBaseName + classNameCounter++;
    }

    public String getNewVariableName()
    {
        return variableBaseName + variableNameCounter++;
    }
}
