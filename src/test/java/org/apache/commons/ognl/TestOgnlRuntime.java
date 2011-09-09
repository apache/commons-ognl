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
package org.apache.commons.ognl;

import org.apache.commons.ognl.test.objects.*;
import org.junit.Test;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Tests various methods / functionality of {@link org.apache.commons.ognl.OgnlRuntime}.
 */
public class TestOgnlRuntime
{

    @Test
    public void test_Get_Super_Or_Interface_Class()
        throws Exception
    {
        ListSource list = new ListSourceImpl();

        Method m = OgnlRuntime.getReadMethod( list.getClass(), "total" );
        assertNotNull( m );

        assertEquals( ListSource.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass( m, list.getClass() ) );
    }

    @Test
    public void test_Get_Private_Class()
        throws Exception
    {
        List list = Arrays.asList( "hello", "world" );

        Method m = OgnlRuntime.getReadMethod( list.getClass(), "iterator" );
        assertNotNull( m );

        assertEquals( Iterable.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass( m, list.getClass() ) );
    }

    @Test
    public void test_Complicated_Inheritance()
        throws Exception
    {
        IForm form = new FormImpl();

        Method m = OgnlRuntime.getWriteMethod( form.getClass(), "clientId" );
        assertNotNull( m );

        assertEquals( IComponent.class, OgnlRuntime.getCompiler().getSuperOrInterfaceClass( m, form.getClass() ) );
    }

    @Test
    public void test_Get_Read_Method()
        throws Exception
    {
        Method m = OgnlRuntime.getReadMethod( Bean2.class, "pageBreakAfter" );
        assertNotNull( m );

        assertEquals( "isPageBreakAfter", m.getName() );
    }

    class TestGetters
    {
        public boolean isEditorDisabled()
        {
            return false;
        }

        public boolean isDisabled()
        {
            return true;
        }

        public boolean isNotAvailable()
        {
            return false;
        }

        public boolean isAvailable()
        {
            return true;
        }
    }

    @Test
    public void test_Get_Read_Method_Multiple()
        throws Exception
    {
        Method m = OgnlRuntime.getReadMethod( TestGetters.class, "disabled" );
        assertNotNull( m );

        assertEquals( "isDisabled", m.getName() );
    }

    @Test
    public void test_Get_Read_Method_Multiple_Boolean_Getters()
        throws Exception
    {
        Method m = OgnlRuntime.getReadMethod( TestGetters.class, "available" );
        assertNotNull( m );

        assertEquals( "isAvailable", m.getName() );

        m = OgnlRuntime.getReadMethod( TestGetters.class, "notAvailable" );
        assertNotNull( m );

        assertEquals( "isNotAvailable", m.getName() );
    }

    @Test
    public void test_Find_Method_Mixed_Boolean_Getters()
        throws Exception
    {
        Method m = OgnlRuntime.getReadMethod( GetterMethods.class, "allowDisplay" );
        assertNotNull( m );

        assertEquals( "getAllowDisplay", m.getName() );
    }

    @Test
    public void test_Get_Appropriate_Method()
        throws Exception
    {
        ListSource list = new ListSourceImpl();
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );

        Object ret = OgnlRuntime.callMethod( context, list, "addValue", new String[] { null } );

        assert ret != null;
    }

    @Test
    public void test_Call_Static_Method_Invalid_Class()
    {

        try
        {

            OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
            OgnlRuntime.callStaticMethod( context, "made.up.Name", "foo", null );

            fail( "ClassNotFoundException should have been thrown by previous reference to <made.up.Name> class." );
        }
        catch ( Exception et )
        {

            assertTrue( MethodFailedException.class.isInstance( et ) );
            assertTrue( et.getMessage().contains( "made.up.Name" ) );
        }
    }

    @Test
    public void test_Setter_Returns()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        SetterReturns root = new SetterReturns();

        Method m = OgnlRuntime.getWriteMethod( root.getClass(), "value" );
        assertTrue( m != null );

        Ognl.setValue( "value", context, root, "12__" );
        assertEquals( Ognl.getValue( "value", context, root ), "12__" );
    }

    @Test
    public void test_Call_Method_VarArgs()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        GenericService service = new GenericServiceImpl();

        GameGenericObject argument = new GameGenericObject();

        Object[] args = OgnlRuntime.getObjectArrayPool().create( 2 );
        args[0] = argument;

        assertEquals( "Halo 3", OgnlRuntime.callMethod( context, service, "getFullMessageFor", args ) );
    }

    @Test
    public void test_Class_Cache_Inspector()
        throws Exception
    {
        OgnlRuntime.clearCache();
        assertEquals( 0, OgnlRuntime._propertyDescriptorCache.getSize() );

        Root root = new Root();
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        Node expr = Ognl.compileExpression( context, root, "property.bean3.value != null" );

        assertTrue( (Boolean) expr.getAccessor().get( context, root ) );

        int size = OgnlRuntime._propertyDescriptorCache.getSize();
        assertTrue( size > 0 );

        OgnlRuntime.clearCache();
        assertEquals( 0, OgnlRuntime._propertyDescriptorCache.getSize() );

        // now register class cache prevention

        OgnlRuntime.setClassCacheInspector( new TestCacheInspector() );

        expr = Ognl.compileExpression( context, root, "property.bean3.value != null" );
        assertTrue( (Boolean) expr.getAccessor().get( context, root ) );

        assertEquals( ( size - 1 ), OgnlRuntime._propertyDescriptorCache.getSize() );
    }

    class TestCacheInspector
        implements ClassCacheInspector
    {

        public boolean shouldCache( Class<?> type )
        {
            return !( type == null || type == Root.class );
        }
    }

    @Test
    public void test_Set_Generic_Parameter_Types()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );

        Method m = OgnlRuntime.getSetMethod( context, GenericCracker.class, "param" );
        assertNotNull( m );

        Class[] types = m.getParameterTypes();
        assertEquals( 1, types.length );
        assertEquals( Integer.class, types[0] );
    }

    @Test
    public void test_Get_Generic_Parameter_Types()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );

        Method m = OgnlRuntime.getGetMethod( context, GenericCracker.class, "param" );
        assertNotNull( m );

        assertEquals( Integer.class, m.getReturnType() );
    }

    @Test
    public void test_Find_Parameter_Types()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );

        Method m = OgnlRuntime.getSetMethod( context, GameGeneric.class, "ids" );
        assertNotNull( m );

        Class[] types = OgnlRuntime.findParameterTypes( GameGeneric.class, m );
        assertEquals( 1, types.length );
        assertEquals( Long[].class, types[0] );
    }

    @Test
    public void test_Find_Parameter_Types_Superclass()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );

        Method m = OgnlRuntime.getSetMethod( context, BaseGeneric.class, "ids" );
        assertNotNull( m );

        Class[] types = OgnlRuntime.findParameterTypes( BaseGeneric.class, m );
        assertEquals( 1, types.length );
        assertEquals( Serializable[].class, types[0] );
    }

    @Test
    public void test_Get_Declared_Methods_With_Synthetic_Methods()
        throws Exception
    {
        List result = OgnlRuntime.getDeclaredMethods( SubclassSyntheticObject.class, "list", false );

        // synthetic method would be
        // "public volatile java.util.List org.ognl.test.objects.SubclassSyntheticObject.getList()",
        // causing method return size to be 3

        assertEquals( 2, result.size() );
    }

    @Test
    public void test_Get_Property_Descriptors_With_Synthetic_Methods()
        throws Exception
    {
        PropertyDescriptor pd = OgnlRuntime.getPropertyDescriptor( SubclassSyntheticObject.class, "list" );

        assert pd != null;
        assert OgnlRuntime.isMethodCallable( pd.getReadMethod() );
    }

    private static class GenericParent<T>
    {
        public void save( T entity )
        {

        }
    }

    private static class StringChild
        extends GenericParent<String>
    {

    }

    private static class LongChild
        extends GenericParent<Long>
    {

    }

    /**
     * Tests OGNL parameter discovery.
     */
    @Test
    public void testOGNLParameterDiscovery()
        throws NoSuchMethodException
    {
        Method saveMethod = GenericParent.class.getMethod( "save", Object.class );
        System.out.println( saveMethod );

        Class[] longClass = OgnlRuntime.findParameterTypes( LongChild.class, saveMethod );
        assertNotSame( longClass[0], String.class );
        assertSame( longClass[0], Long.class );

        Class[] stringClass = OgnlRuntime.findParameterTypes( StringChild.class, saveMethod );
        assertNotSame( "The cached parameter types from previous calls are used", stringClass[0], Long.class );
        assertSame( stringClass[0], String.class );
    }

}
