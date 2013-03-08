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

import java.util.Map;
import java.util.Set;

/**
 * Implementation of PropertyAccessor that uses numbers and dynamic subscripts as properties to index into Lists.
 */
public class SetPropertyAccessor
    extends ObjectPropertyAccessor
    implements PropertyAccessor // This is here to make javadoc show this class as an implementor
{

    @Override
    public Object getProperty( Map<String, Object> context, Object target, Object name )
        throws OgnlException
    {
        Set<?> set = (Set<?>) target; // check performed by the invoker

        if ( name instanceof String )
        {
            Object result;

            if ( "size".equals( name ) )
            {
                result = set.size();
            }
            else if ( "iterator".equals( name ) )
            {
                result = set.iterator();
            }
            else if ( "isEmpty".equals( name ) )
            {
                result = set.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
            }
            else
            {
                result = super.getProperty( context, target, name );
            }
            return result;
        }

        throw new NoSuchPropertyException( target, name );
    }

}
