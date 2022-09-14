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

import org.apache.commons.ognl.test.objects.Simple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class MethodWithConversionTest
    extends OgnlTestCase
{

    private static final Simple SIMPLE = new Simple();

    private static final Object[][] TESTS = {
        // Method call with conversion
        { SIMPLE, "setValues(new Integer(10), \"10.56\", new Double(34.225))", null }, { SIMPLE, "stringValue", "10" },
        { SIMPLE, "stringValue", "10", new Character( 'x' ), "x" },
        { SIMPLE, "setStringValue('x')", null }, // set by calling setStringValue() directly
        { SIMPLE, "floatValue", new Float( 10.56 ) }, { SIMPLE, "getValueIsTrue(rootValue)", Boolean.TRUE },
        { SIMPLE, "messages.format('Testing', one, two, three)", "blah" } };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        Collection<Object[]> data = new ArrayList<Object[]>(TESTS.length);
        for (Object[] element : TESTS) {
            Object[] tmp = new Object[6];
            tmp[0] = element[1];
            tmp[1] = element[0];
            tmp[2] = element[1];

            switch ( element.length )
            {
                case 3:
                    tmp[3] = element[2];
                    break;

                case 4:
                    tmp[3] = element[2];
                    tmp[4] = element[3];
                    break;

                case 5:
                    tmp[3] = element[2];
                    tmp[4] = element[3];
                    tmp[5] = element[4];
                    break;

                default:
                    throw new RuntimeException( "don't understand TEST format with length " + element.length );
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public MethodWithConversionTest( String name, Object root, String expressionString, Object expectedResult,
                                     Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }

    @Test

    @Override
    public void runTest()
        throws Exception
    {
        super.runTest();
    }
}
