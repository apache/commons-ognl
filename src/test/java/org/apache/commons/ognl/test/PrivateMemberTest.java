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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.ognl.DefaultMemberAccess;
import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.OgnlException;
import org.junit.Before;

/**
 * This is a test program for private access in OGNL. shows the failures and a summary.
 */
public class PrivateMemberTest
    extends TestCase
{
    private final String _privateProperty = "private value";

    protected OgnlContext context;

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite()
    {
        return new TestSuite( PrivateMemberTest.class );
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public PrivateMemberTest( String name )
    {
        super( name );
    }

    /*
     * =================================================================== Public methods
     * ===================================================================
     */
    private String getPrivateProperty()
    {
        return _privateProperty;
    }

    public void testPrivateAccessor()
        throws OgnlException
    {
        assertEquals( Ognl.getValue( "privateProperty", context, this ), getPrivateProperty() );
    }

    public void testPrivateField()
        throws OgnlException
    {
        assertEquals( Ognl.getValue( "_privateProperty", context, this ), _privateProperty );
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    @Before
    @Override
    public void setUp()
    {
        context = (OgnlContext) Ognl.createDefaultContext( null );
        context.setMemberAccess( new DefaultMemberAccess( true ) );
    }
}
