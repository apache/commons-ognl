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

import org.apache.commons.ognl.ExpressionSyntaxException;
import org.apache.commons.ognl.test.objects.Entry;
import org.apache.commons.ognl.test.objects.Root;
import org.apache.commons.ognl.test.objects.Simple;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class ArrayCreationTest
    extends OgnlTestCase
{

    private static Root ROOT = new Root();

    private static Object[][] TESTS =
        {
            // Array creation
            { ROOT, "new String[] { \"one\", \"two\" }", new String[] { "one", "two" } },
            { ROOT, "new String[] { 1, 2 }", new String[] { "1", "2" } },
            { ROOT, "new Integer[] { \"1\", 2, \"3\" }",
                new Integer[] { 1, 2, 3 } },
            { ROOT, "new String[10]", new String[10] },
            { ROOT, "new Object[4] { #root, #this }", ExpressionSyntaxException.class },
            { ROOT, "new Object[4]", new Object[4] },
            { ROOT, "new Object[] { #root, #this }", new Object[] { ROOT, ROOT } },
            {
                ROOT,
                "new org.apache.commons.ognl.test.objects.Simple[] { new org.apache.commons.ognl.test.objects.Simple(), new org.apache.commons.ognl.test.objects.Simple(\"foo\", 1.0f, 2) }",
                new Simple[] { new Simple(), new Simple( "foo", 1.0f, 2 ) } },
            { ROOT, "new org.apache.commons.ognl.test.objects.Simple[5]", new Simple[5] },
            { ROOT, "new org.apache.commons.ognl.test.objects.Simple(new Object[5])", new Simple( new Object[5] ) },
            { ROOT, "new org.apache.commons.ognl.test.objects.Simple(new String[5])", new Simple( new String[5] ) },
            {
                ROOT,
                "objectIndex ? new org.apache.commons.ognl.test.objects.Entry[] { new org.apache.commons.ognl.test.objects.Entry(), new org.apache.commons.ognl.test.objects.Entry()} "
                    + ": new org.apache.commons.ognl.test.objects.Entry[] { new org.apache.commons.ognl.test.objects.Entry(), new org.apache.commons.ognl.test.objects.Entry()} ",
                new Entry[] { new Entry(), new Entry() } } };

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        Collection<Object[]> data = new ArrayList<Object[]>(TESTS.length);
        for ( Object[] TEST : TESTS )
        {
            Object[] tmp = new Object[6];
            tmp[0] = TEST[1];
            tmp[1] = TEST[0];
            tmp[2] = TEST[1];

            switch ( TEST.length )
            {
                case 3:
                    tmp[3] = TEST[2];
                    break;

                case 4:
                    tmp[3] = TEST[2];
                    tmp[4] = TEST[3];
                    break;

                case 5:
                    tmp[3] = TEST[2];
                    tmp[4] = TEST[3];
                    tmp[5] = TEST[4];
                    break;

                default:
                    throw new RuntimeException( "don't understand TEST format with length" );
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ArrayCreationTest( String name, Object root, String expressionString, Object expectedResult,
                              Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
