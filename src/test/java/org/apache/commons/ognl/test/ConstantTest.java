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

import org.apache.commons.ognl.ExpressionSyntaxException;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class ConstantTest
    extends OgnlTestCase
{

    private static final Object[][] TESTS = {
        { "12345", new Integer( 12345 ) },
        { "0x100", new Integer( 256 ) },
        { "0xfE", new Integer( 254 ) },
        { "01000", new Integer( 512 ) },
        { "1234L", new Integer( 1234 ) },
        { "12.34", new Double( 12.34 ) },
        { ".1234", new Double( .12340000000000 ) },
        { "12.34f", Double.valueOf( 12.34 ) },
        { "12.", new Double( 12 ) },
        { "12e+1d", new Double( 120 ) },
        { "'x'", new Character( 'x' ) },
        { "'\\n'", new Character( '\n' ) },
        { "'\\u048c'", new Character( '\u048c' ) },
        { "'\\47'", new Character( '\47' ) },
        { "'\\367'", new Character( '\367' ) },
        { "'\\367", ExpressionSyntaxException.class },
        { "'\\x'", ExpressionSyntaxException.class },
        { "\"hello world\"", "hello world" },
        { "\"\\u00a0\\u0068ell\\'o\\\\\\n\\r\\f\\t\\b\\\"\\167orld\\\"\"", "\u00a0hell'o\\\n\r\f\t\b\"world\"" },
        { "\"hello world", ExpressionSyntaxException.class },
        { "\"hello\\x world\"", ExpressionSyntaxException.class },
        { "null", null },
        { "true", Boolean.TRUE },
        { "false", Boolean.FALSE },
        { "{ false, true, null, 0, 1. }",
            Arrays.asList( new Object[] { Boolean.FALSE, Boolean.TRUE, null, new Integer( 0 ), new Double( 1 ) } ) },
        { "'HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"'",
            "HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"" }, };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        Collection<Object[]> data = new ArrayList<Object[]>(TESTS.length);
        for ( int i = 0; i < TESTS.length; i++ )
        {
            Object[] tmp = new Object[6];
            tmp[0] = TESTS[i][0] + " (" + TESTS[i][1] + ")";
            tmp[1] = null;
            tmp[2] = TESTS[i][0];
            tmp[3] = TESTS[i][1];
            tmp[4] = null;
            tmp[5] = null;

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ConstantTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                         Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }

    @Before
    @Override
    public void setUp()
    {
        super.setUp();
        _context.put( "x", "1" );
        _context.put( "y", new BigDecimal( 1 ) );
    }
}
