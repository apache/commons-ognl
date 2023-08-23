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

import org.apache.commons.ognl.enhance.ExpressionAccessor;

import java.io.PrintWriter;
import java.io.Serializable;

public abstract class SimpleNode
    implements Node, Serializable
{

    private static final long serialVersionUID = 8305393337889433901L;

    protected Node parent;

    protected Node[] children;

    protected int id;

    protected OgnlParser parser;

    private boolean constantValueCalculated;

    private volatile boolean hasConstantValue;

    private Object constantValue;

    private ExpressionAccessor accessor;

    public SimpleNode( int i )
    {
        id = i;
    }

    public SimpleNode( OgnlParser p, int i )
    {
        this( i );
        parser = p;
    }

    public void jjtOpen()
    {
    }

    public void jjtClose()
    {
    }

    public void jjtSetParent( Node n )
    {
        parent = n;
    }

    public Node jjtGetParent()
    {
        return parent;
    }

    public void jjtAddChild( Node n, int i )
    {
        if ( children == null )
        {
            children = new Node[i + 1];
        }
        else if ( i >= children.length )
        {
            Node[] c = new Node[i + 1];
            System.arraycopy( children, 0, c, 0, children.length );
            children = c;
        }
        children[i] = n;
    }

    public Node jjtGetChild( int i )
    {
        return children[i];
    }

    public int jjtGetNumChildren()
    {
        return ( children == null ) ? 0 : children.length;
    }

    /*
     * You can override these two methods in subclasses of SimpleNode to customize the way the node appears when the
     * tree is dumped. If your output uses more than one line you should override toString(String), otherwise overriding
     * toString() is probably all you need to do.
     */

    @Override
    public String toString()
    {
        final StringBuilder data = new StringBuilder();
        try
        {
            accept( ToStringVisitor.INSTANCE, data );
        }
        catch ( OgnlException e )
        {
            // ignored.
        }
        return data.toString();
    }

    // OGNL additions

    public String toString( String prefix )
    {
        return prefix + OgnlParserTreeConstants.jjtNodeName[id] + " " + toString();
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        return toString();
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        return toString();
    }

    /*
     * Override this method if you want to customize how the node dumps out its children.
     */

    public void dump( PrintWriter writer, String prefix )
    {
        writer.println( toString( prefix ) );

        if ( children != null )
        {
            for (Node child : children) {
                SimpleNode n = (SimpleNode) child;
                if ( n != null )
                {
                    n.dump( writer, prefix + "  " );
                }
            }
        }
    }

    public int getIndexInParent()
    {
        int result = -1;

        if ( parent != null )
        {
            int icount = parent.jjtGetNumChildren();

            for ( int i = 0; i < icount; i++ )
            {
                if ( parent.jjtGetChild( i ) == this )
                {
                    result = i;
                    break;
                }
            }
        }

        return result;
    }

    public Node getNextSibling()
    {
        Node result = null;
        int i = getIndexInParent();

        if ( i >= 0 )
        {
            int icount = parent.jjtGetNumChildren();

            if ( i < icount )
            {
                result = parent.jjtGetChild( i + 1 );
            }
        }
        return result;
    }

    protected Object evaluateGetValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        context.setCurrentObject( source );
        context.setCurrentNode( this );

        if ( !constantValueCalculated )
        {
            constantValueCalculated = true;
            boolean constant = isConstant( context );

            if ( constant )
            {
                constantValue = getValueBody( context, source );
            }

            hasConstantValue = constant;
        }

        return hasConstantValue ? constantValue : getValueBody( context, source );
    }

    protected void evaluateSetValueBody( OgnlContext context, Object target, Object value )
        throws OgnlException
    {
        context.setCurrentObject( target );
        context.setCurrentNode( this );
        setValueBody( context, target, value );
    }

    public final Object getValue( OgnlContext context, Object source )
        throws OgnlException
    {
        Object result = null;

        if ( context.getTraceEvaluations() )
        {

            Throwable evalException = null;
            Evaluation evaluation = new Evaluation( this, source );

            context.pushEvaluation( evaluation );
            try
            {
                result = evaluateGetValueBody( context, source );
            }
            catch ( OgnlException | RuntimeException ex )
            {
                evalException = ex;
                throw ex;
            } finally
            {
                Evaluation eval = context.popEvaluation();

                eval.setResult( result );
                if ( evalException != null )
                {
                    eval.setException( evalException );
                }
            }
        }
        else
        {
            result = evaluateGetValueBody( context, source );
        }

        return result;
    }

    /**
     * Subclasses implement this method to do the actual work of extracting the appropriate value from the source
     * object.
     */
    protected abstract Object getValueBody( OgnlContext context, Object source )
        throws OgnlException;

    public final void setValue( OgnlContext context, Object target, Object value )
        throws OgnlException
    {
        if ( context.getTraceEvaluations() )
        {
            Throwable evalException = null;
            Evaluation evaluation = new Evaluation( this, target, true );

            context.pushEvaluation( evaluation );
            try
            {
                evaluateSetValueBody( context, target, value );
            }
            catch ( OgnlException ex )
            {
                evalException = ex;
                ex.setEvaluation( evaluation );
                throw ex;
            }
            catch ( RuntimeException ex )
            {
                evalException = ex;
                throw ex;
            }
            finally
            {
                Evaluation eval = context.popEvaluation();

                if ( evalException != null )
                {
                    eval.setException( evalException );
                }
            }
        }
        else
        {
            evaluateSetValueBody( context, target, value );
        }
    }

    /**
     * Subclasses implement this method to do the actual work of setting the appropriate value in the target object. The
     * default implementation throws an <code>InappropriateExpressionException</code>, meaning that it cannot be a set
     * expression.
     */
    protected void setValueBody( OgnlContext context, Object target, Object value )
        throws OgnlException
    {
        throw new InappropriateExpressionException( this );
    }

    /** Returns true iff this node is constant without respect to the children. */
    public boolean isNodeConstant( OgnlContext context )
        throws OgnlException
    {
        return false;
    }

    public boolean isConstant( OgnlContext context )
        throws OgnlException
    {
        return isNodeConstant( context );
    }

    public boolean isNodeSimpleProperty( OgnlContext context )
        throws OgnlException
    {
        return false;
    }

    public boolean isSimpleProperty( OgnlContext context )
        throws OgnlException
    {
        return isNodeSimpleProperty( context );
    }

    public boolean isSimpleNavigationChain( OgnlContext context )
        throws OgnlException
    {
        return isSimpleProperty( context );
    }

    public boolean isEvalChain( OgnlContext context )
        throws OgnlException
    {
        if ( children == null )
        {
            return false;
        }
        for ( Node child : children )
        {
            if ( child instanceof SimpleNode && ( (SimpleNode) child ).isEvalChain( context ) )
            {
                return true;
            }

        }
        return false;
    }

    protected boolean lastChild( OgnlContext context )
    {
        return parent == null || context.get( "_lastChild" ) != null;
    }

    /**
     * This method may be called from subclasses' jjtClose methods. It flattens the tree under this node by eliminating
     * any children that are of the same class as this node and copying their children to this node.
     */
    protected void flattenTree()
    {
        boolean shouldFlatten = false;
        int newSize = 0;

        for ( Node aChildren : children )
        {
            if ( aChildren.getClass() == getClass() )
            {
                shouldFlatten = true;
                newSize += aChildren.jjtGetNumChildren();
            }
            else
            {
                ++newSize;
            }
        }

        if ( shouldFlatten )
        {
            Node[] newChildren = new Node[newSize];
            int j = 0;

            for ( Node c : children )
            {
                if ( c.getClass() == getClass() )
                {
                    for ( int k = 0; k < c.jjtGetNumChildren(); ++k )
                    {
                        newChildren[j++] = c.jjtGetChild( k );
                    }

                }
                else
                {
                    newChildren[j++] = c;
                }
            }

            if ( j != newSize )
            {
                throw new Error( "Assertion error: " + j + " != " + newSize );
            }

            children = newChildren;
        }
    }

    public ExpressionAccessor getAccessor()
    {
        return accessor;
    }

    public void setAccessor( ExpressionAccessor accessor )
    {
        this.accessor = accessor;
    }
}
