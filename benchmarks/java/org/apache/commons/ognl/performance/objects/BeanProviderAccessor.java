/*
 * $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * 
 */
package org.apache.commons.ognl.performance.objects;

import org.apache.commons.ognl.ObjectPropertyAccessor;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.OgnlException;
import org.apache.commons.ognl.OgnlRuntime;
import org.apache.commons.ognl.PropertyAccessor;
import org.apache.commons.ognl.enhance.ExpressionCompiler;
import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

import java.util.Map;

/**
 * Implementation of provider that works with {@link BeanProvider} instances.
 */
public class BeanProviderAccessor
    extends ObjectPropertyAccessor
    implements PropertyAccessor
{
    @Override
    public Object getProperty( Map<String, Object> context, Object target, Object name )
        throws OgnlException
    {
        BeanProvider provider = (BeanProvider) target;
        String beanName = (String) name;

        return provider.getBean( beanName );
    }

    /**
     * Returns true if the name matches a bean provided by the provider. Otherwise invokes the super implementation.
     **/

    @Override
    public boolean hasGetProperty( Map<String, Object> context, Object target, Object oname )
        throws OgnlException
    {
        BeanProvider provider = (BeanProvider) target;
        String beanName = ( (String) oname ).replaceAll( "\"", "" );

        return provider.getBean( beanName ) != null;
    }

    @Override
    public String getSourceAccessor( OgnlContext context, Object target, Object name )
    {
        BeanProvider provider = (BeanProvider) target;
        String beanName = ( (String) name ).replaceAll( "\"", "" );

        if ( provider.getBean( beanName ) != null )
        {
            context.setCurrentAccessor( BeanProvider.class );
            context.setCurrentType( provider.getBean( beanName ).getClass() );

            ExpressionCompiler.addCastString( context,
                                              "(("
                                                  + OgnlRuntime.getCompiler().getInterfaceClass( provider.getBean( beanName ).getClass() ).getName()
                                                  + ")" );

            return ".getBean(\"" + beanName + "\"))";
        }

        return super.getSourceAccessor( context, target, name );
    }

    @Override
    public String getSourceSetter( OgnlContext context, Object target, Object name )
    {
        throw new UnsupportedCompilationException( "Can't set beans on BeanProvider." );
    }
}
