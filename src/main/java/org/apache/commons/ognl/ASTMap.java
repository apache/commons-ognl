package org.apache.commons.ognl;

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

import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * $Id$
 */
class ASTMap
    extends SimpleNode
{
    private String className;

    private Map<OgnlContext, Class> defaultMapClassMap = new HashMap<OgnlContext, Class>();

    public ASTMap( int id )
    {
        super( id );
    }

    public ASTMap( OgnlParser p, int id )
    {
        super( p, id );
    }

    protected void setClassName( String value )
    {
        className = value;
    }

    /**
     * Get the class name for this map.
     *
     * @return the class name.
     * @since 4.0
     */
    String getClassName()
    {
        return className;
    }

    protected Object getValueBody( OgnlContext context, Object source )
        throws OgnlException
    {
        Map answer;

        if ( className == null )
        {
            Class defaultMapClass = getDefaultMapClass( context );
            try
            {
                answer = (Map) defaultMapClass.newInstance();
            }
            catch ( Exception ex )
            {
                /* This should never happen */
                throw new OgnlException( "Default Map class '" + defaultMapClass.getName() + "' instantiation error",
                                         ex );
            }
        }
        else
        {
            try
            {
                answer = (Map) OgnlRuntime.classForName( context, className ).newInstance();
            }
            catch ( Exception ex )
            {
                throw new OgnlException( "Map implementor '" + className + "' not found", ex );
            }
        }

        for ( int i = 0; i < jjtGetNumChildren(); ++i )
        {
            ASTKeyValue kv = (ASTKeyValue) children[i];
            Node k = kv.getKey(), v = kv.getValue();

            answer.put( k.getValue( context, source ), ( v == null ) ? null : v.getValue( context, source ) );
        }

        return answer;
    }

    public String toGetSourceString( OgnlContext context, Object target )
    {
        throw new UnsupportedCompilationException( "Map expressions not supported as native java yet." );
    }

    public String toSetSourceString( OgnlContext context, Object target )
    {
        throw new UnsupportedCompilationException( "Map expressions not supported as native java yet." );
    }

    public <R, P> R accept( NodeVisitor<? extends R, ? super P> visitor, P data )
        throws OgnlException
    {
        return visitor.visit( this, data );
    }

    private Class getDefaultMapClass( OgnlContext context )
    {
        Class defaultMapClass = defaultMapClassMap.get( context );
        if ( defaultMapClass != null )
        {
            return defaultMapClass;
        }

        defaultMapClass = LinkedHashMap.class;

        defaultMapClassMap.put( context, defaultMapClass );
        return defaultMapClass;
    }
}
