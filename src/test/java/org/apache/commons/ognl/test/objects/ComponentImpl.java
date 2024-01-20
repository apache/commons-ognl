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
package org.apache.commons.ognl.test.objects;

/**
 */
public class ComponentImpl
    implements IComponent
{

    String _clientId;

    int _count = 0;

    public String getClientId()
    {
        return _clientId;
    }

    public void setClientId( String id )
    {
        _clientId = id;
    }

    public int getCount( String index )
    {
        return _count;
    }

    public void setCount( String index, int count )
    {
        _count = count;
    }
}
