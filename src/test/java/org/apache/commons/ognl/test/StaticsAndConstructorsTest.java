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

import org.apache.commons.ognl.test.objects.Root;
import org.apache.commons.ognl.test.objects.Simple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class StaticsAndConstructorsTest
    extends OgnlTestCase
{
    private static Root ROOT = new Root();

    private static Object[][] TESTS =
        {
            { "@java.lang.Class@forName(\"java.lang.Object\")", Object.class },
            { "@java.lang.Integer@MAX_VALUE", new Integer( Integer.MAX_VALUE ) },
            { "@@max(3,4)", new Integer( 4 ) },
            { "new java.lang.StringBuffer().append(55).toString()", "55" },
            { "class", ROOT.getClass() },
            { "@org.apache.commons.ognl.test.objects.Root@class", ROOT.getClass() },
            { "class.getName()", ROOT.getClass().getName() },
            { "@org.apache.commons.ognl.test.objects.Root@class.getName()", ROOT.getClass().getName() },
            { "@org.apache.commons.ognl.test.objects.Root@class.name", ROOT.getClass().getName() },
            { "class.getSuperclass()", ROOT.getClass().getSuperclass() },
            { "class.superclass", ROOT.getClass().getSuperclass() },
            { "class.name", ROOT.getClass().getName() },
            { "getStaticInt()", new Integer( Root.getStaticInt() ) },
            { "@org.apache.commons.ognl.test.objects.Root@getStaticInt()", new Integer( Root.getStaticInt() ) },
            { "new org.apache.commons.ognl.test.objects.Simple(property).getStringValue()",
                new Simple().getStringValue() },
            { "new org.apache.commons.ognl.test.objects.Simple(map['test'].property).getStringValue()",
                new Simple().getStringValue() },
            { "map.test.getCurrentClass(@org.apache.commons.ognl.test.StaticsAndConstructorsTest@KEY.toString())",
                "size stop" },
            { "new org.apache.commons.ognl.test.StaticsAndConstructorsTest$IntWrapper(index)",
                new IntWrapper( ROOT.getIndex() ) },
            { "new org.apache.commons.ognl.test.StaticsAndConstructorsTest$IntObjectWrapper(index)",
                new IntObjectWrapper( ROOT.getIndex() ) },
            { "new org.apache.commons.ognl.test.StaticsAndConstructorsTest$A(#root)", new A( ROOT ) },
            { "@org.apache.commons.ognl.test.StaticsAndConstructorsTest$Animals@values().length != 2", Boolean.TRUE },
            { "isOk(@org.apache.commons.ognl.test.objects.SimpleEnum@ONE, null)", Boolean.TRUE }, };

    public static final String KEY = "size";

    public static class IntWrapper
    {
        public IntWrapper( int value )
        {
            this.value = value;
        }

        private final int value;

        public String toString()
        {
            return Integer.toString( value );
        }

        public boolean equals( Object o )
        {
            if ( this == o )
                return true;
            if ( o == null || getClass() != o.getClass() )
                return false;

            IntWrapper that = (IntWrapper) o;

            return value == that.value;
        }
    }

    public static class IntObjectWrapper
    {

        public IntObjectWrapper( Integer value )
        {
            this.value = value;
        }

        private final Integer value;

        public String toString()
        {
            return value.toString();
        }

        public boolean equals( Object o )
        {
            if ( this == o )
                return true;
            if ( o == null || getClass() != o.getClass() )
                return false;

            IntObjectWrapper that = (IntObjectWrapper) o;

            return value.equals( that.value );
        }
    }

    public static class A
    {
        String key = "A";

        public A( Root root )
        {

        }

        public boolean equals( Object o )
        {
            if ( this == o )
                return true;
            if ( o == null || getClass() != o.getClass() )
                return false;

            A a = (A) o;

            if ( key != null ? !key.equals( a.key ) : a.key != null )
                return false;

            return true;
        }
    }

    public enum Animals
    {

        Dog, Cat, Wallabee, Bear
    }

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
            tmp[0] = TESTS[i][0] + " (" + TESTS[i][1] + ")";
            tmp[1] = ROOT;
            tmp[2] = TESTS[i][0];
            tmp[3] = TESTS[i][1];

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public StaticsAndConstructorsTest( String name, Object root, String expressionString, Object expectedResult,
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
