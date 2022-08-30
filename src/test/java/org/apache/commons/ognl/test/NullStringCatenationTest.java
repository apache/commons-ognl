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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.ognl.test.objects.Root;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class NullStringCatenationTest
    extends OgnlTestCase
{

    public static final String MESSAGE = "blarney";

    private static final Root ROOT = new Root();

    private static final Object[][] TESTS =
    {
        // Null string catenation
        { ROOT, "\"bar\" + null", "barnull" }, // Catenate null to a string
        { ROOT, "\"bar\" + nullObject", "barnull" }, // Catenate null to a string
        { ROOT, "20.56 + nullObject", NullPointerException.class }, // Catenate null to a number
        { ROOT, "(true ? 'tabHeader' : '') + (false ? 'tabHeader' : '')", "tabHeader" },
        { ROOT, "theInt == 0 ? '5%' : theInt + '%'", "6%" },
        { ROOT, "'width:' + width + ';'", "width:238px;" },
        { ROOT, "theLong + '_' + index", "4_1" },
        { ROOT, "'javascript:' + @org.apache.commons.ognl.test.NullStringCatenationTest@MESSAGE", "javascript:blarney" },
        { ROOT,  "printDelivery ? '' : 'javascript:deliverySelected(' + property.carrier + ',' + currentDeliveryId + ')'", "" },
        { ROOT, "bean2.id + '_' + theInt", "1_6" }
    };


    /**
     * Setup parameters for this test which are used to call this class constructor
     * @return the collection of paramaters
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        Collection<Object[]> data = new ArrayList<Object[]>(TESTS.length);
        for ( int i = 0; i < TESTS.length; i++ )
        {
            Object[] tmp = new Object[4];
            tmp[0] = TESTS[i][1];
            tmp[1] = TESTS[i][0];
            tmp[2] = TESTS[i][1];
            tmp[3] = TESTS[i][2];

            data.add( tmp );
        }
        return data;
    }

    /**
     * Constructor: size of the Object[] returned by the @Parameter annotated method must match
     * the number of arguments in this constructor
     */
    public NullStringCatenationTest( String name, Object root, String expressionString, Object expectedResult)
    {
        super( name, root, expressionString, expectedResult );
    }

    @Test
    @Override
    public void runTest() throws Exception
    {
        super.runTest();
    }
}
