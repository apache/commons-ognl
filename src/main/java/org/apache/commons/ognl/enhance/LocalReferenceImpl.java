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

package org.apache.commons.ognl.enhance;

/**
 * Implementation of {@link LocalReference}.
 */
public class LocalReferenceImpl
    implements LocalReference
{

    private final String name;

    private final Class<?> type;

    private final String expression;

    public LocalReferenceImpl( String name, String expression, Class<?> type )
    {
        this.name = name;
        this.type = type;
        this.expression = expression;
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String getExpression()
    {
        return expression;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getType()
    {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        LocalReferenceImpl that = (LocalReferenceImpl) o;

        if ( expression != null ? !expression.equals( that.expression ) : that.expression != null )
        {
            return false;
        }
        if ( name != null ? !name.equals( that.name ) : that.name != null )
        {
            return false;
        }
        if ( type != null ? !type.equals( that.type ) : that.type != null )
        {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int result;
        result = ( name != null ? name.hashCode() : 0 );
        result = 31 * result + ( type != null ? type.hashCode() : 0 );
        result = 31 * result + ( expression != null ? expression.hashCode() : 0 );
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "LocalReferenceImpl[" + "_name='" + name + '\'' + '\n' + ", _type=" + type + '\n' + ", _expression='"
            + expression + '\'' + '\n' + ']';
    }
}
