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

import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;
import org.apache.commons.ognl.Ognl;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class SimplePropertyTreeTest
    extends OgnlTestCase
{
    private static final Object[][] TESTS = { { "name", Boolean.TRUE }, { "foo", Boolean.TRUE },
        { "name[i]", Boolean.FALSE }, { "name + foo", Boolean.FALSE }, { "name.foo", Boolean.FALSE },
        { "name.foo.bar", Boolean.FALSE }, { "name.{? foo }", Boolean.FALSE }, { "name.( foo )", Boolean.FALSE } };

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
            tmp[0] = element[0] + " (" + element[1] + ")";
            tmp[1] = null;
            tmp[2] = element[0];
            tmp[3] = element[1];

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public SimplePropertyTreeTest( String name, Object root, String expressionString, Object expectedResult,
                                   Object setValue, Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    @Before
    @Override
    public void runTest()
        throws Exception
    {
        Assert.assertEquals(Ognl.isSimpleProperty(getExpression(), _context), ((Boolean) getExpectedResult()).booleanValue());
    }
}
