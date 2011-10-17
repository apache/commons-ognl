package org.apache.commons.ognl.internal.entry;

import org.apache.commons.ognl.internal.CacheException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
* User: Maurizio Cucchiara
* Date: 10/17/11
* Time: 1:14 AM
*/
public class MethodAccessCacheEntryFactory
    implements CacheEntryFactory<Method, MethodAccessEntryValue>
{

    public static final MethodAccessEntryValue INACCESSIBLE_NON_PUBLIC_METHOD =
        new MethodAccessEntryValue( false, true );

    public static final MethodAccessEntryValue ACCESSIBLE_NON_PUBLIC_METHOD =
        new MethodAccessEntryValue( true, true );

    public static final MethodAccessEntryValue PUBLIC_METHOD = new MethodAccessEntryValue( true );

    public MethodAccessEntryValue create( Method method )
        throws CacheException
    {
        final boolean notPublic = !Modifier.isPublic( method.getModifiers( ) ) || !Modifier.isPublic(
            method.getDeclaringClass( ).getModifiers( ) );
        if ( notPublic )
        {
            if ( !method.isAccessible( ) )
            {
                return INACCESSIBLE_NON_PUBLIC_METHOD;
            }
            else
            {
                return ACCESSIBLE_NON_PUBLIC_METHOD;
            }
        }
        else
        {
            return PUBLIC_METHOD;
        }
    }
}
