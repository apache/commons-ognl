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

import org.apache.commons.ognl.test.objects.CorrectedObject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class ClassMethodTest
    extends OgnlTestCase
{

    private static CorrectedObject CORRECTED = new CorrectedObject();

    private static Object[][] TESTS = {
        // Methods on Class
        { CORRECTED, "getClass().getName()", CORRECTED.getClass().getName() },
        { CORRECTED, "getClass().getInterfaces()", CORRECTED.getClass().getInterfaces() },
        { CORRECTED, "getClass().getInterfaces().length", new Integer( CORRECTED.getClass().getInterfaces().length ) },
        { null, "@System@class.getInterfaces()", System.class.getInterfaces() },
        { null, "@Class@class.getName()", Class.class.getName() },
        { null, "@java.awt.image.ImageObserver@class.getName()", java.awt.image.ImageObserver.class.getName() }, };

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
            tmp[4] = null;
            tmp[5] = null;

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ClassMethodTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                            Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }

    @Before
    @Override
    public void setUp()
    {
        super.setUp();
        _context.put( "x", "1" );
        _context.put( "y", new BigDecimal( 1 ) );
    }
}
