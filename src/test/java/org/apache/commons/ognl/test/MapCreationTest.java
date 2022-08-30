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

import org.apache.commons.ognl.test.objects.Root;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

@RunWith(value = Parameterized.class)
public class MapCreationTest
    extends OgnlTestCase
{

    private static final Root ROOT = new Root();

    private static final Map FOO_BAR_MAP_1;

    private static final Map FOO_BAR_MAP_2;

    private static final Map FOO_BAR_MAP_3;

    private static final Map FOO_BAR_MAP_4;

    private static final Map FOO_BAR_MAP_5;

    static
    {
        FOO_BAR_MAP_1 = new HashMap();
        FOO_BAR_MAP_1.put( "foo", "bar" );
        FOO_BAR_MAP_2 = new HashMap();
        FOO_BAR_MAP_2.put( "foo", "bar" );
        FOO_BAR_MAP_2.put( "bar", "baz" );
        FOO_BAR_MAP_3 = new HashMap();
        FOO_BAR_MAP_3.put( "foo", null );
        FOO_BAR_MAP_3.put( "bar", "baz" );
        FOO_BAR_MAP_4 = new LinkedHashMap();
        FOO_BAR_MAP_4.put( "foo", "bar" );
        FOO_BAR_MAP_4.put( "bar", "baz" );
        FOO_BAR_MAP_5 = new TreeMap();
        FOO_BAR_MAP_5.put( "foo", "bar" );
        FOO_BAR_MAP_5.put( "bar", "baz" );
    }

    private static final Object[][] TESTS = {
        // Map creation
        { ROOT, "#{ \"foo\" : \"bar\" }", FOO_BAR_MAP_1 },
        { ROOT, "#{ \"foo\" : \"bar\", \"bar\" : \"baz\"  }", FOO_BAR_MAP_2 },
        { ROOT, "#{ \"foo\", \"bar\" : \"baz\"  }", FOO_BAR_MAP_3 },
        { ROOT, "#@java.util.LinkedHashMap@{ \"foo\" : \"bar\", \"bar\" : \"baz\"  }", FOO_BAR_MAP_4 },
        { ROOT, "#@java.util.TreeMap@{ \"foo\" : \"bar\", \"bar\" : \"baz\"  }", FOO_BAR_MAP_5 },

    };

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
            tmp[0] = TESTS[i][1];
            tmp[1] = TESTS[i][0];
            tmp[2] = TESTS[i][1];

            switch ( TESTS[i].length )
            {
                case 3:
                    tmp[3] = TESTS[i][2];
                    break;

                case 4:
                    tmp[3] = TESTS[i][2];
                    tmp[4] = TESTS[i][3];
                    break;

                case 5:
                    tmp[3] = TESTS[i][2];
                    tmp[4] = TESTS[i][3];
                    tmp[5] = TESTS[i][4];
                    break;

                default:
                    throw new RuntimeException( "don't understand TEST format with length " + TESTS[i].length );
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public MapCreationTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                            Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
