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

import org.apache.commons.ognl.test.objects.Root;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class PrimitiveArrayTest
    extends OgnlTestCase
{
    private static final Root ROOT = new Root();

    private static final Object[][] TESTS = {
        // Primitive array creation
        { ROOT, "new boolean[5]", new boolean[5] },
        { ROOT, "new boolean[] { true, false }", new boolean[] { true, false } },
        { ROOT, "new boolean[] { 0, 1, 5.5 }", new boolean[] { false, true, true } },
        { ROOT, "new char[] { 'a', 'b' }", new char[] { 'a', 'b' } },
        { ROOT, "new char[] { 10, 11 }", new char[] { (char) 10, (char) 11 } },
        { ROOT, "new byte[] { 1, 2 }", new byte[] { 1, 2 } }, { ROOT, "new short[] { 1, 2 }", new short[] { 1, 2 } },
        { ROOT, "new int[six]", new int[ROOT.six] }, { ROOT, "new int[#root.six]", new int[ROOT.six] },
        { ROOT, "new int[6]", new int[6] }, { ROOT, "new int[] { 1, 2 }", new int[] { 1, 2 } },
        { ROOT, "new long[] { 1, 2 }", new long[] { 1, 2 } }, { ROOT, "new float[] { 1, 2 }", new float[] { 1, 2 } },
        { ROOT, "new double[] { 1, 2 }", new double[] { 1, 2 } },

    };

    /*
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
                    fail( "don't understand TEST format with length " + TEST.length );
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     */
    public PrimitiveArrayTest( String name, Object root, String expressionString, Object expectedResult,
                               Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
