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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;
import org.apache.commons.ognl.Ognl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class SimpleNavigationChainTreeTest
    extends OgnlTestCase
{

    private static final Object[][] TESTS = { { "name", Boolean.TRUE }, { "name[i]", Boolean.FALSE },
        { "name + foo", Boolean.FALSE }, { "name.foo", Boolean.TRUE } };

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
            tmp[1] = null;
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
    public SimpleNavigationChainTreeTest( String name, Object root, String expressionString, Object expectedResult,
                                          Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    @Test
    @Override
    public void runTest()
        throws Exception
    {
        Assert.assertEquals(Ognl.isSimpleNavigationChain(getExpression(), _context), ((Boolean) getExpectedResult()).booleanValue());
    }
}
