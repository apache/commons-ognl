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
import org.apache.commons.ognl.Node;
import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;

import java.util.Map;

/**
 * Tests being able to set property on object with interface that doesn't define setter. See OGNL-115.
 */
public class PropertySetterTest
    extends TestCase
{

    private Map<String, String> map;

    private final TestObject testObject = new TestObject( "propertyValue" );

    private final String propertyKey = "property";

    public interface TestInterface
    {
        String getProperty();
    }

    public class TestObject
        implements TestInterface
    {

        private String property;

        private final Integer integerProperty = 1;

        public TestObject( String property )
        {
            this.property = property;
        }

        public String getProperty()
        {
            return property;
        }

        public void setProperty( String property )
        {
            this.property = property;
        }

        public Integer getIntegerProperty()
        {
            return integerProperty;
        }
    }

    public Map<String, String> getMap()
    {
        return map;
    }

    public String getKey()
    {
        return "key";
    }

    public TestObject getObject()
    {
        return testObject;
    }

    public TestInterface getInterfaceObject()
    {
        return testObject;
    }

    public String getPropertyKey()
    {
        return propertyKey;
    }

    public void testEnhancedOgnl()
        throws Exception
    {
        OgnlContext context = (OgnlContext) Ognl.createDefaultContext( null );
        Node expression = Ognl.compileExpression( context, this, "interfaceObject.property" );
        Ognl.setValue( expression, context, this, "hello" );
        assertEquals( "hello", getObject().getProperty() );

        // Fails if an interface is defined, but succeeds if not
        context.clear();

        expression = Ognl.compileExpression( context, this.getObject(), "property" );
        Ognl.setValue( expression, context, this.getObject(), "hello" );
        assertEquals( "hello", getObject().getProperty() );
    }
}
