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

import org.apache.commons.ognl.OgnlException;
import org.apache.commons.ognl.test.objects.Bean1;
import org.apache.commons.ognl.test.objects.ObjectIndexed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class ObjectIndexedPropertyTest
    extends OgnlTestCase
{

    private static ObjectIndexed OBJECT_INDEXED = new ObjectIndexed();

    private static Bean1 root = new Bean1();

    private static Object[][] TESTS = {
        // Arbitrary indexed properties
        { OBJECT_INDEXED, "attributes[\"bar\"]", "baz" }, // get non-indexed property through
        // attributes Map
        { OBJECT_INDEXED, "attribute[\"foo\"]", "bar" }, // get indexed property
        { OBJECT_INDEXED, "attribute[\"bar\"]", "baz", "newValue", "newValue" }, // set
        // indexed
        // property
        { OBJECT_INDEXED, "attribute[\"bar\"]", "newValue" },// get indexed property back to
        // confirm
        { OBJECT_INDEXED, "attributes[\"bar\"]", "newValue" }, // get property back through Map
        // to confirm
        { OBJECT_INDEXED, "attribute[\"other\"].attribute[\"bar\"]", "baz" }, // get indexed
        // property from
        // indexed, then
        // through other
        { OBJECT_INDEXED, "attribute[\"other\"].attributes[\"bar\"]", "baz" }, // get property
        // back through
        // Map to
        // confirm
        { OBJECT_INDEXED, "attribute[$]", OgnlException.class }, // illegal DynamicSubscript
        // access to object indexed
        // property
        { root, "bean2.bean3.indexedValue[25]", null } };

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
    public ObjectIndexedPropertyTest( String name, Object root, String expressionString, Object expectedResult,
                                      Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
