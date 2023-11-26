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
class ASTLessEq
    extends ComparisonExpression
{
    public ASTLessEq( int id )
    {
        super( id );
    }

    public ASTLessEq( OgnlParser p, int id )
    {
        super( p, id );
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        Object v1 = children[0].getValue( context, source );
        Object v2 = children[1].getValue( context, source );
        return OgnlOps.greater( v1, v2 ) ? Boolean.FALSE : Boolean.TRUE;
    }

    public String getExpressionOperator( int index )
    {
        return "<=";
    }

    public String getComparisonFunction()
    {
        return "!org.apache.commons.ognl.OgnlOps.greater";
    }

    public <R, P> R accept( NodeVisitor<? extends R, ? super P> visitor, P data )
        throws OgnlException
    {
        return visitor.visit( this, data );
    }
}
