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
import java.util.List;

import org.apache.commons.ognl.OgnlRuntime;
import org.apache.commons.ognl.test.objects.Bean1;
import org.apache.commons.ognl.test.objects.BeanProvider;
import org.apache.commons.ognl.test.objects.BeanProviderAccessor;
import org.apache.commons.ognl.test.objects.EvenOdd;
import org.apache.commons.ognl.test.objects.ListSourceImpl;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class InterfaceInheritanceTest
    extends OgnlTestCase
{

    private static final Root ROOT = new Root();

    static
    {
        ROOT.getBeans().setBean( "testBean", new Bean1() );
        ROOT.getBeans().setBean( "evenOdd", new EvenOdd() );

        List list = new ListSourceImpl();
        list.add( "test1" );

        ROOT.getMap().put( "customList", list );
    }

    private static final Object[][] TESTS = { { ROOT, "myMap", ROOT.getMyMap() }, { ROOT, "myMap.test", ROOT },
        { ROOT.getMyMap(), "list", ROOT.getList() }, { ROOT, "myMap.array[0]", new Integer( ROOT.getArray()[0] ) },
        { ROOT, "myMap.list[1]", ROOT.getList().get( 1 ) }, { ROOT, "myMap[^]", new Integer( 99 ) },
        { ROOT, "myMap[$]", null },
        { ROOT.getMyMap(), "array[$]", new Integer( ROOT.getArray()[ROOT.getArray().length - 1] ) },
        { ROOT, "[\"myMap\"]", ROOT.getMyMap() }, { ROOT, "myMap[null]", null }, { ROOT, "myMap[#x = null]", null },
        { ROOT, "myMap.(null,test)", ROOT }, { ROOT, "myMap[null] = 25", new Integer( 25 ) },
        { ROOT, "myMap[null]", new Integer( 25 ), new Integer( 50 ), new Integer( 50 ) },
        { ROOT, "beans.testBean", ROOT.getBeans().getBean( "testBean" ) }, { ROOT, "beans.evenOdd.next", "even" },
        { ROOT, "map.comp.form.clientId", "form1" }, { ROOT, "map.comp.getCount(genericIndex)", Integer.valueOf( 0 ) },
        { ROOT, "map.customList.total", Integer.valueOf( 1 ) }, { ROOT, "myTest.theMap['key']", "value" },
        { ROOT, "contentProvider.hasChildren(property)", Boolean.TRUE },
        { ROOT, "objectIndex instanceof java.lang.Object", Boolean.TRUE } };

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
    public InterfaceInheritanceTest( String name, Object root, String expressionString, Object expectedResult,
                                     Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }

    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        OgnlRuntime.setPropertyAccessor( BeanProvider.class, new BeanProviderAccessor() );
    }

    @Override
    @Test

    public void runTest()
        throws Exception
    {
        super.runTest();
    }
}
