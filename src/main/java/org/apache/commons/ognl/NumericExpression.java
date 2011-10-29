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
 * Base class for numeric expressions.
 */
public abstract class NumericExpression
    extends ExpressionNode
    implements NodeType
{

    private static final long serialVersionUID = -174952564587478850L;

    protected Class<?> getterClass;

    public NumericExpression( int id )
    {
        super( id );
    }

    public NumericExpression( OgnlParser p, int id )
    {
        super( p, id );
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getGetterClass()
    {
        if ( getterClass != null )
        {
            return getterClass;
        }

        return Double.TYPE;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getSetterClass()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toGetSourceString( OgnlContext context, Object target )
    {
        Object value;
        StringBuilder result = new StringBuilder( "" );

        try
        {

            value = getValueBody( context, target );

            if ( value != null )
            {
                getterClass = value.getClass();
            }

            for ( int i = 0; i < children.length; i++ )
            {
                if ( i > 0 )
                {
                    result.append( " " ).append( getExpressionOperator( i ) ).append( " " );
                }
                String str = OgnlRuntime.getChildSource( context, target, children[i] );

                result.append( coerceToNumeric( str, context, children[i] ) );
            }

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return result.toString();
    }

    public String coerceToNumeric( String source, OgnlContext context, Node child )
    {
        StringBuilder ret = new StringBuilder( source );
        Object value = context.getCurrentObject();

        if ( ASTConst.class.isInstance( child ) && value != null )
        {
            return value.toString();
        }

        if ( context.getCurrentType() != null && !context.getCurrentType().isPrimitive()
            && context.getCurrentObject() != null && Number.class.isInstance( context.getCurrentObject() ) )
        {
            ret = new StringBuilder( "((" ).append(
                ExpressionCompiler.getCastString( context.getCurrentObject().getClass() ) ).append( ")" ).append(
                ret ).append( ")." ).append(
                OgnlRuntime.getNumericValueGetter( context.getCurrentObject().getClass() ) );
        }
        else if ( context.getCurrentType() != null && context.getCurrentType().isPrimitive()
            && ( ASTConst.class.isInstance( child ) || NumericExpression.class.isInstance( child ) ) )
        {
            @SuppressWarnings( "unchecked" ) // checked by the condition in the if clause
            Class<? extends Number> numberClass = (Class<? extends Number>) context.getCurrentType();
            ret.append( OgnlRuntime.getNumericLiteral( numberClass ) );
        }
        else if ( context.getCurrentType() != null && String.class.isAssignableFrom( context.getCurrentType() ) )
        {
            ret = new StringBuilder( "Double.parseDouble(" )
                .append( ret )
                .append( ")" );
            context.setCurrentType( Double.TYPE );
        }

        if ( NumericExpression.class.isInstance( child ) )
        {
            ret = new StringBuilder( "(" ).append( ret ).append( ")" );
        }

        return ret.toString();
    }
}
