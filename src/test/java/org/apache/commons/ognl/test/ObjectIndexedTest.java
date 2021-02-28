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
import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.OgnlException;
import org.apache.commons.ognl.OgnlRuntime;
import org.apache.commons.ognl.SimpleNode;
import org.junit.Before;

public class ObjectIndexedTest
    extends TestCase
{
    protected OgnlContext context;

    /*
     * =================================================================== Public static classes
     * ===================================================================
     */
    public interface TestInterface
    {
        String getSunk( String index );

        void setSunk( String index, String sunk );
    }

    public static class Test1
        extends Object
        implements TestInterface
    {
        public String getSunk( String index )
        {
            return "foo";
        }

        public void setSunk( String index, String sunk )
        {
            /* do nothing */
        }
    }

    public static class Test2
        extends Test1
    {
        public String getSunk( String index )
        {
            return "foo";
        }

        public void setSunk( String index, String sunk )
        {
            /* do nothing */
        }
    }

    public static class Test3
        extends Test1
    {
        public String getSunk( String index )
        {
            return "foo";
        }

        public void setSunk( String index, String sunk )
        {
            /* do nothing */
        }

        public String getSunk( Object index )
        {
            return null;
        }
    }

    public static class Test4
        extends Test1
    {
        public String getSunk( String index )
        {
            return "foo";
        }

        public void setSunk( String index, String sunk )
        {
            /* do nothing */
        }

        public void setSunk( Object index, String sunk )
        {
            /* do nothing */
        }
    }

    public static class Test5
        extends Test1
    {
        public String getSunk( String index )
        {
            return "foo";
        }

        public void setSunk( String index, String sunk )
        {
            /* do nothing */
        }

        public String getSunk( Object index )
        {
            return null;
        }

        public void setSunk( Object index, String sunk )
        {
            /* do nothing */
        }
    }

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    public static TestSuite suite()
    {
        return new TestSuite( ObjectIndexedTest.class );
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public ObjectIndexedTest()
    {
    }

    public ObjectIndexedTest( String name )
    {
        super( name );
    }

    /*
     * =================================================================== Public methods
     * ===================================================================
     */
    public void testPropertyDescriptorReflection()
        throws Exception
    {
        OgnlRuntime.getPropertyDescriptor( java.util.AbstractList.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.util.AbstractSequentialList.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.lang.reflect.Array.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.util.ArrayList.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.util.BitSet.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.util.Calendar.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.lang.reflect.Field.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.util.LinkedList.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.util.List.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.util.Iterator.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.lang.ThreadLocal.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.net.URL.class, "" );
        OgnlRuntime.getPropertyDescriptor( java.util.Vector.class, "" );
    }

    public void testObjectIndexAccess()
        throws OgnlException
    {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression( "#ka.sunk[#root]" );

        context.put( "ka", new Test1() );
        Ognl.getValue( expression, context, "aksdj" );
    }

    public void testObjectIndexInSubclass()
        throws OgnlException
    {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression( "#ka.sunk[#root]" );

        context.put( "ka", new Test2() );
        Ognl.getValue( expression, context, "aksdj" );
    }

    public void testMultipleObjectIndexGetters()
        throws OgnlException
    {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression( "#ka.sunk[#root]" );

        context.put( "ka", new Test3() );
        try
        {
            Ognl.getValue( expression, context, new Test3() );
            fail();
        }
        catch ( OgnlException ex )
        {
            /* Should throw */
        }
    }

    public void testMultipleObjectIndexSetters()
        throws OgnlException
    {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression( "#ka.sunk[#root]" );

        context.put( "ka", new Test4() );
        try
        {
            Ognl.getValue( expression, context, "aksdj" );
            fail();
        }
        catch ( OgnlException ex )
        {
            /* Should throw */
        }
    }

    public void testMultipleObjectIndexMethodPairs()
        throws OgnlException
    {
        SimpleNode expression = (SimpleNode) Ognl.parseExpression( "#ka.sunk[#root]" );

        context.put( "ka", new Test5() );
        try
        {
            Ognl.getValue( expression, context, "aksdj" );
            fail();
        }
        catch ( OgnlException ex )
        {
            /* Should throw */
        }
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    @Override
    @Before
    protected void setUp()
    {
        context = (OgnlContext) Ognl.createDefaultContext( null );
    }
}
