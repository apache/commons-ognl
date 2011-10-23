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

import org.apache.commons.ognl.enhance.OrderedReturn;
import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

/**
 * $Id$
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTVarRef
    extends SimpleNode
    implements NodeType, OrderedReturn
{

    private String name;

    protected Class getterClass;

    protected String core;

    protected String last;

    public ASTVarRef( int id )
    {
        super( id );
    }

    public ASTVarRef( OgnlParser p, int id )
    {
        super( p, id );
    }

    void setName( String name )
    {
        this.name = name;
    }
    /**
     * Get the variable name.
     *
     * @return the variable name.
     * @since 4.0
     */
    String getName()
    {
        return name;
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        return context.get( name );
    }

    protected void setValueBody( OgnlContext context, Object target, Object value )
        throws OgnlException
    {
        context.put( name, value );
    }

    public Class getGetterClass()
    {
        return getterClass;
    }

    public Class getSetterClass()
    {
        return null;
    }

    public String getCoreExpression()
    {
        return core;
    }

    public String getLastExpression()
    {
        return last;
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        Object value = context.get( name );

        if ( value != null )
        {

            getterClass = value.getClass();
        }

        context.setCurrentType( getterClass );
        context.setCurrentAccessor( context.getClass() );

        context.setCurrentObject( value );
        // context.setRoot(context.get(name));

        if ( context.getCurrentObject() == null )
        {
            throw new UnsupportedCompilationException( "Current context object is null, can't compile var reference." );
        }
        
        String pre = "";
        String post = "";
        if ( context.getCurrentType() != null )
        {
            pre = "((" + OgnlRuntime.getCompiler( context ).getInterfaceClass( context.getCurrentType() ).getName() + ")";
            post = ")";
        }

        if ( _parent != null && ASTAssign.class.isInstance( _parent ) )
        {
            core = "$1.put(\"" + name + "\",";
            last = pre + "$1.get(\"" + name + "\")" + post;

            return core;
        }

        return pre + "$1.get(\"" + name + "\")" + post;
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
}
