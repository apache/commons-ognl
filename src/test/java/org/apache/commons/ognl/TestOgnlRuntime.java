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

import org.apache.commons.ognl.enhance.OgnlExpressionCompiler;
import org.apache.commons.ognl.internal.CacheException;
import org.apache.commons.ognl.test.objects.BaseGeneric;
import org.apache.commons.ognl.test.objects.Bean1;
import org.apache.commons.ognl.test.objects.Bean2;
import org.apache.commons.ognl.test.objects.FormImpl;
import org.apache.commons.ognl.test.objects.GameGeneric;
import org.apache.commons.ognl.test.objects.GameGenericObject;
import org.apache.commons.ognl.test.objects.GenericCracker;
import org.apache.commons.ognl.test.objects.GenericService;
import org.apache.commons.ognl.test.objects.GenericServiceImpl;
import org.apache.commons.ognl.test.objects.GetterMethods;
import org.apache.commons.ognl.test.objects.IComponent;
import org.apache.commons.ognl.test.objects.IForm;
import org.apache.commons.ognl.test.objects.ListSource;
import org.apache.commons.ognl.test.objects.ListSourceImpl;
import org.apache.commons.ognl.test.objects.OtherObjectIndexed;
import org.apache.commons.ognl.test.objects.Root;
import org.apache.commons.ognl.test.objects.SetterReturns;
import org.apache.commons.ognl.test.objects.SubclassSyntheticObject;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
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

        Method method = OgnlRuntime.getReadMethod( list.getClass(), "total" );
        assertNotNull( method );

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        assertEquals( ListSource.class,
                      OgnlRuntime.getCompiler( context ).getSuperOrInterfaceClass( method, list.getClass() ) );
    }

    @Test
    public void test_Get_Private_Class()
        throws Exception
    {
        List<String> list = Arrays.asList( "hello", "world" );

        Method m = OgnlRuntime.getReadMethod( list.getClass(), "iterator" );
        assertNotNull( m );

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        assertEquals( Iterable.class,
                      OgnlRuntime.getCompiler( context ).getSuperOrInterfaceClass( m, list.getClass() ) );
    }

    @Test
    public void test_Complicated_Inheritance()
        throws Exception
    {
        IForm form = new FormImpl();

        Method method = OgnlRuntime.getWriteMethod( form.getClass(), "clientId" );
        assertNotNull( method );

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        assertEquals( IComponent.class,
                      OgnlRuntime.getCompiler( context ).getSuperOrInterfaceClass( method, form.getClass() ) );
    }

    @Test
    public void test_Get_Read_Method()
        throws Exception
    {
        Method method = OgnlRuntime.getReadMethod( Bean2.class, "pageBreakAfter" );
        assertNotNull( method );

        assertEquals( "isPageBreakAfter", method.getName() );
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
        Method method = OgnlRuntime.getReadMethod( TestGetters.class, "disabled" );
        assertNotNull( method );

        assertEquals( "isDisabled", method.getName() );
    }

    @Test
    public void test_Get_Read_Method_Multiple_Boolean_Getters()
        throws Exception
    {
        Method method = OgnlRuntime.getReadMethod( TestGetters.class, "available" );
        assertNotNull( method );

        assertEquals( "isAvailable", method.getName() );

        method = OgnlRuntime.getReadMethod( TestGetters.class, "notAvailable" );
        assertNotNull( method );

        assertEquals( "isNotAvailable", method.getName() );
    }

    @Test
    public void test_Find_Method_Mixed_Boolean_Getters()
        throws Exception
    {
        Method method = OgnlRuntime.getReadMethod( GetterMethods.class, "allowDisplay" );
        assertNotNull( method );

        assertEquals( "getAllowDisplay", method.getName() );
    }

    @Test
    public void test_Get_Appropriate_Method()
        throws Exception
    {
        ListSource list = new ListSourceImpl();
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );

        Object ret = OgnlRuntime.callMethod( context, list, "addValue", new String[]{ null } );

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
        assertNotNull(m);

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

        Object[] args = new Object[2];
        args[0] = argument;

        assertEquals( "Halo 3", OgnlRuntime.callMethod( context, service, "getFullMessageFor", args ) );
    }

    @Test
    public void test_Class_Cache_Inspector()
        throws Exception
    {
        OgnlRuntime.cache.clear();

        assertEquals( 0, OgnlRuntime.cache.propertyDescriptorCache.getSize() );

        Root root = new Root();
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        Node expr = Ognl.compileExpression( context, root, "property.bean3.value != null" );

        assertTrue( (Boolean) expr.getAccessor().get( context, root ) );

        int size = OgnlRuntime.cache.propertyDescriptorCache.getSize();
        assertTrue( size > 0 );

        OgnlRuntime.clearCache();
        assertEquals( 0, OgnlRuntime.cache.propertyDescriptorCache.getSize() );

        // now register class cache prevention

        OgnlRuntime.setClassCacheInspector( new TestCacheInspector() );

        expr = Ognl.compileExpression( context, root, "property.bean3.value != null" );
        assertTrue( (Boolean) expr.getAccessor().get( context, root ) );

        assertEquals( ( size - 1 ), OgnlRuntime.cache.propertyDescriptorCache.getSize() );
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

        Method method = OgnlRuntime.getSetMethod( context, GenericCracker.class, "param" );
        assertNotNull( method );

        Class<?>[] types = method.getParameterTypes();
        assertEquals( 1, types.length );
        assertEquals( Integer.class, types[0] );
    }

    @Test
    public void test_Get_Generic_Parameter_Types()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );

        Method method = OgnlRuntime.getGetMethod( context, GenericCracker.class, "param" );
        assertNotNull( method );

        assertEquals( Integer.class, method.getReturnType() );
    }

    @Test
    public void test_Find_Parameter_Types()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );

        Method method = OgnlRuntime.getSetMethod( context, GameGeneric.class, "ids" );
        assertNotNull( method );

        Class<?>[] types = OgnlRuntime.findParameterTypes( GameGeneric.class, method );
        assertEquals( 1, types.length );
        assertEquals( Long[].class, types[0] );
    }

    @Test
    public void test_Find_Parameter_Types_Superclass()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );

        Method method = OgnlRuntime.getSetMethod( context, BaseGeneric.class, "ids" );
        assertNotNull( method );

        Class<?>[] types = OgnlRuntime.findParameterTypes( BaseGeneric.class, method );
        assertEquals( 1, types.length );
        assertEquals( Serializable[].class, types[0] );
    }

    @Test
    public void test_Get_Declared_Methods_With_Synthetic_Methods()
        throws Exception
    {
        List<Method> result = OgnlRuntime.getDeclaredMethods( SubclassSyntheticObject.class, "list", false );

        // synthetic method would be
        // "public volatile java.util.List org.ognl.test.objects.SubclassSyntheticObject.getList()",
        // causing method return size to be 3

        assertEquals( 2, result.size() );
    }

    @Test
    public void test_Get_Property_Descriptors_With_Synthetic_Methods()
        throws Exception
    {
        PropertyDescriptor propertyDescriptor = OgnlRuntime.getPropertyDescriptor( SubclassSyntheticObject.class, "list" );

        assert propertyDescriptor != null;
        assert OgnlRuntime.isMethodCallable( propertyDescriptor.getReadMethod() );
    }

    private static class GenericParent<T>
    {
        @SuppressWarnings( "unused" )
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
        throws NoSuchMethodException, CacheException
    {
        Method saveMethod = GenericParent.class.getMethod( "save", Object.class );
        System.out.println( saveMethod );

        Class<?>[] longClass = OgnlRuntime.findParameterTypes( LongChild.class, saveMethod );
        assertNotSame( longClass[0], String.class );
        assertSame( longClass[0], Long.class );

        Class<?>[] stringClass = OgnlRuntime.findParameterTypes( StringChild.class, saveMethod );
        assertNotSame( "The cached parameter types from previous calls are used", stringClass[0], Long.class );
        assertSame( stringClass[0], String.class );
    }

    @Test
    public void testGetField()
        throws OgnlException
    {
        Field field = OgnlRuntime.getField( OtherObjectIndexed.class, "attributes" );
        assertNotNull( "Field is null", field );
    }

    @Test
    public void testGetSetMethod()
        throws IntrospectionException, OgnlException
    {
        Method setter = OgnlRuntime.getSetMethod( null, Bean1.class, "bean2" );
        Method getter = OgnlRuntime.getGetMethod( null, Bean1.class, "bean2" );
        assertNotNull( getter );
        assertNull( setter );
    }

    @Test
    public void testGetCompiler()
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        OgnlExpressionCompiler compiler1 = OgnlRuntime.getCompiler( context );
        context.put( "root2", new Root() );
        OgnlExpressionCompiler compiler2 = OgnlRuntime.getCompiler( context );
        assertSame( "compilers are not the same", compiler1, compiler2 );
    }

    @Test
    public void testGetPropertyDescriptorFromArray()
        throws Exception
    {
        PropertyDescriptor propertyDescriptor =
            OgnlRuntime.getPropertyDescriptorFromArray( Root.class, "disabled" );
        assertEquals( "disabled", propertyDescriptor.getName() );
    }
}
