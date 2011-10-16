package org.apache.commons.ognl.internal.entry;

import java.lang.reflect.Method;

/**
* User: Maurizio Cucchiara
* Date: 10/16/11
* Time: 9:28 PM
*/
public class GenericMethodParameterTypeCacheEntry
    implements CacheEntry
{
    Method method;

    Class<?> type;

    public GenericMethodParameterTypeCacheEntry( Method method, Class<?> type )
    {
        this.method = method;
        this.type = type;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof GenericMethodParameterTypeCacheEntry ) )
        {
            return false;
        }

        GenericMethodParameterTypeCacheEntry that = (GenericMethodParameterTypeCacheEntry) o;

        if ( !method.equals( that.method ) )
        {
            return false;
        }
        if ( !type.equals( that.type ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode( )
    {
        int result = method.hashCode( );
        result = 31 * result + type.hashCode( );
        return result;
    }
}
