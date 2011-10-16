package org.apache.commons.ognl.internal.entry;

import org.apache.commons.ognl.internal.CacheException;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
* User: Maurizio Cucchiara
* Date: 10/16/11
* Time: 9:29 PM
*/
public class GenericMethodParameterTypeFactory
    implements CacheEntryFactory<GenericMethodParameterTypeCacheEntry, Class<?>[]>
{
    public Class<?>[] create( GenericMethodParameterTypeCacheEntry entry )
        throws CacheException
    {
        Class<?>[] types;

        ParameterizedType param = (ParameterizedType) entry.type.getGenericSuperclass( );
        Type[] genTypes = entry.method.getGenericParameterTypes( );
        TypeVariable<?>[] declaredTypes = entry.method.getDeclaringClass( ).getTypeParameters( );

        types = new Class[genTypes.length];

        for ( int i = 0; i < genTypes.length; i++ )
        {
            TypeVariable<?> paramType = null;

            if ( TypeVariable.class.isInstance( genTypes[i] ) )
            {
                paramType = (TypeVariable<?>) genTypes[i];
            }
            else if ( GenericArrayType.class.isInstance( genTypes[i] ) )
            {
                paramType = (TypeVariable<?>) ( (GenericArrayType) genTypes[i] ).getGenericComponentType( );
            }
            else if ( ParameterizedType.class.isInstance( genTypes[i] ) )
            {
                types[i] = (Class<?>) ( (ParameterizedType) genTypes[i] ).getRawType( );
                continue;
            }
            else if ( Class.class.isInstance( genTypes[i] ) )
            {
                types[i] = (Class<?>) genTypes[i];
                continue;
            }

            Class<?> resolved = resolveType( param, paramType, declaredTypes );

            if ( resolved != null )
            {
                if ( GenericArrayType.class.isInstance( genTypes[i] ) )
                {
                    resolved = Array.newInstance( resolved, 0 ).getClass( );
                }

                types[i] = resolved;
                continue;
            }
            types[i] = entry.method.getParameterTypes( )[i];
        }

        return types;
    }

    private Class<?> resolveType( ParameterizedType param, TypeVariable<?> var, TypeVariable<?>[] declaredTypes )
    {
        if ( param.getActualTypeArguments( ).length < 1 )
        {
            return null;
        }

        for ( int i = 0; i < declaredTypes.length; i++ )
        {
            if ( !TypeVariable.class.isInstance( param.getActualTypeArguments( )[i] )
                && declaredTypes[i].getName( ).equals( var.getName( ) ) )
            {
                return (Class<?>) param.getActualTypeArguments( )[i];
            }
        }

        return null;
    }
}
