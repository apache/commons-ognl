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
 */
class ASTKeyValue
    extends SimpleNode
{
    public ASTKeyValue( int id )
    {
        super( id );
    }

    public ASTKeyValue( OgnlParser p, int id )
    {
        super( p, id );
    }

    protected Node getKey()
    {
        return children[0];
    }

    protected Node getValue()
    {
        return ( jjtGetNumChildren() > 1 ) ? children[1] : null;
    }

    /**
     * Returns null because this is a parser construct and does not evaluate
     */
    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        return null;
    }

    public <R, P> R accept( NodeVisitor<? extends R, ? super P> visitor, P data )
        throws OgnlException
    {
        return visitor.visit( this, data );
    }
}
