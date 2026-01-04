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
package org.apache.commons.ognl.test;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class ArithmeticAndLogicalOperatorsTest
    extends OgnlTestCase
{

    private static final Object[][] TESTS = {
        // Double-valued arithmetic expressions
        { "-1d", new Double( -1 ) },
        { "+1d", new Double( 1 ) },
        { "--1f", new Double( 1 ) },
        { "2*2.0", new Double( 4.0 ) },
        { "5/2.", new Double( 2.5 ) },
        { "5+2D", new Double( 7 ) },
        { "5f-2F", new Double( 3.0 ) },
        { "5.+2*3", new Double( 11 ) },
        { "(5.+2)*3", new Double( 21 ) },

        // BigDecimal-valued arithmetic expressions
        { "-1b", new Integer( -1 ) },
        { "+1b", new Integer( 1 ) },
        { "--1b", new Integer( 1 ) },
        { "2*2.0b", new Double( 4.0 ) },
        { "5/2.B", new Integer( 2 ) },
        { "5.0B/2", new Double( 2.5 ) },
        { "5+2b", new Integer( 7 ) },
        { "5-2B", new Integer( 3 ) },
        { "5.+2b*3", new Double( 11 ) },
        { "(5.+2b)*3", new Double( 21 ) },

        // Integer-valued arithmetic expressions
        { "-1", new Integer( -1 ) },
        { "+1", new Integer( 1 ) },
        { "--1", new Integer( 1 ) },
        { "2*2", new Integer( 4 ) },
        { "5/2", new Integer( 2 ) },
        { "5+2", new Integer( 7 ) },
        { "5-2", new Integer( 3 ) },
        { "5+2*3", new Integer( 11 ) },
        { "(5+2)*3", new Integer( 21 ) },
        { "~1", new Integer( ~1 ) },
        { "5%2", new Integer( 1 ) },
        { "5<<2", new Integer( 20 ) },
        { "5>>2", new Integer( 1 ) },
        { "5>>1+1", new Integer( 1 ) },
        { "-5>>>2", new Integer( -5 >>> 2 ) },
        { "-5L>>>2", new Long( -5L >>> 2 ) },
        { "5. & 3", new Long( 1 ) },
        { "5 ^3", new Integer( 6 ) },
        { "5l&3|5^3", new Long( 7 ) },
        { "5&(3|5^3)", new Long( 5 ) },
        { "true ? 1 : 1/0", new Integer( 1 ) },

        // BigInteger-valued arithmetic expressions
        { "-1h", Integer.valueOf( -1 ) },
        { "+1H", Integer.valueOf( 1 ) },
        { "--1h", Integer.valueOf( 1 ) },
        { "2h*2", Integer.valueOf( 4 ) },
        { "5/2h", Integer.valueOf( 2 ) },
        { "5h+2", Integer.valueOf( 7 ) },
        { "5-2h", Integer.valueOf( 3 ) },
        { "5+2H*3", Integer.valueOf( 11 ) },
        { "(5+2H)*3", Integer.valueOf( 21 ) },
        { "~1h", Integer.valueOf( ~1 ) },
        { "5h%2", Integer.valueOf( 1 ) },
        { "5h<<2", Integer.valueOf( 20 ) },
        { "5h>>2", Integer.valueOf( 1 ) },
        { "5h>>1+1", Integer.valueOf( 1 ) },
        { "-5h>>>2", Integer.valueOf( -2 ) },
        { "5.b & 3", new Long( 1 ) },
        { "5h ^3", Integer.valueOf( 6 ) },
        { "5h&3|5^3", new Long( 7 ) },
        { "5H&(3|5^3)", new Long( 5 ) },

        // Logical expressions
        { "!1", Boolean.FALSE }, { "!null", Boolean.TRUE },
        { "5<2", Boolean.FALSE },
        { "5>2", Boolean.TRUE },
        { "5<=5", Boolean.TRUE },
        { "5>=3", Boolean.TRUE },
        { "5<-5>>>2", Boolean.TRUE },
        { "5==5.0", Boolean.TRUE },
        { "5!=5.0", Boolean.FALSE },
        { "null in {true,false,null}", Boolean.TRUE },
        { "null not in {true,false,null}", Boolean.FALSE },
        { "null in {true,false,null}.toArray()", Boolean.TRUE },
        { "5 in {true,false,null}", Boolean.FALSE },
        { "5 not in {true,false,null}", Boolean.TRUE },
        { "5 instanceof java.lang.Integer", Boolean.TRUE },
        { "5. instanceof java.lang.Integer", Boolean.FALSE },
        { "!false || true", Boolean.TRUE },
        { "!(true && true)", Boolean.FALSE },
        { "(1 > 0 && true) || 2 > 0", Boolean.TRUE },

        // Logical expressions (string versions)
        { "2 or 0", Integer.valueOf( 2 ) }, { "1 and 0", Integer.valueOf( 0 ) }, { "1 bor 0", new Integer( 1 ) },
        { "true && 12", Integer.valueOf( 12 ) }, { "1 xor 0", new Integer( 1 ) }, { "1 band 0", new Long( 0 ) },
        { "1 eq 1", Boolean.TRUE }, { "1 neq 1", Boolean.FALSE }, { "1 lt 5", Boolean.TRUE },
        { "1 lte 5", Boolean.TRUE }, { "1 gt 5", Boolean.FALSE }, { "1 gte 5", Boolean.FALSE },
        { "1 lt 5", Boolean.TRUE }, { "1 shl 2", new Integer( 4 ) }, { "4 shr 2", new Integer( 1 ) },
        { "4 ushr 2", new Integer( 1 ) }, { "not null", Boolean.TRUE }, { "not 1", Boolean.FALSE },

        { "#x > 0", Boolean.TRUE }, { "#x < 0", Boolean.FALSE }, { "#x == 0", Boolean.FALSE },
        { "#x == 1", Boolean.TRUE }, { "0 > #x", Boolean.FALSE }, { "0 < #x", Boolean.TRUE },
        { "0 == #x", Boolean.FALSE }, { "1 == #x", Boolean.TRUE }, { "\"1\" > 0", Boolean.TRUE },
        { "\"1\" < 0", Boolean.FALSE }, { "\"1\" == 0", Boolean.FALSE }, { "\"1\" == 1", Boolean.TRUE },
        { "0 > \"1\"", Boolean.FALSE }, { "0 < \"1\"", Boolean.TRUE }, { "0 == \"1\"", Boolean.FALSE },
        { "1 == \"1\"", Boolean.TRUE }, { "#x + 1", "11" }, { "1 + #x", "11" }, { "#y == 1", Boolean.TRUE },
        { "#y == \"1\"", Boolean.TRUE }, { "#y + \"1\"", "11" }, { "\"1\" + #y", "11" } };

    /*
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        Collection<Object[]> data = new ArrayList<Object[]>(TESTS.length);
        for ( Object[] TEST : TESTS )
        {
            Object[] tmp = new Object[4];
            tmp[0] = TEST[0] + " (" + TEST[1] + ")";
            tmp[1] = null;
            tmp[2] = TEST[0];
            tmp[3] = TEST[1];

            data.add( tmp );
        }
        return data;
    }

    /*
     */

    public ArithmeticAndLogicalOperatorsTest( String name, Object root, String expressionString, Object expectedResult )
    {
        super( name, root, expressionString, expectedResult );
    }
    /*
     */
    @Before
    @Override
    public void setUp()
    {
        super.setUp();
        _context.put( "x", "1" );
        _context.put("y", new BigDecimal(1));
    }
}
