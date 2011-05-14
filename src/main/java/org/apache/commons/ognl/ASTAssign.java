/*
 * $Id$
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
package org.apache.commons.ognl;

import org.apache.commons.ognl.enhance.OrderedReturn;
import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
class ASTAssign
    extends SimpleNode
{
    public ASTAssign( int id )
    {
        super( id );
    }

    public ASTAssign( OgnlParser p, int id )
    {
        super( p, id );
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        Object result = _children[1].getValue( context, source );
        _children[0].setValue( context, source, result );
        return result;
    }

    public String toString()
    {
        return _children[0] + " = " + _children[1];
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        String result = "";

        String first = _children[0].toGetSourceString( context, target );
        String second = "";

        if ( ASTProperty.class.isInstance( _children[1] ) )
        {
            second += "((" + OgnlRuntime.getCompiler().getClassName( target.getClass() ) + ")$2).";
        }

        second += _children[1].toGetSourceString( context, target );

        if ( ASTSequence.class.isAssignableFrom( _children[1].getClass() ) )
        {
            ASTSequence seq = (ASTSequence) _children[1];

            context.setCurrentType( Object.class );

            String core = seq.getCoreExpression();
            if ( core.endsWith( ";" ) )
                core = core.substring( 0, core.lastIndexOf( ";" ) );

            second =
                OgnlRuntime.getCompiler().createLocalReference( context,
                                                                "org.apache.commons.ognl.OgnlOps.returnValue(($w)"
                                                                    + core + ", ($w) " + seq.getLastExpression() + ")",
                                                                Object.class );
        }

        if ( NodeType.class.isInstance( _children[1] ) && !ASTProperty.class.isInstance( _children[1] )
            && ( (NodeType) _children[1] ).getGetterClass() != null && !OrderedReturn.class.isInstance( _children[1] ) )
        {

            second = "new " + ( (NodeType) _children[1] ).getGetterClass().getName() + "(" + second + ")";
        }

        if ( OrderedReturn.class.isAssignableFrom( _children[0].getClass() )
            && ( (OrderedReturn) _children[0] ).getCoreExpression() != null )
        {
            context.setCurrentType( Object.class );

            result = first + second + ")";

            // System.out.println("building ordered ret from child[0] with result of:" + result);

            result =
                OgnlRuntime.getCompiler().createLocalReference( context,
                                                                "org.apache.commons.ognl.OgnlOps.returnValue(($w)"
                                                                    + result
                                                                    + ", ($w)"
                                                                    + ( (OrderedReturn) _children[0] ).getLastExpression()
                                                                    + ")", Object.class );
        }

        return result;
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        String result = "";

        result += _children[0].toSetSourceString( context, target );

        if ( ASTProperty.class.isInstance( _children[1] ) )
        {
            result += "((" + OgnlRuntime.getCompiler().getClassName( target.getClass() ) + ")$2).";
        }

        String value = _children[1].toSetSourceString( context, target );

        if ( value == null )
            throw new UnsupportedCompilationException(
                                                       "Value for assignment is null, can't enhance statement to bytecode." );

        if ( ASTSequence.class.isAssignableFrom( _children[1].getClass() ) )
        {
            ASTSequence seq = (ASTSequence) _children[1];
            result = seq.getCoreExpression() + result;
            value = seq.getLastExpression();
        }

        if ( NodeType.class.isInstance( _children[1] ) && !ASTProperty.class.isInstance( _children[1] )
            && ( (NodeType) _children[1] ).getGetterClass() != null )
        {

            value = "new " + ( (NodeType) _children[1] ).getGetterClass().getName() + "(" + value + ")";
        }

        return result + value + ")";
    }
}
