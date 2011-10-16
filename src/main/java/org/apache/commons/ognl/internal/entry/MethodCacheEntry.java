package org.apache.commons.ognl.internal.entry;

/**
 * User: Maurizio Cucchiara
 * Date: 10/15/11
 * Time: 1:54 PM
 */
public class MethodCacheEntry implements CacheEntry
{
    public Class<?> targetClass;

    public MethodCacheEntry( Class<?> targetClass )
    {
        this.targetClass = targetClass;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof MethodCacheEntry ) )
        {
            return false;
        }

        MethodCacheEntry that = (MethodCacheEntry) o;

        if ( !targetClass.equals( that.targetClass ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode( )
    {
        return targetClass.hashCode( );
    }
}
