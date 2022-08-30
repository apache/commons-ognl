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

import org.apache.commons.ognl.enhance.ExpressionCompiler;
import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

import static java.lang.String.format;

/**
 */
public class ASTAnd
    extends BooleanExpression
{
    /** Serial */
    private static final long serialVersionUID = -4585941425250141812L;

    /**
     * TODO: javadoc
     * @param id the id
     */
    public ASTAnd( int id )
    {
        super( id );
    }

    /**
     * TODO: javadoc
     * @param p the parser
     * @param id the id
     */
    public ASTAnd( OgnlParser p, int id )
    {
        super( p, id );
    }

    /* (non-Javadoc)
     * @see org.apache.commons.ognl.SimpleNode#jjtClose()
     */
    public void jjtClose()
    {
        flattenTree();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.ognl.SimpleNode#getValueBody(org.apache.commons.ognl.OgnlContext, Object)
     */
    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        Object result = null;
        int last = children.length - 1;
        for ( int i = 0; i <= last; ++i )
        {
            result = children[i].getValue( context, source );

            if ( i != last && !OgnlOps.booleanValue( result ) )
            {
                break;
            }
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.ognl.SimpleNode#setValueBody(org.apache.commons.ognl.OgnlContext, Object, Object)
     */
    protected void setValueBody( OgnlContext context, Object target, Object value )
        throws OgnlException
    {
        int last = children.length - 1;

        for ( int i = 0; i < last; ++i )
        {
            Object v = children[i].getValue( context, target );

            if ( !OgnlOps.booleanValue( v ) )
            {
                return;
            }
        }

        children[last].setValue( context, target, value );
    }

    /* (non-Javadoc)
     * @see org.apache.commons.ognl.ExpressionNode#getExpressionOperator(int)
     */
    public String getExpressionOperator( int index )
    {
        return "&&";
    }

    /* (non-Javadoc)
     * @see org.apache.commons.ognl.BooleanExpression#getGetterClass()
     */
    public Class getGetterClass()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.ognl.BooleanExpression#toGetSourceString(org.apache.commons.ognl.OgnlContext, Object)
     */
    public String toGetSourceString( OgnlContext context, Object target )
    {
        if ( children.length != 2 )
        {
            throw new UnsupportedCompilationException(
                "Can only compile boolean expressions with two children." );
        }

        String result = "";

        try
        {

            String first = OgnlRuntime.getChildSource( context, target, children[0] );
            if ( !OgnlOps.booleanValue( context.getCurrentObject() ) )
            {
                throw new UnsupportedCompilationException(
                    "And expression can't be compiled until all conditions are true." );
            }

            if ( !OgnlRuntime.isBoolean( first ) && !context.getCurrentType().isPrimitive() )
            {
                first = OgnlRuntime.getCompiler( context ).createLocalReference( context, first, context.getCurrentType() );
            }

            String second = OgnlRuntime.getChildSource( context, target, children[1] );
            if ( !OgnlRuntime.isBoolean( second ) && !context.getCurrentType().isPrimitive() )
            {
                second = OgnlRuntime.getCompiler( context ).createLocalReference( context, second, context.getCurrentType() );
            }

            result += format( "(org.apache.commons.ognl.OgnlOps.booleanValue(%s) ?  ($w) (%s) :  ($w) (%s))", first, second, first );

            context.setCurrentObject( target );
            context.setCurrentType( Object.class );
        }
        catch ( NullPointerException e )
        {

            throw new UnsupportedCompilationException( "evaluation resulted in null expression." );
        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.ognl.ExpressionNode#toSetSourceString(org.apache.commons.ognl.OgnlContext, Object)
     */
    public String toSetSourceString( OgnlContext context, Object target )
    {
        if ( children.length != 2 )
        {
            throw new UnsupportedCompilationException( "Can only compile boolean expressions with two children." );
        }

        String pre = (String) context.get( "_currentChain" );
        if ( pre == null )
        {
            pre = "";
        }

        String result = "";

        try
        {

            if ( !OgnlOps.booleanValue( children[0].getValue( context, target ) ) )
            {
                throw new UnsupportedCompilationException(
                    "And expression can't be compiled until all conditions are true." );
            }

            String first =
                ExpressionCompiler.getRootExpression( children[0], context.getRoot(), context ) + pre
                    + children[0].toGetSourceString( context, target );

            children[1].getValue( context, target );

            String second =
                ExpressionCompiler.getRootExpression( children[1], context.getRoot(), context ) + pre
                    + children[1].toSetSourceString( context, target );

            if ( !OgnlRuntime.isBoolean( first ) )
            {
                result += "if(org.apache.commons.ognl.OgnlOps.booleanValue(" + first + ")){";
            }
            else
            {
                result += "if(" + first + "){";
            }

            result += second;
            result += "; } ";

            context.setCurrentObject( target );
            context.setCurrentType( Object.class );

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.ognl.Node#accept(org.apache.commons.ognl.NodeVisitor, Object)
     */
    public <R, P> R accept( NodeVisitor<? extends R, ? super P> visitor, P data )
        throws OgnlException
    {
        return visitor.visit( this, data );
    }
}
