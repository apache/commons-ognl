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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class ProjectionSelectionTest
    extends OgnlTestCase
{
    private static final Root ROOT = new Root();

    private static final Object[][] TESTS = {
        // Projection, selection
        { ROOT, "array.{class}",
            Arrays.asList( Integer.class, Integer.class, Integer.class, Integer.class ) },
        { ROOT, "map.array.{? #this > 2 }", Arrays.asList( new Integer( 3 ), new Integer( 4 ) ) },
        { ROOT, "map.array.{^ #this > 2 }", Arrays.asList( new Integer( 3 ) ) },
        { ROOT, "map.array.{$ #this > 2 }", Arrays.asList( new Integer( 4 ) ) },
        { ROOT, "map.array[*].{?true} instanceof java.util.Collection", Boolean.TRUE },
        { null, "#fact=1, 30H.{? #fact = #fact * (#this+1), false }, #fact",
            new BigInteger( "265252859812191058636308480000000" ) }, };

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
            tmp[3] = TESTS[i][2];

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ProjectionSelectionTest( String name, Object root, String expressionString, Object expectedResult,
                                    Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
