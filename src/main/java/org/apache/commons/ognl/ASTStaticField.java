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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * $Id$
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTStaticField
    extends SimpleNode
    implements NodeType
{

    private String className;

    private String fieldName;

    private Class _getterClass;

    public ASTStaticField( int id )
    {
        super( id );
    }

    public ASTStaticField( OgnlParser p, int id )
    {
        super( p, id );
    }

    /** Called from parser action. */
    void init( String className, String fieldName )
    {
        this.className = className;
        this.fieldName = fieldName;
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        return OgnlRuntime.getStaticField( context, className, fieldName );
    }

    public boolean isNodeConstant( OgnlContext context )
        throws OgnlException
    {
        boolean result = false;
        Exception reason = null;

        try
        {
            Class c = OgnlRuntime.classForName( context, className );

            /*
             * Check for virtual static field "class"; this cannot interfere with normal static fields because it is a
             * reserved word. It is considered constant.
             */
            if ( fieldName.equals( "class" ) )
            {
                result = true;
            }
            else if ( OgnlRuntime.isJdk15() && c.isEnum() )
            {
                result = true;
            }
            else
            {
                Field f = c.getField( fieldName );

                if ( !Modifier.isStatic( f.getModifiers() ) )
                    throw new OgnlException( "Field " + fieldName + " of class " + className + " is not static" );

                result = Modifier.isFinal( f.getModifiers() );
            }
        }
        catch ( ClassNotFoundException e )
        {
            reason = e;
        }
        catch ( NoSuchFieldException e )
        {
            reason = e;
        }
        catch ( SecurityException e )
        {
            reason = e;
        }

        if ( reason != null )
            throw new OgnlException( "Could not get static field " + fieldName + " from class " + className, reason );

        return result;
    }

    Class getFieldClass( OgnlContext context )
        throws OgnlException
    {
        Exception reason = null;

        try
        {
            Class c = OgnlRuntime.classForName( context, className );

            /*
             * Check for virtual static field "class"; this cannot interfere with normal static fields because it is a
             * reserved word. It is considered constant.
             */
            if ( fieldName.equals( "class" ) )
            {
                return c;
            }
            else if ( OgnlRuntime.isJdk15() && c.isEnum() )
            {
                return c;
            }
            else
            {
                Field f = c.getField( fieldName );

                return f.getType();
            }
        }
        catch ( ClassNotFoundException e )
        {
            reason = e;
        }
        catch ( NoSuchFieldException e )
        {
            reason = e;
        }
        catch ( SecurityException e )
        {
            reason = e;
        }
        throw new OgnlException( "Could not get static field " + fieldName + " from class " + className, reason );
    }

    public Class getGetterClass()
    {
        return _getterClass;
    }

    public Class getSetterClass()
    {
        return _getterClass;
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        try
        {

            Object obj = OgnlRuntime.getStaticField( context, className, fieldName );

            context.setCurrentObject( obj );

            _getterClass = getFieldClass( context );

            context.setCurrentType( _getterClass );

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return className + "." + fieldName;
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        try
        {

            Object obj = OgnlRuntime.getStaticField( context, className, fieldName );

            context.setCurrentObject( obj );

            _getterClass = getFieldClass( context );

            context.setCurrentType( _getterClass );

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return className + "." + fieldName;
    }
    
    public <R, P> R accept( NodeVisitor<? extends R, ? super P> visitor, P data ) 
    {
        return visitor.visit( this, data );
    }

    /**
     * Get the field name for this field.
     *
     * @return the field name.
     * @since 4.0
     */
    String getFieldName()
    {
        return fieldName;
    }

    /**
     * Get the class name for this field.
     *
     * @return the class name.
     * @since 4.0
     */
    String getClassName()
    {
        return className;
    }
}
