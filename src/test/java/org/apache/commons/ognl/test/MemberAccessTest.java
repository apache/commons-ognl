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

import org.apache.commons.ognl.DefaultMemberAccess;
import org.apache.commons.ognl.OgnlException;
import org.apache.commons.ognl.test.objects.Simple;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RunWith(value = Parameterized.class)
public class MemberAccessTest
    extends OgnlTestCase
{

    private static Simple ROOT = new Simple();

    private static Object[][] TESTS = { { "@Runtime@getRuntime()", OgnlException.class },
        { "@System@getProperty('java.specification.version')", System.getProperty( "java.specification.version" ) },
        { "bigIntValue", OgnlException.class },
        { "bigIntValue", OgnlException.class, 25, OgnlException.class },
        { "getBigIntValue()", OgnlException.class }, { "stringValue", ROOT.getStringValue() }, };

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
            tmp[0] = TEST[0] + " (" + TEST[1] + ")";
            tmp[1] = ROOT;
            tmp[2] = TEST[0];
            tmp[3] = TEST[1];
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
    public MemberAccessTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                             Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    @Override
    @Before
    public void setUp()
    {
        super.setUp();

        /* Should allow access at all to the Simple class except for the bigIntValue property */
        _context.setMemberAccess( new DefaultMemberAccess( false )
        {

            @Override
            public boolean isAccessible( Map context, Object target, Member member, String propertyName )
            {
                if ( target == Runtime.class )
                {
                    return false;
                }
                if ( target instanceof Simple )
                {
                    if ( propertyName != null )
                    {
                        return !propertyName.equals( "bigIntValue" )
                            && super.isAccessible( context, target, member, propertyName );
                    }
                    if ( member instanceof Method )
                    {
                        return !member.getName().equals( "getBigIntValue" )
                            && !member.getName().equals( "setBigIntValue" )
                            && super.isAccessible( context, target, member, propertyName );
                    }
                }
                return super.isAccessible( context, target, member, propertyName );
            }
        } );
    }
}
