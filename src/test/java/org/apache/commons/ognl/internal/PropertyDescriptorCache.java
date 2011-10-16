package org.apache.commons.ognl.internal;

import org.apache.commons.ognl.internal.entry.PropertyDescriptorCacheEntryFactory;
import org.apache.commons.ognl.test.objects.Bean2;
import org.junit.Test;

import java.beans.PropertyDescriptor;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * User: Maurizio Cucchiara
 * Date: 10/15/11
 * Time: 8:00 AM
 */
public class PropertyDescriptorCache
{
    ConcurrentHashMapCache<Class<?>, Map<String,PropertyDescriptor>> cache =
        new ConcurrentHashMapCache<Class<?>, Map<String,PropertyDescriptor>>( new PropertyDescriptorCacheEntryFactory());

    @Test
    public void testGet( )
        throws Exception
    {
        Map<String, PropertyDescriptor> d = getPropertyDescriptor( Bean2.class  );
        assertTrue( d.containsKey( "id" ) );
        assertTrue( d.containsKey( "bean3" ) );
        assertTrue( d.containsKey( "carrier" ) );
    }

    private Map<String, PropertyDescriptor> getPropertyDescriptor( Class<?> entry )
        throws CacheException
    {
        Map<String, PropertyDescriptor> descriptor = cache.get( entry );
        assertNotNull( descriptor );
        return descriptor;
    }
}
