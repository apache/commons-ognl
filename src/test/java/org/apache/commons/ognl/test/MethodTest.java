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

import org.apache.commons.ognl.test.objects.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class MethodTest
    extends OgnlTestCase
{

    private static final Simple ROOT = new Simple();

    private static final ListSource LIST = new ListSourceImpl();

    private static final BaseGeneric<GameGenericObject, Long> GENERIC = new GameGeneric();

    private static final Object[][] TESTS = {
        { "hashCode()", new Integer( ROOT.hashCode() ) },
        { "getBooleanValue() ? \"here\" : \"\"", "" }, { "getValueIsTrue(!false) ? \"\" : \"here\" ", "" },
        { "messages.format('ShowAllCount', new Object[]{one,two})", "foo" },
        { "messages.format('ShowAllCount', one)", "first" },
        { "getTestValue(@org.apache.commons.ognl.test.objects.SimpleEnum@ONE.value)", new Integer( 2 ) },
        { "@org.apache.commons.ognl.test.MethodTest@getA().isProperty()", Boolean.FALSE },
        { "isDisabled()", Boolean.TRUE }, { "isEditorDisabled()", Boolean.FALSE },
        { LIST, "addValue(name)", Boolean.TRUE }, { "getDisplayValue(methodsTest.allowDisplay)", "test" },
        { "isThisVarArgsWorking(three, rootValue)", Boolean.TRUE },
        { GENERIC, "service.getFullMessageFor(value, null)", "Halo 3" }
    };

    public static class A
    {
        public boolean isProperty()
        {
            return false;
        }
    }

    public static A getA()
    {
        return new A();
    }

    /*
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        Collection<Object[]> data = new ArrayList<Object[]>(TESTS.length);
        for (Object[] element : TESTS) {
            Object[] tmp = new Object[6];

            if ( element.length == 3 )
            {
                tmp[0] = element[1] + " (" + element[2] + ")";
                tmp[1] = element[0];
                tmp[2] = element[1];
                tmp[3] = element[2];
            }
            else
            {
                tmp[0] = element[0] + " (" + element[1] + ")";
                tmp[1] = ROOT;
                tmp[2] = element[0];
                tmp[3] = element[1];
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     */
    public MethodTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                       Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
