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

import org.apache.commons.ognl.test.objects.Root;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class ArrayElementsTest
    extends OgnlTestCase
{

    private static final String[] STRING_ARRAY = new String[] { "hello", "world" };

    private static final int[] INT_ARRAY = new int[] { 10, 20 };

    private static final Root ROOT = new Root();

    private static final Object[][] TESTS = {
        // Array elements test
        { STRING_ARRAY, "length", 2 },
        { STRING_ARRAY, "#root[1]", "world" },
        { INT_ARRAY, "#root[1]", 20 },
        { INT_ARRAY, "#root[1]", 20, "50", 50 },
        { INT_ARRAY, "#root[1]", 50, new String[] { "50", "100" }, 50 },
        { ROOT, "intValue", 0, new String[] { "50", "100" }, 50 },
        { ROOT, "array", ROOT.getArray(), new String[] { "50", "100" }, new int[] { 50, 100 } },
        { null, "\"{Hello}\".toCharArray()[6]", '}' },
        { null, "\"Tapestry\".toCharArray()[2]", 'p' },
        { null, "{'1','2','3'}",
            Arrays.asList( '1', '2', '3' ) },
        { null, "{ true, !false }", Arrays.asList( Boolean.TRUE, Boolean.TRUE ) } };

    /*
     * =================================================================== Private static methods
     * ===================================================================
     */
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
                    throw new RuntimeException( "don't understand TEST format with length" );
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ArrayElementsTest( String name, Object root, String expressionString, Object expectedResult,
                              Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        /**
         * TypeConverter arrayConverter;
         * arrayConverter = new DefaultTypeConverter() { public Object convertValue(Map context, Object target, Member
         * member, String propertyName, Object value, Class toType) { if (value.getClass().isArray()) { if
         * (!toType.isArray()) { value = Array.get(value, 0); } } return super.convertValue(context, target, member,
         * propertyName, value, toType); } }; _context.setTypeConverter(arrayConverter);
         */
    }
}
