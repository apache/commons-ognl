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

import org.apache.commons.ognl.OgnlException;
import org.apache.commons.ognl.test.objects.Simple;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class NumberFormatExceptionTest
    extends OgnlTestCase
{
    private static final Simple SIMPLE = new Simple();

    private static final Object[][] TESTS = {
        // NumberFormatException handling (default is to throw NumberFormatException on bad string conversions)
        { SIMPLE, "floatValue", 0f, 10f, 10f },
        { SIMPLE, "floatValue", 10f, "x10x", OgnlException.class },

        { SIMPLE, "intValue", 0, 34, 34 },
        { SIMPLE, "intValue", 34, "foobar", OgnlException.class },
        { SIMPLE, "intValue", 34, "", OgnlException.class },
        { SIMPLE, "intValue", 34, "       \t", OgnlException.class },
        { SIMPLE, "intValue", 34, "       \t1234\t\t", 1234 },

        { SIMPLE, "bigIntValue", BigInteger.valueOf( 0 ), BigInteger.valueOf( 34 ), BigInteger.valueOf( 34 ) },
        { SIMPLE, "bigIntValue", BigInteger.valueOf( 34 ), null, null },
        { SIMPLE, "bigIntValue", null, "", OgnlException.class },
        { SIMPLE, "bigIntValue", null, "foobar", OgnlException.class },

        { SIMPLE, "bigDecValue", new BigDecimal( 0.0 ), new BigDecimal( 34.55 ), new BigDecimal( 34.55 ) },
        { SIMPLE, "bigDecValue", new BigDecimal( 34.55 ), null, null },
        { SIMPLE, "bigDecValue", null, "", OgnlException.class },
        { SIMPLE, "bigDecValue", null, "foobar", OgnlException.class }

    };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        Collection<Object[]> data = new ArrayList<Object[]>(TESTS.length);
        for ( Object[] TEST : TESTS )
        {
            Object[] tmp = new Object[6];
            tmp[0] = TEST[1];
            tmp[1] = TEST[0];
            tmp[2] = TEST[1];

            switch ( TEST.length )
            {
                case 3:
                    tmp[3] = TEST[2];
                    break;

                case 4:
                    tmp[3] = TEST[2];
                    tmp[4] = TEST[3];
                    break;

                case 5:
                    tmp[3] = TEST[2];
                    tmp[4] = TEST[3];
                    tmp[5] = TEST[4];
                    break;

                default:
                    throw new RuntimeException( "don't understand TEST format with length " + TEST.length );
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public NumberFormatExceptionTest( String name, Object root, String expressionString, Object expectedResult,
                                      Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, true, expectedAfterSetResult, true );
    }
}
