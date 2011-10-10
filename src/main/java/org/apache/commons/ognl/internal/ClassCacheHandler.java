package org.apache.commons.ognl.internal;

/**
 * User: Maurizio Cucchiara
 * Date: 10/9/11
 * Time: 1:02 PM
 */
public class ClassCacheHandler
{

    private ClassCacheHandler( )
    {
    }

    public static <T> T getHandler( Class<?> forClass, ClassCache<T> handlers )
        throws CacheException
    {
        T answer;

        synchronized ( handlers )
        {
            if ( ( answer = handlers.get( forClass ) ) == null )
            {
                Class<?> keyFound;

                if ( forClass.isArray( ) )
                {
                    answer = handlers.get( Object[].class );
                    keyFound = null;
                }
                else
                {
                    keyFound = forClass;
                    outer:
                    for ( Class<?> c = forClass; c != null; c = c.getSuperclass( ) )
                    {
                        answer = handlers.get( c );
                        if ( answer == null )
                        {
                            Class<?>[] interfaces = c.getInterfaces( );
                            for ( Class<?> iface : interfaces )
                            {
                                answer = handlers.get( iface );
                                if ( answer == null )
                                {
                                    /* Try super-interfaces */
                                    answer = getHandler( iface ,handlers);
                                }
                                if ( answer != null )
                                {
                                    keyFound = iface;
                                    break outer;
                                }
                            }
                        }
                        else
                        {
                            keyFound = c;
                            break;
                        }
                    }
                }
                if ( answer != null )
                {
                    if ( keyFound != forClass )
                    {
                        handlers.put( forClass, answer );
                    }
                }
            }
        }
        return answer;

    }
}
