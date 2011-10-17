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
import org.apache.commons.ognl.enhance.OrderedReturn;

/**
 * $Id$
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTSequence
    extends SimpleNode
    implements NodeType, OrderedReturn
{
    private Class getterClass;

    private String lastExpression;

    private String coreExpression;

    public ASTSequence( int id )
    {
        super( id );
    }

    public ASTSequence( OgnlParser p, int id )
    {
        super( p, id );
    }

    public void jjtClose()
    {
        flattenTree();
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        Object result = null;
        for ( int i = 0; i < _children.length; ++i )
        {
            result = _children[i].getValue( context, source );
        }

        return result; // The result is just the last one we saw.
    }

    protected void setValueBody( OgnlContext context, Object target, Object value )
        throws OgnlException
    {
        int last = _children.length - 1;
        for ( int i = 0; i < last; ++i )
        {
            _children[i].getValue( context, target );
        }
        _children[last].setValue( context, target, value );
    }

    public Class getGetterClass()
    {
        return getterClass;
    }

    public Class getSetterClass()
    {
        return null;
    }

    public String getLastExpression()
    {
        return lastExpression;
    }

    public String getCoreExpression()
    {
        return coreExpression;
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        return "";
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        String result = "";

        NodeType lastType = null;

        for ( int i = 0; i < _children.length; ++i )
        {
            // System.out.println("astsequence child : " + _children[i].getClass().getName());
            String seqValue = _children[i].toGetSourceString( context, target );

            if ( ( i + 1 ) < _children.length && ASTOr.class.isInstance( _children[i] ) )
            {
                seqValue = "(" + seqValue + ")";
            }

            if ( i > 0 && ASTProperty.class.isInstance( _children[i] ) && seqValue != null
                && seqValue.trim().length() > 0 )
            {
                String pre = (String) context.get( "_currentChain" );
                if ( pre == null )
                {
                    pre = "";
                }
                
                seqValue =
                    ExpressionCompiler.getRootExpression( _children[i], context.getRoot(), context ) + pre + seqValue;
                context.setCurrentAccessor( context.getRoot().getClass() );
            }

            if ( ( i + 1 ) >= _children.length )
            {
                coreExpression = result;
                lastExpression = seqValue;
            }

            if ( seqValue != null && seqValue.trim().length() > 0 && ( i + 1 ) < _children.length )
            {
                result += seqValue + ";";
            }
            else if ( seqValue != null && seqValue.trim().length() > 0 )
            {
                result += seqValue;
            }
            // set last known type from last child with a type

            if ( NodeType.class.isInstance( _children[i] ) && ( (NodeType) _children[i] ).getGetterClass() != null )
            {
                lastType = (NodeType) _children[i];
            }
        }

        if ( lastType != null )
        {
            getterClass = lastType.getGetterClass();
        }

        return result;
    }
    
    public <R, P> R accept( NodeVisitor<? extends R, ? super P> visitor, P data )
        throws OgnlException
    {
        return visitor.visit( this, data );
    }
}
