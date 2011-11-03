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
import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

import java.lang.reflect.Array;

/**
 * $Id$
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTChain
    extends SimpleNode
    implements NodeType, OrderedReturn
{

    private Class getterClass;

    private Class setterClass;

    private String lastExpression;

    private String coreExpression;

    public ASTChain( int id )
    {
        super( id );
    }

    public ASTChain( OgnlParser p, int id )
    {
        super( p, id );
    }

    public String getLastExpression()
    {
        return lastExpression;
    }

    public String getCoreExpression()
    {
        return coreExpression;
    }

    public void jjtClose()
    {
        flattenTree();
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        Object result = source;

        for ( int i = 0, ilast = children.length - 1; i <= ilast; ++i )
        {
            boolean handled = false;

            if ( i < ilast )
            {
                if ( children[i] instanceof ASTProperty )
                {
                    ASTProperty propertyNode = (ASTProperty) children[i];
                    int indexType = propertyNode.getIndexedPropertyType( context, result );

                    if ( ( indexType != OgnlRuntime.INDEXED_PROPERTY_NONE )
                        && ( children[i + 1] instanceof ASTProperty ) )
                    {
                        ASTProperty indexNode = (ASTProperty) children[i + 1];

                        if ( indexNode.isIndexedAccess() )
                        {
                            Object index = indexNode.getProperty( context, result );

                            if ( index instanceof DynamicSubscript )
                            {
                                if ( indexType == OgnlRuntime.INDEXED_PROPERTY_INT )
                                {
                                    Object array = propertyNode.getValue( context, result );
                                    int len = Array.getLength( array );

                                    switch ( ( (DynamicSubscript) index ).getFlag() )
                                    {
                                        case DynamicSubscript.ALL:
                                            result = Array.newInstance( array.getClass().getComponentType(), len );
                                            System.arraycopy( array, 0, result, 0, len );
                                            handled = true;
                                            i++;
                                            break;
                                        case DynamicSubscript.FIRST:
                                            index = ( len > 0 ) ? 0 : -1;
                                            break;
                                        case DynamicSubscript.MID:
                                            index = ( len > 0 ) ? ( len / 2 ) : -1;
                                            break;
                                        case DynamicSubscript.LAST:
                                            index = ( len > 0 ) ? ( len - 1 ) : -1;
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                else
                                {
                                    if ( indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT )
                                    {
                                        throw new OgnlException( "DynamicSubscript '" + indexNode
                                            + "' not allowed for object indexed property '" + propertyNode + "'" );
                                    }
                                }
                            }
                            if ( !handled )
                            {
                                result =
                                    OgnlRuntime.getIndexedProperty( 
                                        context,
                                        result,
                                        propertyNode.getProperty( context, result ).toString(),
                                        index );
                                handled = true;
                                i++;
                            }
                        }
                    }
                }
            }
            if ( !handled )
            {
                result = children[i].getValue( context, result );
            }
        }
        return result;
    }

    protected void setValueBody( OgnlContext context, Object target, Object value )
        throws OgnlException
    {
        boolean handled = false;

        for ( int i = 0, ilast = children.length - 2; i <= ilast; ++i )
        {
            if ( i <= ilast )
            {
                if ( children[i] instanceof ASTProperty )
                {
                    ASTProperty propertyNode = (ASTProperty) children[i];
                    int indexType = propertyNode.getIndexedPropertyType( context, target );

                    if ( ( indexType != OgnlRuntime.INDEXED_PROPERTY_NONE )
                        && ( children[i + 1] instanceof ASTProperty ) )
                    {
                        ASTProperty indexNode = (ASTProperty) children[i + 1];

                        if ( indexNode.isIndexedAccess() )
                        {
                            Object index = indexNode.getProperty( context, target );

                            if ( index instanceof DynamicSubscript )
                            {
                                if ( indexType == OgnlRuntime.INDEXED_PROPERTY_INT )
                                {
                                    Object array = propertyNode.getValue( context, target );
                                    int len = Array.getLength( array );

                                    switch ( ( (DynamicSubscript) index ).getFlag() )
                                    {
                                        case DynamicSubscript.ALL:
                                            System.arraycopy( target, 0, value, 0, len );
                                            handled = true;
                                            i++;
                                            break;
                                        case DynamicSubscript.FIRST:
                                            index = ( len > 0 ) ? 0 : -1;
                                            break;
                                        case DynamicSubscript.MID:
                                            index = ( len > 0 ) ? ( len / 2 ) : -1;
                                            break;
                                        case DynamicSubscript.LAST:
                                            index = ( len > 0 ) ? ( len - 1 ) : -1;
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                else
                                {
                                    if ( indexType == OgnlRuntime.INDEXED_PROPERTY_OBJECT )
                                    {
                                        throw new OgnlException( "DynamicSubscript '" + indexNode
                                            + "' not allowed for object indexed property '" + propertyNode + "'" );
                                    }
                                }
                            }
                            if ( !handled && i == ilast )
                            {
                                OgnlRuntime.setIndexedProperty( context, target,
                                                                propertyNode.getProperty( context, target ).toString(),
                                                                index, value );
                                handled = true;
                                i++;
                            }
                            else if ( !handled )
                            {
                                target =
                                    OgnlRuntime.getIndexedProperty( 
                                        context,
                                        target,
                                        propertyNode.getProperty( context, target ).toString(),
                                        index );
                                i++;
                                continue;
                            }
                        }
                    }
                }
            }
            if ( !handled )
            {
                target = children[i].getValue( context, target );
            }
        }
        if ( !handled )
        {
            children[children.length - 1].setValue( context, target, value );
        }
    }

    public boolean isSimpleNavigationChain( OgnlContext context )
        throws OgnlException
    {
        boolean result = false;

        if ( ( children != null ) && ( children.length > 0 ) )
        {
            result = true;
            for ( int i = 0; result && ( i < children.length ); i++ )
            {
                result =
                    children[i] instanceof SimpleNode && ( (SimpleNode) children[i] ).isSimpleProperty( context );
            }
        }
        return result;
    }

    public Class getGetterClass()
    {
        return getterClass;
    }

    public Class getSetterClass()
    {
        return setterClass;
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        String prevChain = (String) context.get( "_currentChain" );

        if ( target != null )
        {
            context.setCurrentObject( target );
            context.setCurrentType( target.getClass() );
        }

        String result = "";
        NodeType lastType = null;
        boolean ordered = false;
        boolean constructor = false;
        try
        {
            if ( ( children != null ) && ( children.length > 0 ) )
            {
                for ( int i = 0; i < children.length; i++ )
                {
                    /*
                     * System.out.println("astchain child: " + _children[i].getClass().getName() +
                     * " with current object target " + context.getCurrentObject() + " current type: " +
                     * context.getCurrentType());
                     */

                    String value = children[i].toGetSourceString( context, context.getCurrentObject() );

                    // System.out.println("astchain child returned >>  " + value + "  <<");

                    if ( ASTCtor.class.isInstance( children[i] ) )
                    {
                        constructor = true;
                    }
                    
                    if ( NodeType.class.isInstance( children[i] )
                        && ( (NodeType) children[i] ).getGetterClass() != null )
                    {
                        lastType = (NodeType) children[i];
                    }

                    // System.out.println("Astchain i: " + i + " currentobj : " + context.getCurrentObject() +
                    // " and root: " + context.getRoot());
                    if ( !ASTVarRef.class.isInstance( children[i] )
                        && !constructor
                        && !( OrderedReturn.class.isInstance( children[i] )
                        && ( (OrderedReturn) children[i] ).getLastExpression() != null )
                        && ( parent == null || !ASTSequence.class.isInstance( parent ) ) )
                    {
                        value = OgnlRuntime.getCompiler( context ).castExpression( context, children[i], value );
                    }

                    /*
                     * System.out.println("astchain value now : " + value + " with index " + i + " current type " +
                     * context.getCurrentType() + " current accessor " + context.getCurrentAccessor() + " prev type " +
                     * context.getPreviousType() + " prev accessor " + context.getPreviousAccessor());
                     */

                    if ( OrderedReturn.class.isInstance( children[i] )
                        && ( (OrderedReturn) children[i] ).getLastExpression() != null )
                    {
                        ordered = true;
                        OrderedReturn or = (OrderedReturn) children[i];

                        if ( or.getCoreExpression() == null || or.getCoreExpression().trim().length() <= 0 )
                        {
                            result = "";
                        }
                        else
                        {
                            result += or.getCoreExpression();
                        }
                        
                        lastExpression = or.getLastExpression();

                        if ( context.get( ExpressionCompiler.PRE_CAST ) != null )
                        {
                            lastExpression = context.remove( ExpressionCompiler.PRE_CAST ) + lastExpression;
                        }
                    }
                    else if ( ASTOr.class.isInstance( children[i] ) || ASTAnd.class.isInstance( children[i] )
                        || ASTCtor.class.isInstance( children[i] )
                        || ( ASTStaticField.class.isInstance( children[i] ) && parent == null ) )
                    {
                        context.put( "_noRoot", "true" );
                        result = value;
                    }
                    else
                    {
                        result += value;
                    }

                    context.put( "_currentChain", result );
                }
            }
        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        if ( lastType != null )
        {
            getterClass = lastType.getGetterClass();
            setterClass = lastType.getSetterClass();
        }

        if ( ordered )
        {
            coreExpression = result;
        }

        context.put( "_currentChain", prevChain );

        return result;
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        String prevChain = (String) context.get( "_currentChain" );
        String prevChild = (String) context.get( "_lastChild" );

        if ( prevChain != null )
        {
            throw new UnsupportedCompilationException( "Can't compile nested chain expressions." );
        }
        
        if ( target != null )
        {
            context.setCurrentObject( target );
            context.setCurrentType( target.getClass() );
        }

        String result = "";
        NodeType lastType = null;
        boolean constructor = false;
        try
        {
            if ( ( children != null ) && ( children.length > 0 ) )
            {
                if ( ASTConst.class.isInstance( children[0] ) )
                {
                    throw new UnsupportedCompilationException( "Can't modify constant values." );
                }

                for ( int i = 0; i < children.length; i++ )
                {
                    // System.out.println("astchain setsource child[" + i + "] : " + _children[i].getClass().getName());

                    if ( i == ( children.length - 1 ) )
                    {
                        context.put( "_lastChild", "true" );
                    }

                    String value = children[i].toSetSourceString( context, context.getCurrentObject() );
                    // if (value == null || value.trim().length() <= 0)
                    // return "";

                    // System.out.println("astchain setter child returned >>  " + value + "  <<");

                    if ( ASTCtor.class.isInstance( children[i] ) )
                    {
                        constructor = true;
                    }
                    
                    if ( NodeType.class.isInstance( children[i] )
                        && ( (NodeType) children[i] ).getGetterClass() != null )
                    {
                        lastType = (NodeType) children[i];
                    }

                    if ( !ASTVarRef.class.isInstance( children[i] )
                        && !constructor
                        && !( OrderedReturn.class.isInstance( children[i] )
                        && ( (OrderedReturn) children[i] ).getLastExpression() != null )
                        && ( parent == null || !ASTSequence.class.isInstance( parent ) ) )
                    {
                        value = OgnlRuntime.getCompiler( context ).castExpression( context, children[i], value );
                    }

                    // System.out.println("astchain setter after cast value is: " + value);

                    /*
                     * if (!constructor && !OrderedReturn.class.isInstance(_children[i]) && (_parent == null ||
                     * !ASTSequence.class.isInstance(_parent))) { value =
                     * OgnlRuntime.getCompiler().castExpression(context, _children[i], value); }
                     */

                    if ( ASTOr.class.isInstance( children[i] ) || ASTAnd.class.isInstance( children[i] )
                        || ASTCtor.class.isInstance( children[i] ) || ASTStaticField.class.isInstance( children[i] ) )
                    {
                        context.put( "_noRoot", "true" );
                        result = value;
                    }
                    else
                    {
                        result += value;
                    }
                    
                    context.put( "_currentChain", result );
                }
            }
        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        context.put( "_lastChild", prevChild );
        context.put( "_currentChain", prevChain );

        if ( lastType != null )
        {
            setterClass = lastType.getSetterClass();
        }
        
        return result;
    }
    
    public <R, P> R accept( NodeVisitor<? extends R, ? super P> visitor, P data )
        throws OgnlException
    {
        return visitor.visit( this, data );
    }
}
