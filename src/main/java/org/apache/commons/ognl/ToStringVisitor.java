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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A NodeVisitor implementation which will build a String representation of the AST tree.
 * <p/>
 * This class is meant to be used by SimpleNode.toString(), but you may use it to
 *
 * @since 4.0
 */
public class ToStringVisitor
    implements NodeVisitor<StringBuilder, StringBuilder>
{
    static final ToStringVisitor INSTANCE = new ToStringVisitor();

    public StringBuilder visit( ASTSequence node, StringBuilder data )
    {
        return commaSeparatedChildren( node, data );
    }

    private StringBuilder commaSeparatedChildren( SimpleNode node, StringBuilder data )
    {
        if ( ( node.children != null ) )
        {
            for ( int i = 0; i < node.children.length; ++i )
            {
                if ( i > 0 )
                {
                    data.append( ", " );
                }
                recurse( node.children[i], data );
            }
        }
        return data;
    }

    public StringBuilder visit( ASTAssign node, StringBuilder data )
    {
        return concatInfix( node, " = ", data );
    }

    public StringBuilder visit( ASTTest node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    private StringBuilder visitExpressionNode( ExpressionNode node, StringBuilder data )
    {
        if ( node.parent != null )
        {
            data.append( "(" );
        }

        if ( ( node.children != null ) && ( node.children.length > 0 ) )
        {
            for ( int i = 0; i < node.children.length; ++i )
            {
                if ( i > 0 )
                {
                    data.append( " " ).append( node.getExpressionOperator( i ) ).append( " " );
                }
                recurse( node.children[i], data );
            }
        }

        if ( node.parent != null )
        {
            data.append( ')' );
        }
        return data;
    }

    public StringBuilder visit( ASTOr node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTAnd node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTBitOr node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTXor node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTBitAnd node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTEq node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTNotEq node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTLess node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTGreater node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTLessEq node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTGreaterEq node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTIn node, StringBuilder data )
    {
        final String infix = " in ";
        return concatInfix( node, infix, data );
    }

    private StringBuilder concatInfix( SimpleNode node, String infix, StringBuilder data )
    {
        return concatInfix( node.children[0], infix, node.children[1], data );
    }

    private StringBuilder concatInfix( Node left, String infix, Node right, StringBuilder data )
    {
        recurse( left, data ).append( infix );
        return recurse( right, data );
    }

    public StringBuilder visit( ASTNotIn node, StringBuilder data )
    {
        return concatInfix( node, " not in ", data );
    }

    public StringBuilder visit( ASTShiftLeft node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTShiftRight node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTUnsignedShiftRight node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTAdd node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTSubtract node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTMultiply node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTDivide node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTRemainder node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTNegate node, StringBuilder data )
    {
        return appendPrefixed( "-", node, data );
    }

    public StringBuilder visit( ASTBitNegate node, StringBuilder data )
    {
        return appendPrefixed( "~", node, data );
    }

    private StringBuilder appendPrefixed( String prefix, SimpleNode node, StringBuilder data )
    {
        data.append( prefix );
        return recurse( node.children[0], data );
    }

    public StringBuilder visit( ASTNot node, StringBuilder data )
    {
        return visitExpressionNode( node, data );
    }

    public StringBuilder visit( ASTInstanceof node, StringBuilder data )
    {
        return recurse( node.children[0], data ).append( " instanceof " ).append( node.getTargetType() );
    }

    public StringBuilder visit( ASTChain node, StringBuilder data )
    {

        if ( ( node.children != null ) && ( node.children.length > 0 ) )
        {
            for ( int i = 0; i < node.children.length; i++ )
            {
                if ( i > 0 && !( node.children[i] instanceof ASTProperty )
                    || !( (ASTProperty) node.children[i] ).isIndexedAccess() )
                {
                    data.append( "." );
                }
                recurse( node.children[i], data );
            }
        }
        return data;
    }

    public StringBuilder visit( ASTEval node, StringBuilder data )
    {
        data.append( "(" );
        concatInfix( node, ")(", data );
        return data.append( ")" );
    }

    public StringBuilder visit( ASTConst node, StringBuilder data )
    {
        final Object value = node.getValue();
        if ( value == null )
        {
            data.append( "null" );
        }
        else
        {
            if ( value instanceof String )
            {
                data.append( '\"' ).append( OgnlOps.getEscapeString( value.toString() ) ).append( '\"' );
            }
            else
            {
                if ( value instanceof Character )
                {
                    data.append( '\'' ).append( OgnlOps.getEscapedChar( (Character) value ) ).append( '\'' );
                }
                else
                {
                    if ( value instanceof Node )
                    {
                        data.append( ":[ " );
                        recurse( (Node) value, data );
                        data.append( " ]" );
                    }
                    else
                    {
                        data.append( value );
                        if ( value instanceof Long )
                        {
                            data.append( 'L' );
                        }
                        else if ( value instanceof BigDecimal )
                        {
                            data.append( 'B' );
                        }
                        else if ( value instanceof BigInteger )
                        {
                            data.append( 'H' );
                        }
                    }
                }
            }
        }
        return data;
    }

    public StringBuilder visit( ASTThisVarRef node, StringBuilder data )
    {
        return data.append( "#this" );
    }

    public StringBuilder visit( ASTRootVarRef node, StringBuilder data )
    {
        return data.append( "#root" );
    }

    public StringBuilder visit( ASTVarRef node, StringBuilder data )
    {
        return data.append( "#" ).append( node.getName() );
    }

    public StringBuilder visit( ASTList node, StringBuilder data )
    {
        return wrappedCommaSeparatedChildren( "{ ", node, " }", data );
    }

    public StringBuilder visit( ASTMap node, StringBuilder data )
    {
        data.append( "#" );

        if ( node.getClassName() != null )
        {
            data.append( "@" ).append( node.getClassName() ).append( "@" );
        }

        data.append( "{ " );
        for ( int i = 0; i < node.jjtGetNumChildren(); ++i )
        {
            ASTKeyValue kv = (ASTKeyValue) node.children[i];

            if ( i > 0 )
            {
                data.append( ", " );
            }
            concatInfix( kv.getKey(), " : ", kv.getValue(), data );
        }
        return data.append( " }" );
    }

    public StringBuilder visit( ASTKeyValue node, StringBuilder data )
    {
        return concatInfix( node.getKey(), " -> ", node.getValue(), data );
    }

    public StringBuilder visit( ASTStaticField node, StringBuilder data )
    {
        return data.append( "@" ).append( node.getClassName() ).append( "@" ).append( node.getFieldName() );
    }

    public StringBuilder visit( ASTCtor node, StringBuilder data )
    {
        data.append( "new " ).append( node.getClassName() );

        if ( node.isArray() )
        {
            if ( node.children[0] instanceof ASTConst )
            {
                indexedChild( node, data );
            }
            else
            {
                appendPrefixed( "[] ", node, data );
            }
        }
        else
        {
            wrappedCommaSeparatedChildren( "(", node, ")", data );
        }
        return data;
    }

    private StringBuilder wrappedCommaSeparatedChildren( String prefix, SimpleNode node, String suffix,
                                                         StringBuilder data )
    {
        data.append( prefix );
        return commaSeparatedChildren( node, data ).append( suffix );
    }

    public StringBuilder visit( ASTProperty node, StringBuilder data )
    {
        if ( node.isIndexedAccess() )
        {
            indexedChild( node, data );
        }
        else
        {
            data.append( ( (ASTConst) node.children[0] ).getValue() );
        }
        return data;
    }

    private StringBuilder indexedChild( SimpleNode node, StringBuilder data )
    {
        return surroundedNode( "[", node.children[0], "]", data );
    }

    public StringBuilder visit( ASTStaticMethod node, StringBuilder data )
    {
        data.append( "@" ).append( node.getClassName() ).append( "@" ).append( node.getMethodName() );
        return wrappedCommaSeparatedChildren( "(", node, ")", data );
    }

    public StringBuilder visit( ASTMethod node, StringBuilder data )
    {
        data.append( node.getMethodName() );
        return wrappedCommaSeparatedChildren( "(", node, ")", data );
    }

    public StringBuilder visit( ASTProject node, StringBuilder data )
    {
        return surroundedNode( "{ ", node.children[0], " }", data );
    }

    private StringBuilder surroundedNode( String open, Node inner, String close, StringBuilder data )
    {
        data.append( open );
        return recurse( inner, data ).append( close );
    }

    public StringBuilder visit( ASTSelect node, StringBuilder data )
    {
        return surroundedNode( "{? ", node.children[0], " }", data );
    }

    public StringBuilder visit( ASTSelectFirst node, StringBuilder data )
    {
        return surroundedNode( "{^ ", node.children[0], " }", data );
    }

    public StringBuilder visit( ASTSelectLast node, StringBuilder data )
    {
        return surroundedNode( "{$ ", node.children[0], " }", data );
    }

    private StringBuilder recurse( Node child, StringBuilder data )
    {
        try
        {
            return child == null ? data.append( "null" ) : child.accept( this, data );
        }
        catch ( OgnlException e )
        {
            // This should never happen, but delegate it on just in case.
            throw new RuntimeException( e );
        }
    }

}
