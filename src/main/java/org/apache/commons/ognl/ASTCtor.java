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

import org.apache.commons.ognl.enhance.ExpressionCompiler;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTCtor
    extends SimpleNode
{

    private String className;

    private boolean isArray;

    public ASTCtor( int id )
    {
        super( id );
    }

    public ASTCtor( OgnlParser p, int id )
    {
        super( p, id );
    }

    /** Called from parser action. */
    void setClassName( String className )
    {
        this.className = className;
    }

    void setArray( boolean value )
    {
        isArray = value;
    }

    public boolean isArray()
    {
        return isArray;
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        Object result, root = context.getRoot();
        int count = jjtGetNumChildren();
        Object[] args = OgnlRuntime.getObjectArrayPool().create( count );

        try
        {
            for ( int i = 0; i < count; ++i )
            {
                args[i] = _children[i].getValue( context, root );
            }
            if ( isArray )
            {
                if ( args.length == 1 )
                {
                    try
                    {
                        Class componentClass = OgnlRuntime.classForName( context, className );
                        List sourceList = null;
                        int size;

                        if ( args[0] instanceof List )
                        {
                            sourceList = (List) args[0];
                            size = sourceList.size();
                        }
                        else
                        {
                            size = (int) OgnlOps.longValue( args[0] );
                        }
                        result = Array.newInstance( componentClass, size );
                        if ( sourceList != null )
                        {
                            TypeConverter converter = context.getTypeConverter();

                            for ( int i = 0, icount = sourceList.size(); i < icount; i++ )
                            {
                                Object o = sourceList.get( i );

                                if ( ( o == null ) || componentClass.isInstance( o ) )
                                {
                                    Array.set( result, i, o );
                                }
                                else
                                {
                                    Array.set( result, i,
                                               converter.convertValue( context, null, null, null, o, componentClass ) );
                                }
                            }
                        }
                    }
                    catch ( ClassNotFoundException ex )
                    {
                        throw new OgnlException( "array component class '" + className + "' not found", ex );
                    }
                }
                else
                {
                    throw new OgnlException( "only expect array size or fixed initializer list" );
                }
            }
            else
            {
                result = OgnlRuntime.callConstructor( context, className, args );
            }

            return result;
        }
        finally
        {
            OgnlRuntime.getObjectArrayPool().recycle( args );
        }
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder( "new " ).append( className );

        if ( isArray )
        {
            if ( _children[0] instanceof ASTConst )
            {
                result.append( "[" ).append( _children[0] ).append( "]" );
            }
            else
            {
                result.append( "[] " ).append( _children[0] );
            }
        }
        else
        {
            result.append( "(" );
            if ( ( _children != null ) && ( _children.length > 0 ) )
            {
                for ( int i = 0; i < _children.length; i++ )
                {
                    if ( i > 0 )
                    {
                        result.append( ", " );
                    }
                    result.append( _children[i] );
                }
            }
            result.append( ")" );
        }
        return result.toString();
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        String result = "new " + className;

        Class clazz = null;
        Object ctorValue = null;
        try
        {

            clazz = OgnlRuntime.classForName( context, className );

            ctorValue = this.getValueBody( context, target );
            context.setCurrentObject( ctorValue );

            if ( clazz != null && ctorValue != null )
            {

                context.setCurrentType( ctorValue.getClass() );
                context.setCurrentAccessor( ctorValue.getClass() );
            }

            if ( isArray )
                context.put( "_ctorClass", clazz );

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        try
        {

            if ( isArray )
            {
                if ( _children[0] instanceof ASTConst )
                {

                    result = result + "[" + _children[0].toGetSourceString( context, target ) + "]";
                }
                else if ( ASTProperty.class.isInstance( _children[0] ) )
                {

                    result =
                        result + "[" + ExpressionCompiler.getRootExpression( _children[0], target, context )
                            + _children[0].toGetSourceString( context, target ) + "]";
                }
                else if ( ASTChain.class.isInstance( _children[0] ) )
                {

                    result = result + "[" + _children[0].toGetSourceString( context, target ) + "]";
                }
                else
                {

                    result = result + "[] " + _children[0].toGetSourceString( context, target );
                }

            }
            else
            {
                result = result + "(";

                if ( ( _children != null ) && ( _children.length > 0 ) )
                {

                    Object[] values = new Object[_children.length];
                    String[] expressions = new String[_children.length];
                    Class[] types = new Class[_children.length];

                    // first populate arrays with child values

                    for ( int i = 0; i < _children.length; i++ )
                    {

                        Object objValue = _children[i].getValue( context, context.getRoot() );
                        String value = _children[i].toGetSourceString( context, target );

                        if ( !ASTRootVarRef.class.isInstance( _children[i] ) )
                        {
                            value = ExpressionCompiler.getRootExpression( _children[i], target, context ) + value;
                        }

                        String cast = "";
                        if ( ExpressionCompiler.shouldCast( _children[i] ) )
                        {

                            cast = (String) context.remove( ExpressionCompiler.PRE_CAST );
                        }
                        if ( cast == null )
                            cast = "";

                        if ( !ASTConst.class.isInstance( _children[i] ) )
                            value = cast + value;

                        values[i] = objValue;
                        expressions[i] = value;
                        types[i] = context.getCurrentType();
                    }

                    // now try and find a matching constructor

                    Constructor[] cons = clazz.getConstructors();
                    Constructor ctor = null;
                    Class[] ctorParamTypes = null;

                    for ( int i = 0; i < cons.length; i++ )
                    {
                        Class[] ctorTypes = cons[i].getParameterTypes();

                        if ( OgnlRuntime.areArgsCompatible( values, ctorTypes )
                            && ( ctor == null || OgnlRuntime.isMoreSpecific( ctorTypes, ctorParamTypes ) ) )
                        {
                            ctor = cons[i];
                            ctorParamTypes = ctorTypes;
                        }
                    }

                    if ( ctor == null )
                        ctor =
                            OgnlRuntime.getConvertedConstructorAndArgs( context, clazz,
                                                                        OgnlRuntime.getConstructors( clazz ), values,
                                                                        new Object[values.length] );

                    if ( ctor == null )
                        throw new NoSuchMethodException(
                                                         "Unable to find constructor appropriate for arguments in class: "
                                                             + clazz );

                    ctorParamTypes = ctor.getParameterTypes();

                    // now loop over child values again and build up the actual source string

                    for ( int i = 0; i < _children.length; i++ )
                    {
                        if ( i > 0 )
                        {
                            result = result + ", ";
                        }

                        String value = expressions[i];

                        if ( types[i].isPrimitive() )
                        {

                            String literal = OgnlRuntime.getNumericLiteral( types[i] );
                            if ( literal != null )
                                value += literal;
                        }

                        if ( ctorParamTypes[i] != types[i] )
                        {

                            if ( values[i] != null && !types[i].isPrimitive() && !values[i].getClass().isArray()
                                && !ASTConst.class.isInstance( _children[i] ) )
                            {

                                value =
                                    "(" + OgnlRuntime.getCompiler().getInterfaceClass( values[i].getClass() ).getName()
                                        + ")" + value;
                            }
                            else if ( !ASTConst.class.isInstance( _children[i] )
                                || ( ASTConst.class.isInstance( _children[i] ) && !types[i].isPrimitive() ) )
                            {

                                if ( !types[i].isArray() && types[i].isPrimitive() && !ctorParamTypes[i].isPrimitive() )
                                    value =
                                        "new "
                                            + ExpressionCompiler.getCastString( OgnlRuntime.getPrimitiveWrapperClass( types[i] ) )
                                            + "(" + value + ")";
                                else
                                    value = " ($w) " + value;
                            }
                        }

                        result += value;
                    }

                }
                result = result + ")";
            }

            context.setCurrentType( ctorValue != null ? ctorValue.getClass() : clazz );
            context.setCurrentAccessor( clazz );
            context.setCurrentObject( ctorValue );

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        context.remove( "_ctorClass" );

        return result;
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        return "";
    }
    
    public <R,P> R accept(NodeVisitor<? extends R, ? super P> visitor, P data) 
    {
        return visitor.visit(this, data);
    }
}
