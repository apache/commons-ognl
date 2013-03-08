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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.*;

/**
 * Implementation of PropertyAccessor that uses numbers and dynamic subscripts as properties to index into Lists.
 */
public class ListPropertyAccessor
    extends ObjectPropertyAccessor
    implements PropertyAccessor
{

    @Override
    public Object getProperty( Map<String, Object> context, Object target, Object name )
        throws OgnlException
    {
        List<?> list = (List<?>) target;

        if ( name instanceof String )
        {
            Object result;

            if ( "size".equals( name ) )
            {
                result = list.size();
            }
            else
            {
                if ( "iterator".equals( name ) )
                {
                    result = list.iterator();
                }
                else
                {
                    if ( "isEmpty".equals( name ) || "empty".equals( name ) )
                    {
                        result = list.isEmpty() ? Boolean.TRUE : Boolean.FALSE;
                    }
                    else
                    {
                        result = super.getProperty( context, target, name );
                    }
                }
            }

            return result;
        }

        if ( name instanceof Number )
        {
            return list.get( ( (Number) name ).intValue() );
        }

        if ( name instanceof DynamicSubscript )
        {
            int len = list.size();
            switch ( ( (DynamicSubscript) name ).getFlag() )
            {
                case DynamicSubscript.FIRST:
                    return len > 0 ? list.get( 0 ) : null;
                case DynamicSubscript.MID:
                    return len > 0 ? list.get( len / 2 ) : null;
                case DynamicSubscript.LAST:
                    return len > 0 ? list.get( len - 1 ) : null;
                case DynamicSubscript.ALL:
                    return new ArrayList<Object>( list );
                default:
                    break;
            }
        }

        throw new NoSuchPropertyException( target, name );
    }

    @Override
    public void setProperty( Map<String, Object> context, Object target, Object name, Object value )
        throws OgnlException
    {
        if ( name instanceof String && !( (String) name ).contains( "$" ) )
        {
            super.setProperty( context, target, name, value );
            return;
        }

        @SuppressWarnings( "unchecked" ) // check performed by the invoker
            List<Object> list = (List<Object>) target;

        if ( name instanceof Number )
        {
            list.set( ( (Number) name ).intValue(), value );
            return;
        }

        if ( name instanceof DynamicSubscript )
        {
            int len = list.size();
            switch ( ( (DynamicSubscript) name ).getFlag() )
            {
                case DynamicSubscript.FIRST:
                    if ( len > 0 )
                    {
                        list.set( 0, value );
                    }
                    return;
                case DynamicSubscript.MID:
                    if ( len > 0 )
                    {
                        list.set( len / 2, value );
                    }
                    return;
                case DynamicSubscript.LAST:
                    if ( len > 0 )
                    {
                        list.set( len - 1, value );
                    }
                    return;
                case DynamicSubscript.ALL:
                    if ( !( value instanceof Collection ) )
                    {
                        throw new OgnlException( "Value must be a collection" );
                    }
                    list.clear();
                    list.addAll( (Collection<?>) value );
                    return;
                default:
                    return;
            }
        }

        throw new NoSuchPropertyException( target, name );
    }

    @Override
    public Class<?> getPropertyClass( OgnlContext context, Object target, Object index )
    {
        if ( index instanceof String )
        {
            String key = ( (String) index ).replaceAll( "\"", "" );
            if ( "size".equals( key ) )
            {
                return int.class;
            }
            if ( "iterator".equals( key ) )
            {
                return Iterator.class;
            }
            if ( "isEmpty".equals( key ) || "empty".equals( key ) )
            {
                return boolean.class;
            }
            return super.getPropertyClass( context, target, index );
        }

        if ( index instanceof Number )
        {
            return Object.class;
        }

        return null;
    }

    @Override
    public String getSourceAccessor( OgnlContext context, Object target, Object index )
    {
        String indexStr = index.toString().replaceAll( "\"", "" );

        if ( String.class.isInstance( index ) )
        {
            if ( "size".equals( indexStr ) )
            {
                context.setCurrentAccessor( List.class );
                context.setCurrentType( int.class );
                return ".size()";
            }
            if ( "iterator".equals( indexStr ) )
            {
                context.setCurrentAccessor( List.class );
                context.setCurrentType( Iterator.class );
                return ".iterator()";
            }
            if ( "isEmpty".equals( indexStr ) || "empty".equals( indexStr ) )
            {
                context.setCurrentAccessor( List.class );
                context.setCurrentType( boolean.class );
                return ".isEmpty()";
            }
        }

        // TODO: This feels really inefficient, must be some better way
        // check if the index string represents a method on a custom class implementing java.util.List instead..
        return getSourceBeanMethod( context, target, index, indexStr, false );
    }

    @Override
    public String getSourceSetter( OgnlContext context, Object target, Object index )
    {
        String indexStr = index.toString().replaceAll( "\"", "" );

        // TODO: This feels really inefficient, must be some better way
        // check if the index string represents a method on a custom class implementing java.util.List instead..
        /*
         * System.out.println("Listpropertyaccessor setter using index: " + index + " and current object: " +
         * context.getCurrentObject() + " number is current object? " +
         * Number.class.isInstance(context.getCurrentObject()));
         */

        return getSourceBeanMethod( context, target, index, indexStr, true );
    }

    private String getSourceBeanMethod( OgnlContext context, Object target, Object index, String indexStr,
                                        boolean isSetter )
    {
        Object currentObject = context.getCurrentObject();
        Class<?> currentType = context.getCurrentType();
        if ( currentObject != null && !Number.class.isInstance( currentObject ) )
        {
            try
            {
                if ( isSetter )
                {
                    if ( OgnlRuntime.getWriteMethod( target.getClass(), indexStr ) != null
                        || !currentType.isPrimitive() )
                    {
                        return super.getSourceSetter( context, target, index );
                    }
                }
                else
                {
                    if ( OgnlRuntime.getReadMethod( target.getClass(), indexStr ) != null )
                    {
                        return super.getSourceAccessor( context, target, index );
                    }
                }
            }
            catch ( Throwable t )
            {
                throw OgnlOps.castToRuntime( t );
            }
        }

        /*
         * if (String.class.isInstance(index)) { context.setCurrentAccessor(List.class); return ""; }
         */

        context.setCurrentAccessor( List.class );

        // need to convert to primitive for list index access

        if ( !currentType.isPrimitive() && Number.class.isAssignableFrom( currentType ) )
        {
            indexStr += "." + OgnlRuntime.getNumericValueGetter( currentType );
        }
        else if ( currentObject != null && Number.class.isAssignableFrom( currentObject.getClass() )
            && !currentType.isPrimitive() )
        {
            // means it needs to be cast first as well

            String toString = String.class.isInstance( index ) && currentType != Object.class ? "" : ".toString()";

            indexStr = "org.apache.commons.ognl.OgnlOps#getIntValue(" + indexStr + toString + ")";
        }

        context.setCurrentType( Object.class );

        return isSetter ? ".set(" + indexStr + ", $3)" : ".get(" + indexStr + ")";
    }

}
