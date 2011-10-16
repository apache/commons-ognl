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

import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

/**
 * $Id$
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTConst
    extends SimpleNode
    implements NodeType
{

    private Object value;

    private Class getterClass;

    public ASTConst( int id )
    {
        super( id );
    }

    public ASTConst( OgnlParser p, int id )
    {
        super( p, id );
    }

    /** 
     * Called from parser actions.
     * @param value the value to set 
     */
    public void setValue( Object value )
    {
        this.value = value;
    }

    public Object getValue()
    {
        return value;
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        return this.value;
    }

    public boolean isNodeConstant( OgnlContext context )
        throws OgnlException
    {
        return true;
    }

    public Class getGetterClass()
    {
        if ( getterClass == null )
        {
            return null;
        }
        
        return getterClass;
    }

    public Class getSetterClass()
    {
        return null;
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        if ( value == null && _parent != null && ExpressionNode.class.isInstance( _parent ) )
        {
            context.setCurrentType( null );
            return "null";
        }
        else if ( value == null )
        {
            context.setCurrentType( null );
            return "";
        }

        getterClass = value.getClass();

        Object retval = value;
        if ( _parent != null && ASTProperty.class.isInstance( _parent ) )
        {
            context.setCurrentObject( value );

            return value.toString();
        }
        else if ( value != null && Number.class.isAssignableFrom( value.getClass() ) )
        {
            context.setCurrentType( OgnlRuntime.getPrimitiveWrapperClass( value.getClass() ) );
            context.setCurrentObject( value );

            return value.toString();
        }
        else if ( !( _parent != null 
                        && value != null 
                        && NumericExpression.class.isAssignableFrom( _parent.getClass() ) )
            && String.class.isAssignableFrom( value.getClass() ) )
        {
            context.setCurrentType( String.class );

            retval = '\"' + OgnlOps.getEscapeString( value.toString() ) + '\"';

            context.setCurrentObject( retval.toString() );

            return retval.toString();
        }
        else if ( Character.class.isInstance( value ) )
        {
            Character val = (Character) value;

            context.setCurrentType( Character.class );

            if ( Character.isLetterOrDigit( val.charValue() ) )
            {
                retval = "'" + ( (Character) value ).charValue() + "'";
            }
            else
            {
                retval = "'" + OgnlOps.getEscapedChar( ( (Character) value ).charValue() ) + "'";
            }
            
            context.setCurrentObject( retval );
            return retval.toString();
        }

        if ( Boolean.class.isAssignableFrom( value.getClass() ) )
        {
            getterClass = Boolean.TYPE;

            context.setCurrentType( Boolean.TYPE );
            context.setCurrentObject( value );

            return value.toString();
        }

        return value.toString();
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        if ( _parent == null )
        {
            throw new UnsupportedCompilationException( "Can't modify constant values." );
        }
        
        return toGetSourceString( context, target );
    }
    
    public <R, P> R accept( NodeVisitor<? extends R, ? super P> visitor, P data ) 
    {
        return visitor.visit( this, data );
    }
}
