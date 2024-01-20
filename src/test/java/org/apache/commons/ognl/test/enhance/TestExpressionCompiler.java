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
/**
 */
package org.apache.commons.ognl.test.enhance;

import org.apache.commons.ognl.Node;
import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.enhance.ExpressionCompiler;
import org.apache.commons.ognl.enhance.OgnlExpressionCompiler;
import org.apache.commons.ognl.test.objects.Bean1;
import org.apache.commons.ognl.test.objects.GenericRoot;
import org.apache.commons.ognl.test.objects.IndexedMapObject;
import org.apache.commons.ognl.test.objects.Inherited;
import org.apache.commons.ognl.test.objects.Root;
import org.apache.commons.ognl.test.objects.TestInherited1;
import org.apache.commons.ognl.test.objects.TestInherited2;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;

/**
 * Tests functionality of {@link ExpressionCompiler}.
 */
public class TestExpressionCompiler
{
    OgnlExpressionCompiler _compiler;

    OgnlContext _context = (OgnlContext) Ognl.createDefaultContext( null );

    @Before
    public void setUp()
    {
        _compiler = new ExpressionCompiler();
    }

    @Test
    public void test_Get_Property_Access()
        throws Throwable
    {
        Node expr = (Node) Ognl.parseExpression( "bean2" );
        Bean1 root = new Bean1();

        _compiler.compileExpression( _context, expr, root );

        assertNotNull( expr.getAccessor().get( _context, root ) );
    }

    @Test
    public void test_Get_Indexed_Property()
        throws Throwable
    {
        Node expr = (Node) Ognl.parseExpression( "bean2.bean3.indexedValue[25]" );
        Bean1 root = new Bean1();

        assertNull( Ognl.getValue( expr, _context, root ) );

        _compiler.compileExpression( _context, expr, root );

        assertNull( expr.getAccessor().get( _context, root ) );
    }

    @Test
    public void test_Set_Indexed_Property()
        throws Throwable
    {
        Node expr = (Node) Ognl.parseExpression( "bean2.bean3.indexedValue[25]" );
        Bean1 root = new Bean1();

        assertNull( Ognl.getValue( expr, _context, root ) );

        _compiler.compileExpression( _context, expr, root );

        expr.getAccessor().set( _context, root, "test string" );

        assertEquals( "test string", expr.getAccessor().get( _context, root ) );
    }

    @Test
    public void test_Expression()
        throws Throwable
    {
        Node expr = (Node) Ognl.parseExpression( "bean2.bean3.value <= 24" );
        Bean1 root = new Bean1();

        assertEquals( Boolean.FALSE, Ognl.getValue( expr, _context, root ) );

        _compiler.compileExpression( _context, expr, root );

        assertEquals( Boolean.FALSE, expr.getAccessor().get( _context, root ) );
    }

    @Test
    public void test_Get_Context_Property()
        throws Throwable
    {
        _context.put( "key", "foo" );
        Node expr = (Node) Ognl.parseExpression( "bean2.bean3.map[#key]" );
        Bean1 root = new Bean1();

        assertEquals( "bar", Ognl.getValue( expr, _context, root ) );

        _compiler.compileExpression( _context, expr, root );

        assertEquals( "bar", expr.getAccessor().get( _context, root ) );

        _context.put( "key", "bar" );

        assertEquals( "baz", Ognl.getValue( expr, _context, root ) );
        assertEquals( "baz", expr.getAccessor().get( _context, root ) );
    }

    @Test
    public void test_Set_Context_Property()
        throws Throwable
    {
        _context.put( "key", "foo" );
        Node expr = (Node) Ognl.parseExpression( "bean2.bean3.map[#key]" );
        Bean1 root = new Bean1();

        _compiler.compileExpression( _context, expr, root );

        assertEquals( "bar", expr.getAccessor().get( _context, root ) );

        _context.put( "key", "bar" );
        assertEquals( "baz", expr.getAccessor().get( _context, root ) );

        expr.getAccessor().set( _context, root, "bam" );
        assertEquals( "bam", expr.getAccessor().get( _context, root ) );
    }

    @Test
    public void test_Property_Index()
        throws Throwable
    {
        Root root = new Root();
        Node expr = Ognl.compileExpression( _context, root, "{index + 1}" );

        Object ret = expr.getAccessor().get( _context, root );

        assertTrue( Collection.class.isInstance( ret ) );
    }

    @Test
    public void test_Root_Expression_Inheritance()
        throws Throwable
    {
        Inherited obj1 = new TestInherited1();
        Inherited obj2 = new TestInherited2();

        Node expr = Ognl.compileExpression( _context, obj1, "myString" );

        assertEquals( expr.getAccessor().get( _context, obj1 ), "inherited1" );
        assertEquals( expr.getAccessor().get( _context, obj2 ), "inherited2" );
    }

    @Test
    public void test_Create_Empty_Collection()
        throws Throwable
    {
        Node expr = Ognl.compileExpression( _context, null, "{}" );

        Object ret = expr.getAccessor().get( _context, null );

        assertNotNull( ret );
        assertTrue( Collection.class.isAssignableFrom( ret.getClass() ) );
    }

    public String getKey()
    {
        return "key";
    }

    @Test
    public void test_Indexed_Property()
        throws Throwable
    {
        Map<String,String> map = new HashMap<String,String>();
        map.put( "key", "value" );

        Node expression = Ognl.compileExpression( _context, this, "key" );
        assertEquals( "key", expression.getAccessor().get( _context, this ) );
    }

    IndexedMapObject mapObject = new IndexedMapObject( "propertyValue" );

    public IndexedMapObject getObject()
    {
        return mapObject;
    }

    public String getPropertyKey()
    {
        return "property";
    }

    @Test
    public void test_Indexed_Map_Property()
        throws Throwable
    {
        assertEquals( "propertyValue", Ognl.getValue( "object[propertyKey]", this ) );

        _context.clear();
        Node expression = Ognl.compileExpression( _context, this, "object[#this.propertyKey]" );
        assertEquals( "propertyValue", expression.getAccessor().get( _context, this ) );

        _context.clear();
        expression = Ognl.compileExpression( _context, this, "object[propertyKey]" );
        assertEquals( "propertyValue", expression.getAccessor().get( _context, this ) );
    }

    @Test
    public void test_Set_Generic_Property()
        throws Exception
    {
        _context.clear();

        GenericRoot root = new GenericRoot();

        Node node = Ognl.compileExpression( _context, root, "cracker.param" );
        assertNull(node.getAccessor().get(_context, root));

        node.getAccessor().set( _context, root, 0 );
        assertEquals( 0, node.getAccessor().get( _context, root ) );

        node.getAccessor().set( _context, root, 12 );
        assertEquals( 12, node.getAccessor().get( _context, root ) );
    }
}
