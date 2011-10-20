package org.apache.commons.ognl.internal.entry;

import org.apache.commons.ognl.OgnlException;
import org.apache.commons.ognl.OgnlRuntime;
import org.apache.commons.ognl.internal.CacheException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Maurizio Cucchiara
 * Date: 10/15/11
 * Time: 8:08 AM
 */
public class PropertyDescriptorCacheEntryFactory
    implements ClassCacheEntryFactory<Map<String,PropertyDescriptor>>
{
    public Map<String,PropertyDescriptor> create( Class<?> targetClass)
        throws CacheException
    {
        Map<String, PropertyDescriptor> result = new HashMap<String, PropertyDescriptor>( 101 );
        PropertyDescriptor[] pda;
        try
        {
            pda = Introspector.getBeanInfo( targetClass ).getPropertyDescriptors( );

            for ( int i = 0, icount = pda.length; i < icount; i++ )
            {
                // workaround for Introspector bug 6528714 (bugs.sun.com)
                if ( pda[i].getReadMethod( ) != null && !OgnlRuntime.isMethodCallable( pda[i].getReadMethod( ) ) )
                {
                    pda[i].setReadMethod(
                        findClosestMatchingMethod( targetClass, pda[i].getReadMethod( ), pda[i].getName( ),
                                                   pda[i].getPropertyType( ), true ) );
                }
                if ( pda[i].getWriteMethod( ) != null && !OgnlRuntime.isMethodCallable( pda[i].getWriteMethod( ) ) )
                {
                    pda[i].setWriteMethod(
                        findClosestMatchingMethod( targetClass, pda[i].getWriteMethod( ), pda[i].getName( ),
                                                   pda[i].getPropertyType( ), false ) );
                }

                result.put( pda[i].getName( ), pda[i] );
            }

            OgnlRuntime.findObjectIndexedPropertyDescriptors( targetClass, result );
        }
        catch ( IntrospectionException e )
        {
            throw new CacheException( e );
        }
        catch ( OgnlException e )
        {
            throw new CacheException( e );
        }
        return result;
    }

    static Method findClosestMatchingMethod( Class<?> targetClass, Method m, String propertyName, Class<?> propertyType,
                                             boolean isReadMethod )
        throws OgnlException
    {
        List<Method> methods = OgnlRuntime.getDeclaredMethods( targetClass, propertyName, !isReadMethod );

        for ( Method method : methods )
        {
            if ( method.getName( ).equals( m.getName( ) ) && m.getReturnType( ).isAssignableFrom( m.getReturnType( ) )
                && method.getReturnType( ) == propertyType
                && method.getParameterTypes( ).length == m.getParameterTypes( ).length )
            {
                return method;
            }
        }

        return m;
    }


}
