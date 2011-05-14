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
 * 
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class SetPropertyAccessor
    extends ObjectPropertyAccessor
    implements PropertyAccessor // This is here to make javadoc show this class as an implementor
{
    public Object getProperty( Map context, Object target, Object name )
        throws OgnlException
    {
        Set set = (Set) target;

        if ( name instanceof String )
        {
            Object result;

            if ( name.equals( "size" ) )
            {
                result = new Integer( set.size() );
            }
            else
            {
                if ( name.equals( "iterator" ) )
                {
                    result = set.iterator();
                }
                else
                {
                    if ( name.equals( "isEmpty" ) )
                    {
                        result = set.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
                    }
                    else
                    {
                        result = super.getProperty( context, target, name );
                    }
                }
            }
            return result;
        }

        throw new NoSuchPropertyException( target, name );
    }

}
