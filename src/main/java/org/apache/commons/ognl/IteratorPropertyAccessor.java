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

import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of PropertyAccessor that provides "property" reference to "next" and "hasNext".
 */
public class IteratorPropertyAccessor
    extends ObjectPropertyAccessor
    implements PropertyAccessor // This is here to make javadoc show this class as an implementor
{
    @Override
    public Object getProperty( Map<String, Object> context, Object target, Object name )
        throws OgnlException
    {
        Object result;
        Iterator<?> iterator = (Iterator<?>) target; // check performed by the invoker

        if ( name instanceof String )
        {
            if ( "next".equals( name ) )
            {
                result = iterator.next();
            }
            else
            {
                if ( "hasNext".equals( name ) )
                {
                    result = iterator.hasNext() ? Boolean.TRUE : Boolean.FALSE;
                }
                else
                {
                    result = super.getProperty( context, target, name );
                }
            }
        }
        else
        {
            result = super.getProperty( context, target, name );
        }
        return result;
    }

    @Override
    public void setProperty( Map<String, Object> context, Object target, Object name, Object value )
        throws OgnlException
    {
        throw new IllegalArgumentException( "can't set property " + name + " on Iterator" );
    }
}
