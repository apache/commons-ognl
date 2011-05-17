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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class CollectionDirectPropertyTest
    extends OgnlTestCase
{

    private static Root ROOT = new Root();

    private static Object[][] TESTS = {
        // Collection direct properties
        { Arrays.asList( new String[] { "hello", "world" } ), "size", new Integer( 2 ) },
        { Arrays.asList( new String[] { "hello", "world" } ), "isEmpty", Boolean.FALSE },
        { Arrays.asList( new String[] {} ), "isEmpty", Boolean.TRUE },
        { Arrays.asList( new String[] { "hello", "world" } ), "iterator.next", "hello" },
        { Arrays.asList( new String[] { "hello", "world" } ), "iterator.hasNext", Boolean.TRUE },
        { Arrays.asList( new String[] { "hello", "world" } ), "#it = iterator, #it.next, #it.next, #it.hasNext",
            Boolean.FALSE },
        { Arrays.asList( new String[] { "hello", "world" } ), "#it = iterator, #it.next, #it.next", "world" },
        { Arrays.asList( new String[] { "hello", "world" } ), "size", new Integer( 2 ) },
        { ROOT, "map[\"test\"]", ROOT }, { ROOT, "map.size", new Integer( ROOT.getMap().size() ) },
        { ROOT, "map.keySet", ROOT.getMap().keySet() }, { ROOT, "map.values", ROOT.getMap().values() },
        { ROOT, "map.keys.size", new Integer( ROOT.getMap().keySet().size() ) },
        { ROOT, "map[\"size\"]", ROOT.getMap().get( "size" ) },
        { ROOT, "map.isEmpty", ROOT.getMap().isEmpty() ? Boolean.TRUE : Boolean.FALSE },
        { ROOT, "map[\"isEmpty\"]", null }, };

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
    public CollectionDirectPropertyTest( String name, Object root, String expressionString, Object expectedResult,
                                         Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
