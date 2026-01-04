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

import org.apache.commons.ognl.OgnlRuntime;
import org.apache.commons.ognl.test.objects.CorrectedObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class NullHandlerTest
    extends OgnlTestCase
{
    private static final CorrectedObject CORRECTED = new CorrectedObject();

    private static final Object[][] TESTS = {
        // NullHandler
        { CORRECTED, "stringValue", "corrected" }, { CORRECTED, "getStringValue()", "corrected" },
        { CORRECTED, "#root.stringValue", "corrected" }, { CORRECTED, "#root.getStringValue()", "corrected" }, };

    /*
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
                    fail( "don't understand TEST format with length " + element.length );
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     */
    public NullHandlerTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                            Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }

    /*
     */
    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        _compileExpressions = false;
        OgnlRuntime.setNullHandler( CorrectedObject.class, new CorrectedObjectNullHandler( "corrected" ) );
    }

    @Test

    @Override
    public void runTest()
        throws Exception
    {
        super.runTest();
    }
}
