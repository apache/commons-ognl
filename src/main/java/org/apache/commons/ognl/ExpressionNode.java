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

import org.apache.commons.ognl.enhance.ExpressionCompiler;

public abstract class ExpressionNode
    extends SimpleNode
{

    private static final long serialVersionUID = 9076228016268317598L;

    public ExpressionNode( int i )
    {
        super( i );
    }

    public ExpressionNode( OgnlParser p, int i )
    {
        super( p, i );
    }

    /**
     * Returns true iff this node is constant without respect to the children.
     */
    @Override
    public boolean isNodeConstant( OgnlContext context )
        throws OgnlException
    {
        return false;
    }

    @Override
    public boolean isConstant( OgnlContext context )
        throws OgnlException
    {
        boolean result = isNodeConstant( context );

        if ( ( children != null ) && ( children.length > 0 ) )
        {
            result = true;
            for ( int i = 0; result && ( i < children.length ); ++i )
            {
                if ( children[i] instanceof SimpleNode )
                {
                    result = ( (SimpleNode) children[i] ).isConstant( context );
                }
                else
                {
                    result = false;
                }
            }
        }
        return result;
    }

    public String getExpressionOperator( int index )
    {
        throw new RuntimeException( "unknown operator for " + OgnlParserTreeConstants.jjtNodeName[id] );
    }

    @Override
    public String toGetSourceString( OgnlContext context, Object target )
    {
        StringBuilder result =
            new StringBuilder(
                ( parent == null || NumericExpression.class.isAssignableFrom( parent.getClass() ) ) ? "" : "(" );

        if ( ( children != null ) && ( children.length > 0 ) )
        {
            for ( int i = 0; i < children.length; ++i )
            {
                if ( i > 0 )
                {
                    result.append( " " ).append( getExpressionOperator( i ) ).append( " " );
                }

                String value = children[i].toGetSourceString( context, target );

                if ( ( ASTProperty.class.isInstance( children[i] ) || ASTMethod.class.isInstance( children[i] )
                    || ASTSequence.class.isInstance( children[i] ) || ASTChain.class.isInstance( children[i] ) )
                    && value != null && value.trim().length() > 0 )
                {

                    String pre = null;
                    if ( ASTMethod.class.isInstance( children[i] ) )
                    {
                        pre = (String) context.get( "_currentChain" );
                    }

                    if ( pre == null )
                    {
                        pre = "";
                    }

                    String cast = (String) context.remove( ExpressionCompiler.PRE_CAST );
                    if ( cast == null )
                    {
                        cast = "";
                    }

                    value =
                        cast + ExpressionCompiler.getRootExpression( children[i], context.getRoot(), context ) + pre
                            + value;
                }

                result.append( value );
            }
        }

        if ( parent != null && !NumericExpression.class.isAssignableFrom( parent.getClass() ) )
        {
            result.append( ")" );
        }

        return result.toString();
    }

    @Override
    public String toSetSourceString( OgnlContext context, Object target )
    {
        StringBuilder sourceStringBuilder = new StringBuilder( parent == null ? "" : "(" );

        if ( ( children != null ) && ( children.length > 0 ) )
        {
            for ( int i = 0; i < children.length; ++i )
            {
                if ( i > 0 )
                {
                    sourceStringBuilder.append( " " ).append( getExpressionOperator( i ) ).append( ' ' );
                }

                sourceStringBuilder.append( children[i].toSetSourceString( context, target ) );
            }
        }
        if ( parent != null )
        {
            sourceStringBuilder.append( ")" );
        }

        return sourceStringBuilder.toString();
    }
}
