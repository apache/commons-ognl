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
/**
 * 
 */
package org.apache.commons.ognl.test.objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link BeanProvider}.
 */
public class BeanProviderImpl
    implements Serializable, BeanProvider
{
    private Map _map = new HashMap();

    public BeanProviderImpl()
    {
    }

    public Object getBean( String name )
    {
        return _map.get( name );
    }

    public void setBean( String name, Object bean )
    {
        _map.put( name, bean );
    }
}
