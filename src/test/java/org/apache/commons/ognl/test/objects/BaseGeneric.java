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
package org.apache.commons.ognl.test.objects;

import java.io.Serializable;

/**
 * Used to test ognl handling of java generics.
 */
public class BaseGeneric<E extends GenericObject, I extends Serializable>
{

    E _value;

    GenericService _service;

    protected I[] ids;

    public BaseGeneric()
    {
        _service = new GenericServiceImpl();
    }

    public void setIds( I[] ids )
    {
        this.ids = ids;
    }

    public I[] getIds()
    {
        return this.ids;
    }

    public String getMessage()
    {
        return "Message";
    }

    public E getValue()
    {
        return _value;
    }

    public GenericService getService()
    {
        return _service;
    }

    public String format( Object value )
    {
        return value.toString();
    }
}
