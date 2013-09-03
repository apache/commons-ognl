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
import org.apache.commons.ognl.enhance.OgnlExpressionCompiler;
import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

import java.lang.reflect.Method;

/**
 * $Id$
 */
public class ASTStaticMethod
    extends SimpleNode
    implements NodeType
{

    private String className;

    private String methodName;

    private Class getterClass;

    public ASTStaticMethod( int id )
    {
        super( id );
    }

    public ASTStaticMethod( OgnlParser p, int id )
    {
        super( p, id );
    }

    /**
     * Called from parser action.
     */
    void init( String className, String methodName )
    {
        this.className = className;
        this.methodName = methodName;
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        Object[] args = new Object[jjtGetNumChildren()];
        Object root = context.getRoot();

        for ( int i = 0, icount = args.length; i < icount; ++i )
        {
            args[i] = children[i].getValue( context, root );
        }

        return OgnlRuntime.callStaticMethod( context, className, methodName, args );
    }

    public Class getGetterClass()
    {
        return getterClass;
    }

    public Class getSetterClass()
    {
        return getterClass;
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        String result = className + "#" + methodName + "(";

        try
        {
            Class clazz = OgnlRuntime.classForName( context, className );
            Method m = OgnlRuntime.getMethod( context, clazz, methodName, children, true );

            if ( m == null )
            {
                throw new UnsupportedCompilationException(
                    "Unable to find class/method combo " + className + " / " + methodName );
            }

            if ( !context.getMemberAccess().isAccessible( context, clazz, m, methodName ) )
            {
                throw new UnsupportedCompilationException(
                    "Method is not accessible, check your jvm runtime security settings. " + "For static class method "
                        + className + " / " + methodName );
            }

            OgnlExpressionCompiler compiler = OgnlRuntime.getCompiler( context );
            if ( ( children != null ) && ( children.length > 0 ) )
            {
                Class[] parms = m.getParameterTypes();

                for ( int i = 0; i < children.length; i++ )
                {
                    if ( i > 0 )
                    {
                        result = result + ", ";
                    }

                    Class prevType = context.getCurrentType();

                    Node child = children[i];
                    Object root = context.getRoot();

                    String parmString = ASTMethodUtil.getParmString( context, root, child, prevType );

                    Class valueClass = ASTMethodUtil.getValueClass( context, root, child );

                    if ( valueClass != parms[i] )
                    {
                        if ( parms[i].isArray() )
                        {
                            parmString = compiler.createLocalReference( context, "(" + ExpressionCompiler.getCastString(
                                parms[i] ) + ")org.apache.commons.ognl.OgnlOps.toArray(" + parmString + ", "
                                + parms[i].getComponentType().getName() + ".class, true)", parms[i] );

                        }
                        else if ( parms[i].isPrimitive() )
                        {
                            Class wrapClass = OgnlRuntime.getPrimitiveWrapperClass( parms[i] );

                            parmString = compiler.createLocalReference( context, "((" + wrapClass.getName()
                                + ")org.apache.commons.ognl.OgnlOps.convertValue(" + parmString + ","
                                + wrapClass.getName() + ".class, true))." + OgnlRuntime.getNumericValueGetter(
                                wrapClass ), parms[i] );

                        }
                        else if ( parms[i] != Object.class )
                        {
                            parmString = compiler.createLocalReference( context, "(" + parms[i].getName()
                                + ")org.apache.commons.ognl.OgnlOps.convertValue(" + parmString + ","
                                + parms[i].getName() + ".class)", parms[i] );
                        }
                        else if ( ( NodeType.class.isInstance( child ) && ( (NodeType) child ).getGetterClass() != null
                            && Number.class.isAssignableFrom( ( (NodeType) child ).getGetterClass() ) )
                            || valueClass.isPrimitive() )
                        {
                            parmString = " ($w) " + parmString;
                        }
                        else if ( valueClass.isPrimitive() )
                        {
                            parmString = "($w) " + parmString;
                        }
                    }

                    result += parmString;
                }
            }

            result += ")";

            try
            {
                Object contextObj = getValueBody( context, target );
                context.setCurrentObject( contextObj );
            }
            catch ( Throwable t )
            {
                // ignore
            }

            if ( m != null )
            {
                getterClass = m.getReturnType();

                context.setCurrentType( m.getReturnType() );
                context.setCurrentAccessor( compiler.getSuperOrInterfaceClass( m, m.getDeclaringClass() ) );
            }

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return result;
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        return toGetSourceString( context, target );
    }

    public <R, P> R accept( NodeVisitor<? extends R, ? super P> visitor, P data )
        throws OgnlException
    {
        return visitor.visit( this, data );
    }

    /**
     * Get the class name for this method.
     *
     * @return the class name.
     * @since 4.0
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Get the method name for this method.
     *
     * @return the method name.
     * @since 4.0
     */
    public String getMethodName()
    {
        return methodName;
    }
}
