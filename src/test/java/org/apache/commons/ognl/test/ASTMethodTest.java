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
import org.apache.commons.ognl.*;
import org.apache.commons.ognl.enhance.ExpressionCompiler;
import org.apache.commons.ognl.test.objects.Bean2;
import org.apache.commons.ognl.test.objects.Bean3;
import org.apache.commons.ognl.test.objects.Root;

import java.util.Map;

/**
 * Tests {@link org.apache.commons.ognl.ASTMethod}.
 */
public class ASTMethodTest
    extends TestCase
{

    public void test_Context_Types()
        throws Throwable
    {
        ASTMethod p = new ASTMethod( 0 );
        p.setMethodName( "get" );

        ASTConst pRef = new ASTConst( 0 );
        pRef.setValue( "value" );
        p.jjtAddChild( pRef, 0 );

        Root root = new Root();

        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        context.setRoot( root.getMap() );
        context.setCurrentObject( root.getMap() );
        context.setCurrentType( root.getMap().getClass() );

        assertEquals( p.toGetSourceString( context, root.getMap() ), ".get(\"value\")" );
        assertEquals( context.getCurrentType(), Object.class );
        assertEquals( root.getMap().get( "value" ), context.getCurrentObject() );
        assert Map.class.isAssignableFrom( context.getCurrentAccessor() );
        assert Map.class.isAssignableFrom( context.getPreviousType() );
        assert context.getPreviousAccessor() == null;

        assertEquals( OgnlRuntime.getCompiler( context ).castExpression( context, p, ".get(\"value\")" ), ".get(\"value\")" );
        assert context.get( ExpressionCompiler.PRE_CAST ) == null;

        // now test one context level further to see casting work properly on base object types

        ASTProperty prop = new ASTProperty( 0 );
        ASTConst propRef = new ASTConst( 0 );
        propRef.setValue( "bean3" );
        prop.jjtAddChild( propRef, 0 );

        Bean2 val = (Bean2) root.getMap().get( "value" );

        assertEquals( prop.toGetSourceString( context, root.getMap().get( "value" ) ), ".getBean3()" );

        assertEquals( context.getCurrentObject(), val.getBean3() );
        assertEquals( context.getCurrentType(), Bean3.class );
        assertEquals( context.getCurrentAccessor(), Bean2.class );
        assertEquals( Object.class, context.getPreviousType() );
        assert Map.class.isAssignableFrom( context.getPreviousAccessor() );

        assertEquals( OgnlRuntime.getCompiler( context ).castExpression( context, prop, ".getBean3()" ), ").getBean3()" );

    }
}
