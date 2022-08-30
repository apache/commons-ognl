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
import org.apache.commons.ognl.enhance.OgnlExpressionCompiler;


/**
 */
class ASTMethodUtil
{

    private ASTMethodUtil()
    {
    }

    static String getParmString( OgnlContext context, Object root, Node child, Class prevType )
        throws OgnlException
    {
        String parmString = child.toGetSourceString( context, root );

        if ( parmString == null || parmString.trim().length() < 1 )
        {
            parmString = "null";
        }

        // to undo type setting of constants when used as method parameters
        if (child instanceof ASTConst)
        {
            context.setCurrentType( prevType );
        }

        parmString = ExpressionCompiler.getRootExpression( child, root, context ) + parmString;

        String cast = "";
        if ( ExpressionCompiler.shouldCast( child ) )
        {
            cast = (String) context.remove( ExpressionCompiler.PRE_CAST );
        }

        if ( cast == null )
        {
            cast = "";
        }

        if ( !(child instanceof ASTConst))
        {
            parmString = cast + parmString;
        }
        return parmString;
    }

    static Class getValueClass( OgnlContext context, Object root, Node child )
        throws OgnlException
    {
        Object value = child.getValue( context, root );
        Class valueClass = value != null ? value.getClass() : null;
        if ( NodeType.class.isAssignableFrom( child.getClass() ) )
        {
            valueClass = ( (NodeType) child ).getGetterClass();
        }
        return valueClass;
    }

    static String getParmString( OgnlContext context, Class parm, String parmString, Node child, Class valueClass,
                                 String endParam )
    {
        OgnlExpressionCompiler compiler = OgnlRuntime.getCompiler( context );
        if ( parm.isArray() )
        {
            parmString = compiler.createLocalReference( context, "(" + ExpressionCompiler.getCastString( parm )
                + ")org.apache.commons.ognl.OgnlOps#toArray(" + parmString + ", " + parm.getComponentType().getName()
                + endParam, parm );

        }
        else if ( parm.isPrimitive() )
        {
            Class wrapClass = OgnlRuntime.getPrimitiveWrapperClass( parm );

            parmString = compiler.createLocalReference( context, "((" + wrapClass.getName()
                + ")org.apache.commons.ognl.OgnlOps#convertValue(" + parmString + "," + wrapClass.getName()
                + ".class, true))." + OgnlRuntime.getNumericValueGetter( wrapClass ), parm );

        }
        else if ( parm != Object.class )
        {
            parmString = compiler.createLocalReference( context, "(" + parm.getName()
                + ")org.apache.commons.ognl.OgnlOps#convertValue(" + parmString + "," + parm.getName() + ".class)",
                                                        parm );

        }
        else if ( ( child instanceof NodeType && ( (NodeType) child ).getGetterClass() != null
            && Number.class.isAssignableFrom( ( (NodeType) child ).getGetterClass() ) ) || ( valueClass != null
            && valueClass.isPrimitive() ) )
        {
            parmString = " ($w) " + parmString;
        }
        else if ( valueClass != null && valueClass.isPrimitive() )
        {
            parmString = "($w) " + parmString;
        }
        return parmString;
    }
}
