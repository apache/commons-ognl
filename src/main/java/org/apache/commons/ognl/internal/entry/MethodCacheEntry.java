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
}
