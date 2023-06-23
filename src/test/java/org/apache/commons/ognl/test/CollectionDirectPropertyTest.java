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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.ognl.test.objects.Root;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class CollectionDirectPropertyTest
    extends OgnlTestCase
{

    private static final Root ROOT = new Root();

    private static final Object[][] TESTS = {
        // Collection direct properties
        { Arrays.asList( "hello", "world" ), "size", 2 },
        { Arrays.asList( "hello", "world" ), "isEmpty", Boolean.FALSE },
        { Arrays.asList(), "isEmpty", Boolean.TRUE },
        { Arrays.asList( "hello", "world" ), "iterator.next", "hello" },
        { Arrays.asList( "hello", "world" ), "iterator.hasNext", Boolean.TRUE },
        { Arrays.asList( "hello", "world" ), "#it = iterator, #it.next, #it.next, #it.hasNext", Boolean.FALSE },
        { Arrays.asList( "hello", "world" ), "#it = iterator, #it.next, #it.next", "world" },
        { Arrays.asList( "hello", "world" ), "size", 2 },
        { ROOT, "map[\"test\"]", ROOT }, { ROOT, "map.size", ROOT.getMap().size() },
        { ROOT, "map.keySet", ROOT.getMap().keySet() }, { ROOT, "map.values", ROOT.getMap().values() },
        { ROOT, "map.keys.size", ROOT.getMap().size() },
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
                    fail( "don't understand TEST format with length" );
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
