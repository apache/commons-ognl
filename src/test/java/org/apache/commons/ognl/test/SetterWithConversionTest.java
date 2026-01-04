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
import java.util.Collection;

import org.apache.commons.ognl.test.objects.Root;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class SetterWithConversionTest
    extends OgnlTestCase
{
    private static final Root ROOT = new Root();

    private static final Object[][] TESTS = {
        // Property set with conversion
        { ROOT, "intValue", new Integer( 0 ), new Double( 6.5 ), new Integer( 6 ) },
        { ROOT, "intValue", new Integer( 6 ), new Double( 1025.87645 ), new Integer( 1025 ) },
        { ROOT, "intValue", new Integer( 1025 ), "654", new Integer( 654 ) },
        { ROOT, "stringValue", null, new Integer( 25 ), "25" },
        { ROOT, "stringValue", "25", new Float( 100.25 ), "100.25" },
        { ROOT, "anotherStringValue", "foo", new Integer( 0 ), "0" },
        { ROOT, "anotherStringValue", "0", new Double( 0.5 ), "0.5" },
        { ROOT, "anotherIntValue", new Integer( 123 ), "5", new Integer( 5 ) },
        { ROOT, "anotherIntValue", new Integer( 5 ), new Double( 100.25 ), new Integer( 100 ) },
    // { ROOT, "anotherIntValue", new Integer(100), new String[] { "55" }, new Integer(55)},
    // { ROOT, "yetAnotherIntValue", new Integer(46), new String[] { "55" }, new Integer(55)},

        };

    /*
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

            switch (TEST.length) {
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
                    fail("don't understand TEST format with length " + TEST.length);
            }

            data.add(tmp);
        }
        return data;
    }

    /*
     */
    public SetterWithConversionTest( String name, Object root, String expressionString, Object expectedResult,
                                     Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
