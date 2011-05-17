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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.ognl.test.objects.BaseBean;
import org.apache.commons.ognl.test.objects.Bean2;
import org.apache.commons.ognl.test.objects.FirstBean;
import org.apache.commons.ognl.test.objects.PropertyHolder;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class PropertyTest
    extends OgnlTestCase
{

    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat( "MM/dd/yyyy hh:mm a 'CST'" );

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "MM/dd/yyyy" );

    public static final String VALUE = "foo";

    private static Root ROOT = new Root();

    private static BaseBean BEAN = new FirstBean();

    private static PropertyHolder PROPERTY = new PropertyHolder();

    private static Object[][] TESTS =
        {
            { ROOT, "testString != null && !false", Boolean.TRUE },
            { ROOT, "!getRenderNavigation() and !getReadonly()", Boolean.TRUE },
            { ROOT, "!bean2.pageBreakAfter", Boolean.TRUE },
            { ROOT, "map", ROOT.getMap() },
            { ROOT, "map.test", ROOT },
            { ROOT, "map[\"test\"]", ROOT },
            { ROOT, "map[\"te\" + \"st\"]", ROOT },
            { ROOT, "map[(\"s\" + \"i\") + \"ze\"]", ROOT.getMap().get( Root.SIZE_STRING ) },
            { ROOT, "map[\"size\"]", ROOT.getMap().get( Root.SIZE_STRING ) },
            { ROOT, "map[@org.apache.commons.ognl.test.objects.Root@SIZE_STRING]", ROOT.getMap().get( Root.SIZE_STRING ) },
            { ROOT, "stringValue != null && stringValue.length() > 0", Boolean.FALSE },
            { ROOT, "indexedStringValue != null && indexedStringValue.length() > 0", Boolean.TRUE },
            { ROOT.getMap(), "list", ROOT.getList() },
            { ROOT, "map.array[0]", new Integer( ROOT.getArray()[0] ) },
            { ROOT, "map.list[1]", ROOT.getList().get( 1 ) },
            { ROOT, "map[^]", new Integer( 99 ) },
            { ROOT, "map[$]", null },
            { ROOT.getMap(), "array[$]", new Integer( ROOT.getArray()[ROOT.getArray().length - 1] ) },
            { ROOT, "[\"map\"]", ROOT.getMap() },
            { ROOT.getArray(), "length", new Integer( ROOT.getArray().length ) },
            { ROOT, "getMap().list[|]", ROOT.getList().get( ROOT.getList().size() / 2 ) },
            { ROOT, "map.(array[2] + size())", new Integer( ROOT.getArray()[2] + ROOT.getMap().size() ) },
            { ROOT, "map.(#this)", ROOT.getMap() },
            { ROOT, "map.(#this != null ? #this['size'] : null)", ROOT.getMap().get( Root.SIZE_STRING ) },
            { ROOT, "map[^].(#this == null ? 'empty' : #this)", new Integer( 99 ) },
            { ROOT, "map[$].(#this == null ? 'empty' : #this)", "empty" },
            { ROOT, "map[$].(#root == null ? 'empty' : #root)", ROOT },
            { ROOT, "((selected != null) && (currLocale.toString() == selected.toString())) ? 'first' : 'second'",
                "first" },
            { ROOT, "{stringValue, getMap()}", Arrays.asList( new Object[] { ROOT.getStringValue(), ROOT.getMap() } ) },
            { ROOT, "{'stringValue', map[\"test\"].map[\"size\"]}",
                Arrays.asList( new Object[] { "stringValue", ROOT.getMap().get( "size" ) } ) },
            { ROOT, "property.bean3.value + '(this.checked)'", "100(this.checked)" },
            { ROOT, "getIndexedProperty(property.bean3.map[\"bar\"])", ROOT.getArray() },
            { ROOT, "getProperty().getBean3()", ( (Bean2) ROOT.getProperty() ).getBean3() },
            { ROOT, "intValue", new Integer( 0 ), new Integer( 2 ), new Integer( 2 ) },
            { ROOT, "! booleanValue", Boolean.TRUE },
            { ROOT, "booleanValue", Boolean.FALSE, Boolean.TRUE, Boolean.TRUE },
            { ROOT, "! disabled", new Boolean( false ) },
            { ROOT, "disabled || readonly", Boolean.TRUE },
            { ROOT, "property.bean3.value != null", Boolean.TRUE },
            { ROOT, "\"background-color:blue; width:\" + (currentLocaleVerbosity / 2) + \"px\"",
                "background-color:blue; width:43px" },
            { ROOT, "renderNavigation ? '' : 'noborder'", "noborder" },
            { ROOT, "format('key', array)", "formatted" },
            { ROOT, "format('key', intValue)", "formatted" },
            { ROOT, "format('key', map.size)", "formatted" },
            { ROOT,
                "'disableButton(this,\"' + map.get('button-testing') + '\");clearElement(&quot;testFtpMessage&quot;)'",
                "disableButton(this,'null');clearElement('testFtpMessage')" },
            { ROOT.getMap(), "!disableWarning", Boolean.TRUE },
            { ROOT.getMap(), "get('value').bean3.value",
                new Integer( ( (Bean2) ROOT.getMap().get( "value" ) ).getBean3().getValue() ) },
            { ROOT.getMap(), "\"Tapestry\".toCharArray()[2]", new Character( 'p' ) },
            { ROOT.getMap(), "nested.deep.last", Boolean.TRUE },
            { ROOT, "'last ' + getCurrentClass(@org.apache.commons.ognl.test.PropertyTest@VALUE)", "last foo stop" },
            { ROOT, "@org.apache.commons.ognl.test.PropertyTest@formatValue(property.millis, true, true)",
                formatValue( (int) ( (Bean2) ROOT.getProperty() ).getMillis(), true, true ) },
            { ROOT, "nullObject || !readonly", Boolean.TRUE },
            { ROOT,
                "testDate == null ? '-' : @org.apache.commons.ognl.test.PropertyTest@DATETIME_FORMAT.format(testDate)",
                DATETIME_FORMAT.format( ROOT.getTestDate() ) },
            { ROOT, "disabled ? 'disabled' : 'othernot'", "disabled" },
            { BEAN, "two.getMessage(active ? 'ACT' : 'INA')", "[ACT]" },
            { BEAN, "hasChildren('aaa')", Boolean.TRUE },
            { BEAN, "two.hasChildren('aa')", Boolean.FALSE },
            { BEAN, "two.hasChildren('a')", Boolean.FALSE },
            { ROOT, "sorted ? (readonly ? 'currentSortDesc' : 'currentSortAsc') : 'currentSortNone'", "currentSortAsc" },
            { ROOT, "getAsset( (width?'Yes':'No')+'Icon' )", "YesIcon" },
            { ROOT, "flyingMonkey", Boolean.TRUE },
            { ROOT,
                "expiration == null ? '' : @org.apache.commons.ognl.test.PropertyTest@DATE_FORMAT.format(expiration)",
                "" }, { ROOT, "printDelivery ? 'javascript:toggle(' + bean2.id + ');' : ''", "javascript:toggle(1);" },
            { ROOT, "openTransitionWin", Boolean.FALSE }, { ROOT, "b.methodOfB(a.methodOfA(b)-1)", new Integer( 0 ) },
            { ROOT, "disabled", Boolean.TRUE }, { PROPERTY, "value", "" }, { PROPERTY, "search", "foo" } };

    public static String formatValue( int millis, boolean b1, boolean b2 )
    {
        return millis + "-formatted";
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
            tmp[0] = TESTS[i][1];
            tmp[1] = TESTS[i][0];
            tmp[2] = TESTS[i][1];
            tmp[3] = TESTS[i][2];

            if ( TESTS[i].length == 5 )
            {
                tmp[4] = TESTS[i][3];
                tmp[5] = TESTS[i][4];
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public PropertyTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                         Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
