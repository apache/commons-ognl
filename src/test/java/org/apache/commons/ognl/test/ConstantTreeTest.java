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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class ConstantTreeTest
    extends OgnlTestCase
{

    public static int nonFinalStaticVariable = 15;

    private static final Object[][] TESTS = { { "true", Boolean.TRUE }, { "55", Boolean.TRUE },
        { "@java.awt.Color@black", Boolean.TRUE },
        { "@org.apache.commons.ognl.test.ConstantTreeTest@nonFinalStaticVariable", Boolean.FALSE },
        { "@org.apache.commons.ognl.test.ConstantTreeTest@nonFinalStaticVariable + 10", Boolean.FALSE },
        { "55 + 24 + @java.awt.Event@ALT_MASK", Boolean.TRUE }, { "name", Boolean.FALSE },
        { "name[i]", Boolean.FALSE }, { "name[i].property", Boolean.FALSE }, { "name.{? foo }", Boolean.FALSE },
        { "name.{ foo }", Boolean.FALSE }, { "name.{ 25 }", Boolean.FALSE }

    };

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
            tmp[4] = null;
            tmp[5] = null;

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    @Override
    public void runTest()
        throws Exception
    {
        Assert.assertEquals(Ognl.isConstant(getExpression(), _context), ((Boolean) getExpectedResult()).booleanValue());
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ConstantTreeTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                             Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
