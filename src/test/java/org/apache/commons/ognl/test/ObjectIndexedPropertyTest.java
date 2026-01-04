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

import org.apache.commons.ognl.OgnlException;
import org.apache.commons.ognl.test.objects.Bean1;
import org.apache.commons.ognl.test.objects.ObjectIndexed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ObjectIndexedPropertyTest
    extends OgnlTestCase
{

    private static final ObjectIndexed OBJECT_INDEXED = new ObjectIndexed();

    private static final Bean1 ROOT = new Bean1();

    private static final Object[][] TESTS = {
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
        { ROOT, "bean2.bean3.indexedValue[25]", null } };

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
    public ObjectIndexedPropertyTest( String name, Object root, String expressionString, Object expectedResult,
                                      Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
