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

import junit.framework.TestSuite;
import org.apache.commons.ognl.test.objects.BaseGeneric;
import org.apache.commons.ognl.test.objects.GameGeneric;
import org.apache.commons.ognl.test.objects.GameGenericObject;
import org.apache.commons.ognl.test.objects.ListSource;
import org.apache.commons.ognl.test.objects.ListSourceImpl;
import org.apache.commons.ognl.test.objects.Simple;

public class MethodTest extends OgnlTestCase
{

    private static Simple ROOT = new Simple();
    private static ListSource LIST = new ListSourceImpl();
    private static BaseGeneric<GameGenericObject, Long> GENERIC = new GameGeneric();

    private static Object[][] TESTS = {
            { "hashCode()", new Integer(ROOT.hashCode()) } ,
            { "getBooleanValue() ? \"here\" : \"\"", ""},
            { "getValueIsTrue(!false) ? \"\" : \"here\" ", ""},
            { "messages.format('ShowAllCount', one)", "foo"},
            { "getTestValue(@org.apache.commons.ognl.test.objects.SimpleEnum@ONE.value)", new Integer(2)},
            { "@org.apache.commons.ognl.test.MethodTest@getA().isProperty()", Boolean.FALSE},
            { "isDisabled()", Boolean.TRUE},
            { "isEditorDisabled()", Boolean.FALSE},
            { LIST, "addValue(name)", Boolean.TRUE},
            { "getDisplayValue(methodsTest.allowDisplay)", "test"},
            { "isThisVarArgsWorking(three, rootValue)", Boolean.TRUE},
            { GENERIC, "service.getFullMessageFor(value, null)", "Halo 3"}
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
    * =================================================================== Public static methods
    * ===================================================================
    */
    public static TestSuite suite()
    {
        TestSuite result = new TestSuite();

        for(int i = 0; i < TESTS.length; i++) {
            if (TESTS[i].length == 3)
            {
                result.addTest(new MethodTest((String) TESTS[i][1] + " (" + TESTS[i][2] + ")", TESTS[i][0], (String) TESTS[i][1], TESTS[i][2]));
            } else
            {
                result.addTest(new MethodTest((String) TESTS[i][0] + " (" + TESTS[i][1] + ")", ROOT, (String) TESTS[i][0], TESTS[i][1]));
            }
        }
        return result;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public MethodTest()
    {
        super();
    }

    public MethodTest(String name)
    {
        super(name);
    }

    public MethodTest(String name, Object root, String expressionString, Object expectedResult, Object setValue,
                      Object expectedAfterSetResult)
    {
        super(name, root, expressionString, expectedResult, setValue, expectedAfterSetResult);
    }

    public MethodTest(String name, Object root, String expressionString, Object expectedResult, Object setValue)
    {
        super(name, root, expressionString, expectedResult, setValue);
    }

    public MethodTest(String name, Object root, String expressionString, Object expectedResult)
    {
        super(name, root, expressionString, expectedResult);
    }
}
