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

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
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

        if ( ( _children != null ) && ( _children.length > 0 ) )
        {
            result = true;
            for ( int i = 0; result && ( i < _children.length ); ++i )
            {
                if ( _children[i] instanceof SimpleNode )
                {
                    result = ( (SimpleNode) _children[i] ).isConstant( context );
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
        throw new RuntimeException( "unknown operator for " + OgnlParserTreeConstants.jjtNodeName[_id] );
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder( _parent == null ? "" : "(" );

        if ( ( _children != null ) && ( _children.length > 0 ) )
        {
            for ( int i = 0; i < _children.length; ++i )
            {
                if ( i > 0 )
                {
                    result.append( " " ).append( getExpressionOperator( i ) ).append( " " );
                }
                result.append( _children[i].toString() );
            }
        }
        if ( _parent != null )
        {
            result.append( ')' );
        }
        return result.toString();
    }

    @Override
    public String toGetSourceString( OgnlContext context, Object target )
    {
        StringBuilder result =
            new StringBuilder(
                               ( _parent == null || NumericExpression.class.isAssignableFrom( _parent.getClass() ) ) ? ""
                                               : "(" );

        if ( ( _children != null ) && ( _children.length > 0 ) )
        {
            for ( int i = 0; i < _children.length; ++i )
            {
                if ( i > 0 )
                {
                    result.append( " " ).append( getExpressionOperator( i ) ).append( " " );
                }

                String value = _children[i].toGetSourceString( context, target );

                if ( ( ASTProperty.class.isInstance( _children[i] ) || ASTMethod.class.isInstance( _children[i] )
                    || ASTSequence.class.isInstance( _children[i] ) || ASTChain.class.isInstance( _children[i] ) )
                    && value != null && value.trim().length() > 0 )
                {

                    String pre = null;
                    if ( ASTMethod.class.isInstance( _children[i] ) )
                    {
                        pre = (String) context.get( "_currentChain" );
                    }

                    if ( pre == null )
                        pre = "";

                    String cast = (String) context.remove( ExpressionCompiler.PRE_CAST );
                    if ( cast == null )
                        cast = "";

                    value =
                        cast + ExpressionCompiler.getRootExpression( _children[i], context.getRoot(), context ) + pre
                            + value;
                }

                result.append( value );
            }
        }

        if ( _parent != null && !NumericExpression.class.isAssignableFrom( _parent.getClass() ) )
        {
            result.append( ")" );
        }

        return result.toString();
    }

    @Override
    public String toSetSourceString( OgnlContext context, Object target )
    {
        String result = ( _parent == null ) ? "" : "(";

        if ( ( _children != null ) && ( _children.length > 0 ) )
        {
            for ( int i = 0; i < _children.length; ++i )
            {
                if ( i > 0 )
                {
                    result += " " + getExpressionOperator( i ) + " ";
                }

                result += _children[i].toSetSourceString( context, target );
            }
        }
        if ( _parent != null )
        {
            result = result + ")";
        }

        return result;
    }
}
