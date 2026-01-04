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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.ognl.InappropriateExpressionException;
import org.apache.commons.ognl.NoSuchPropertyException;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class SetterTest
    extends OgnlTestCase
{
    private static final Root ROOT = new Root();

    static Set<String> _list = new HashSet<String>();
    static
    {
        _list.add( "Test1" );
    }

    private static final Object[][] TESTS = {
        // Setting values
        { ROOT.getMap(), "newValue", null, new Integer( 101 ) },
        { ROOT, "settableList[0]", "foo", "quux" }, // absolute indexes
        { ROOT, "settableList[0]", "quux" },
        { ROOT, "settableList[2]", "baz", "quux" },
        { ROOT, "settableList[2]", "quux" },
        { ROOT, "settableList[$]", "quux", "oompa" }, // special indexes
        { ROOT, "settableList[$]", "oompa" },
        { ROOT, "settableList[^]", "quux", "oompa" },
        { ROOT, "settableList[^]", "oompa" },
        { ROOT, "settableList[|]", "bar", "oompa" },
        { ROOT, "settableList[|]", "oompa" },
        { ROOT, "map.newValue", new Integer( 101 ), new Integer( 555 ) },
        { ROOT, "map", ROOT.getMap(), new HashMap<String, String>(), NoSuchPropertyException.class },
        { ROOT.getMap(), "newValue2 || put(\"newValue2\",987), newValue2", new Integer( 987 ), new Integer( 1002 ) },
        { ROOT, "map.(someMissingKey || newValue)", new Integer( 555 ), new Integer( 666 ) },
        { ROOT.getMap(), "newValue || someMissingKey", new Integer( 666 ), new Integer( 666 ) }, // no setting happens!
        { ROOT, "map.(newValue && aKey)", null, new Integer( 54321 ) },
        { ROOT, "map.(someMissingKey && newValue)", null, null }, // again, no setting
        { null, "0", new Integer( 0 ), null, InappropriateExpressionException.class }, // illegal for setting, no
                                                                                       // property
        { ROOT, "map[0]=\"map.newValue\", map[0](#this)", new Integer( 666 ), new Integer( 888 ) },
        { ROOT, "selectedList", null, _list, IllegalArgumentException.class },
        { ROOT, "openTransitionWin", Boolean.FALSE, Boolean.TRUE } };

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
    public SetterTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                       Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
