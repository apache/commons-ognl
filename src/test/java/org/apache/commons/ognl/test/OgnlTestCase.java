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
package org.apache.commons.ognl.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;

import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.SimpleNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class OgnlTestCase
{

    protected OgnlContext _context;

    private String _expressionString;

    private SimpleNode _expression;

    private Object _expectedResult;

    private Object _root;

    protected boolean _compileExpressions = true;

    private boolean hasSetValue;

    private Object setValue;

    private boolean hasExpectedAfterSetResult;

    private Object expectedAfterSetResult;

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */

    /**
     * Returns true if object1 is equal to object2 in either the sense that they are the same object or, if both are
     * non-null if they are equal in the <CODE>equals()</CODE> sense.
     */
    public static boolean isEqual( Object object1, Object object2 )
    {
        boolean result = false;

        if ( object1 == object2 )
        {
            result = true;
        }
        else
        {
            if ( ( object1 != null ) && object1.getClass().isArray() )
            {
                if ( ( object2 != null ) && object2.getClass().isArray() && ( object2.getClass() == object1.getClass() ) )
                {
                    result = ( Array.getLength( object1 ) == Array.getLength( object2 ) );
                    if ( result )
                    {
                        for ( int i = 0, icount = Array.getLength( object1 ); result && ( i < icount ); i++ )
                        {
                            result = isEqual( Array.get( object1, i ), Array.get( object2, i ) );
                        }
                    }
                }
            }
            else
            {
                result = ( object1 != null ) && ( object2 != null ) && object1.equals( object2 );
            }
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public OgnlTestCase(String name, Object root, String expressionString, Object expectedResult)
    {
        this( name, root, expressionString, expectedResult, null, false, null, false);
    }

    public OgnlTestCase( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                         Object expectedAfterSetResult )
    {
        this( name, root, expressionString, expectedResult, setValue, setValue != null, expectedAfterSetResult,
              expectedAfterSetResult != null );
    }

    public OgnlTestCase( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                         boolean hasSetValue, Object expectedAfterSetResult, boolean hasExpectedAfterSetResult )
    {
        this._root = root;
        this._expressionString = expressionString;
        this._expectedResult = expectedResult;

        this.hasExpectedAfterSetResult = hasExpectedAfterSetResult;
        this.expectedAfterSetResult = expectedAfterSetResult;
        this.hasSetValue = hasSetValue;
        this.setValue = setValue;

    }

    /*
     * =================================================================== Public methods
     * ===================================================================
     */
    public String getExpressionDump( SimpleNode node )
    {
        StringWriter writer = new StringWriter();

        node.dump( new PrintWriter( writer ), "   " );
        return writer.toString();
    }

    public String getExpressionString()
    {
        return _expressionString;
    }

    public SimpleNode getExpression()
        throws Exception
    {
        if ( _expression == null )
        {
            _expression = (SimpleNode) Ognl.parseExpression( _expressionString );
        }

        if ( _compileExpressions )
        {
            _expression = (SimpleNode) Ognl.compileExpression( _context, _root, _expressionString );
        }

        return _expression;
    }

    public Object getExpectedResult()
    {
        return _expectedResult;
    }

    public static void assertEquals( Object expected, Object actual )
    {
        if ( expected != null && expected.getClass().isArray() && actual != null && actual.getClass().isArray() )
        {

            Assert.assertEquals( Array.getLength( expected ), Array.getLength( actual ) );

            int length = Array.getLength( expected );

            for ( int i = 0; i < length; i++ )
            {
                Object aexpected = Array.get( expected, i );
                Object aactual = Array.get( actual, i );

                if ( aexpected != null && aactual != null && Boolean.class.isAssignableFrom( aexpected.getClass() ) )
                {
                    Assert.assertEquals( aexpected.toString(), aactual.toString() );
                }
                else
                    OgnlTestCase.assertEquals( aexpected, aactual );
            }
        }
        else if ( expected != null && actual != null && Character.class.isInstance( expected )
            && Character.class.isInstance( actual ) )
        {

            Assert.assertEquals( ( (Character) expected ).charValue(), ( (Character) actual ).charValue() );
        }
        else
        {

            Assert.assertEquals( expected, actual );
        }
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    @Test
    public void runTest()
        throws Exception
    {
        Object testedResult = null;

        try
        {
            SimpleNode expr;

            testedResult = _expectedResult;
            expr = getExpression();

            assertEquals( _expectedResult, Ognl.getValue( expr, _context, _root ) );

            if ( hasSetValue )
            {
                testedResult = hasExpectedAfterSetResult ? expectedAfterSetResult : setValue;
                Ognl.setValue( expr, _context, _root, setValue );

                assertEquals( testedResult, Ognl.getValue( expr, _context, _root ) );
            }

        }
        catch ( Exception ex )
        {
            if ( RuntimeException.class.isInstance( ex ) && ex.getCause() != null
                && Exception.class.isAssignableFrom( ex.getCause().getClass() ) )
            {
                ex = (Exception) ex.getCause();
            }

            if ( testedResult instanceof Class )
            {
                Assert.assertTrue( Exception.class.isAssignableFrom( (Class<?>) testedResult ) );
            }
            else
            {
                throw ex;
            }
        }
    }

    @Before
    public void setUp()
    {
        _context = (OgnlContext) Ognl.createDefaultContext( null );
    }

}
