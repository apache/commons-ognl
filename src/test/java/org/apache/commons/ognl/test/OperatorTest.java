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

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class OperatorTest
    extends OgnlTestCase
{
    private static final Object[][] TESTS = { { null, "\"one\" > \"two\"", Boolean.FALSE },
        { null, "\"one\" >= \"two\"", Boolean.FALSE }, { null, "\"one\" < \"two\"", Boolean.TRUE },
        { null, "\"one\" <= \"two\"", Boolean.TRUE }, { null, "\"one\" == \"two\"", Boolean.FALSE },
        { null, "\"o\" > \"o\"", Boolean.FALSE }, { null, "\"o\" gt \"o\"", Boolean.FALSE },
        { null, "\"o\" >= \"o\"", Boolean.TRUE }, { null, "\"o\" gte \"o\"", Boolean.TRUE },
        { null, "\"o\" < \"o\"", Boolean.FALSE }, { null, "\"o\" lt \"o\"", Boolean.FALSE },
        { null, "\"o\" <= \"o\"", Boolean.TRUE }, { null, "\"o\" lte \"o\"", Boolean.TRUE },
        { null, "\"o\" == \"o\"", Boolean.TRUE }, { null, "\"o\" eq \"o\"", Boolean.TRUE }, };

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
                    fail( "don't understand TEST format with length " + element.length );
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public OperatorTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                         Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
