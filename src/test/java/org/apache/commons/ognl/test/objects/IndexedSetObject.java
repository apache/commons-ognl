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

import java.util.HashMap;

/**
 * Test for OGNL-119.
 */
public class IndexedSetObject
{

    private final HashMap<String, Object> things = new HashMap<String, Object>();

    public IndexedSetObject()
    {
        things.put( "x", new Container( 1 ) );
    }

    public Object getThing( String index )
    {
        return things.get( index );
    }

    public void setThing( String index, Object value )
    {
        things.put( index, value );
    }

    public static class Container
    {
        private int val;

        public Container( int val )
        {
            this.val = val;
        }

        public int getVal()
        {
            return val;
        }

        public void setVal( int val )
        {
            this.val = val;
        }
    }
}
