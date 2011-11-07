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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.ognl.enhance.ExpressionCompiler;
import org.apache.commons.ognl.enhance.OgnlExpressionCompiler;
import org.apache.commons.ognl.internal.Cache;
import org.apache.commons.ognl.internal.CacheException;
import org.apache.commons.ognl.internal.CacheFactory;
import org.apache.commons.ognl.internal.ClassCache;
import org.apache.commons.ognl.internal.ClassCacheHandler;
import org.apache.commons.ognl.internal.HashMapCacheFactory;
import org.apache.commons.ognl.internal.entry.CacheEntryFactory;
import org.apache.commons.ognl.internal.entry.ClassCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.DeclaredMethodCacheEntry;
import org.apache.commons.ognl.internal.entry.DeclaredMethodCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.FiedlCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.GenericMethodParameterTypeCacheEntry;
import org.apache.commons.ognl.internal.entry.GenericMethodParameterTypeFactory;
import org.apache.commons.ognl.internal.entry.MethodAccessCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.MethodAccessEntryValue;
import org.apache.commons.ognl.internal.entry.MethodPermCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.PermissionCacheEntry;
import org.apache.commons.ognl.internal.entry.PermissionCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.PropertyDescriptorCacheEntryFactory;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class used by internal OGNL API to do various things like:
 * <ul>
 * <li>Handles majority of reflection logic / caching.</li>
 * <li>Utility methods for casting strings / various numeric types used by {@link OgnlExpressionCompiler}.</li.
 * <li>Core runtime configuration point for setting/using global {@link TypeConverter} / {@link OgnlExpressionCompiler}
 * / {@link NullHandler} instances / etc..</li>
 * </ul>
 *
 * $Id$
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class OgnlRuntime
{

    /**
     * Constant expression used to indicate that a given method / property couldn't be found during reflection
     * operations.
     */
    public static final Object NotFound = new Object();

    public static final Object[] NoArguments = new Object[]{ };

    public static final Class<?>[] NoArgumentTypes = new Class<?>[]{ };

    /**
     * Token returned by TypeConverter for no conversion possible
     */
    public static final Object NoConversionPossible = "ognl.NoConversionPossible";

    /**
     * Not an indexed property
     */
    public static final int INDEXED_PROPERTY_NONE = 0;

    /**
     * JavaBeans IndexedProperty
     */
    public static final int INDEXED_PROPERTY_INT = 1;

    /**
     * OGNL ObjectIndexedProperty
     */
    public static final int INDEXED_PROPERTY_OBJECT = 2;

    /**
     * Constant string representation of null string.
     */
    public static final String NULL_STRING = "" + null;

    /**
     * Java beans standard set method prefix.
     */
    private static final String SET_PREFIX = "set";

    /**
     * Java beans standard get method prefix.
     */
    private static final String GET_PREFIX = "get";

    /**
     * Java beans standard is<Foo> boolean getter prefix.
     */
    private static final String IS_PREFIX = "is";

    /**
     * Prefix padding for hexadecimal numbers to HEX_LENGTH.
     */
    private static final Map<Integer, String> HEX_PADDING = new HashMap<Integer, String>();

    private static final int HEX_LENGTH = 8;

    /**
     * Returned by <CODE>getUniqueDescriptor()</CODE> when the object is <CODE>null</CODE>.
     */
    private static final String NULL_OBJECT_STRING = "<null>";

    private static CacheFactory cacheFactory = new HashMapCacheFactory();

    static final ClassCache<MethodAccessor> _methodAccessors = cacheFactory.createClassCache();

    static final ClassCache<PropertyAccessor> _propertyAccessors = cacheFactory.createClassCache();

    static final ClassCache<ElementsAccessor> _elementsAccessors = cacheFactory.createClassCache();

    static final ClassCache<NullHandler> _nullHandlers = cacheFactory.createClassCache();

    static final ClassCache<Map<String, PropertyDescriptor>> _propertyDescriptorCache =
        cacheFactory.createClassCache( new PropertyDescriptorCacheEntryFactory() );

    static final ClassCache<List<Constructor<?>>> _constructorCache =
        cacheFactory.createClassCache( new ClassCacheEntryFactory<List<Constructor<?>>>()
        {
            public List<Constructor<?>> create( Class<?> key )
                throws CacheException
            {
                return Arrays.<Constructor<?>>asList( key.getConstructors() );
            }
        } );

    static final Cache<DeclaredMethodCacheEntry, Map<String, List<Method>>> _methodCache =
        cacheFactory.createCache( new DeclaredMethodCacheEntryFactory() );

    static final Cache<PermissionCacheEntry, Permission> _invokePermissionCache =
        cacheFactory.createCache( new PermissionCacheEntryFactory() );

    static final ClassCache<Map<String, Field>> _fieldCache =
        cacheFactory.createClassCache( new FiedlCacheEntryFactory() );

    static final Map<String, Class<?>> _primitiveTypes = new HashMap<String, Class<?>>( 101 );

    static final Map<Class<?>, Object> _primitiveDefaults = new HashMap<Class<?>, Object>( 20 );

    static final Cache<Method, Class<?>[]> _methodParameterTypesCache =
        cacheFactory.createCache( new CacheEntryFactory<Method, Class<?>[]>()
        {
            public Class<?>[] create( Method key )
                throws CacheException
            {
                return key.getParameterTypes();
            }
        } );

    static final Cache<GenericMethodParameterTypeCacheEntry, Class<?>[]> _genericMethodParameterTypesCache =
        cacheFactory.createCache( new GenericMethodParameterTypeFactory() );

    static final Cache<Constructor<?>, Class<?>[]> _ctorParameterTypesCache =
        cacheFactory.createCache( new CacheEntryFactory<Constructor<?>, Class<?>[]>()
        {
            public Class<?>[] create( Constructor<?> key )
                throws CacheException
            {
                return key.getParameterTypes();
            }
        } );

    static SecurityManager _securityManager = System.getSecurityManager();

    static final EvaluationPool _evaluationPool = new EvaluationPool();

    static final ObjectArrayPool _objectArrayPool = new ObjectArrayPool();

    static final Cache<Method, MethodAccessEntryValue> _methodAccessCache =
        cacheFactory.createCache( new MethodAccessCacheEntryFactory() );

    private static final MethodPermCacheEntryFactory methodPermCacheEntryFactory =
        new MethodPermCacheEntryFactory( _securityManager );

    static final Cache<Method, Boolean> _methodPermCache = cacheFactory.createCache( methodPermCacheEntryFactory );

    static ClassCacheInspector _cacheInspector;

    /**
     * Expression compiler used by {@link Ognl#compileExpression(OgnlContext, Object, String)} calls.
     */
    private static OgnlExpressionCompiler _compiler;

    private static Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_CLASSES = new IdentityHashMap<Class<?>, Class<?>>();

    /**
     * Used to provide primitive type equivalent conversions into and out of native / object types.
     */
    static
    {
        PRIMITIVE_WRAPPER_CLASSES.put( Boolean.TYPE, Boolean.class );
        PRIMITIVE_WRAPPER_CLASSES.put( Boolean.class, Boolean.TYPE );
        PRIMITIVE_WRAPPER_CLASSES.put( Byte.TYPE, Byte.class );
        PRIMITIVE_WRAPPER_CLASSES.put( Byte.class, Byte.TYPE );
        PRIMITIVE_WRAPPER_CLASSES.put( Character.TYPE, Character.class );
        PRIMITIVE_WRAPPER_CLASSES.put( Character.class, Character.TYPE );
        PRIMITIVE_WRAPPER_CLASSES.put( Short.TYPE, Short.class );
        PRIMITIVE_WRAPPER_CLASSES.put( Short.class, Short.TYPE );
        PRIMITIVE_WRAPPER_CLASSES.put( Integer.TYPE, Integer.class );
        PRIMITIVE_WRAPPER_CLASSES.put( Integer.class, Integer.TYPE );
        PRIMITIVE_WRAPPER_CLASSES.put( Long.TYPE, Long.class );
        PRIMITIVE_WRAPPER_CLASSES.put( Long.class, Long.TYPE );
        PRIMITIVE_WRAPPER_CLASSES.put( Float.TYPE, Float.class );
        PRIMITIVE_WRAPPER_CLASSES.put( Float.class, Float.TYPE );
        PRIMITIVE_WRAPPER_CLASSES.put( Double.TYPE, Double.class );
        PRIMITIVE_WRAPPER_CLASSES.put( Double.class, Double.TYPE );
    }

    private static final Map<Class<? extends Number>, String> NUMERIC_CASTS =
        new HashMap<Class<? extends Number>, String>();

    /**
     * Constant strings for casting different primitive types.
     */
    static
    {
        NUMERIC_CASTS.put( Double.class, "(double)" );
        NUMERIC_CASTS.put( Float.class, "(float)" );
        NUMERIC_CASTS.put( Integer.class, "(int)" );
        NUMERIC_CASTS.put( Long.class, "(long)" );
        NUMERIC_CASTS.put( BigDecimal.class, "(double)" );
        NUMERIC_CASTS.put( BigInteger.class, "" );
    }

    private static final Map<Class<?>, String> NUMERIC_VALUES = new HashMap<Class<?>, String>();

    /**
     * Constant strings for getting the primitive value of different native types on the generic {@link Number} object
     * interface. (or the less generic BigDecimal/BigInteger types)
     */
    static
    {
        NUMERIC_VALUES.put( Double.class, "doubleValue()" );
        NUMERIC_VALUES.put( Float.class, "floatValue()" );
        NUMERIC_VALUES.put( Integer.class, "intValue()" );
        NUMERIC_VALUES.put( Long.class, "longValue()" );
        NUMERIC_VALUES.put( Short.class, "shortValue()" );
        NUMERIC_VALUES.put( Byte.class, "byteValue()" );
        NUMERIC_VALUES.put( BigDecimal.class, "doubleValue()" );
        NUMERIC_VALUES.put( BigInteger.class, "doubleValue()" );
        NUMERIC_VALUES.put( Boolean.class, "booleanValue()" );
    }

    private static final Map<Class<? extends Number>, String> NUMERIC_LITERALS =
        new HashMap<Class<? extends Number>, String>();

    /**
     * Numeric primitive literal string expressions.
     */
    static
    {
        NUMERIC_LITERALS.put( Integer.class, "" );
        NUMERIC_LITERALS.put( Integer.TYPE, "" );
        NUMERIC_LITERALS.put( Long.class, "l" );
        NUMERIC_LITERALS.put( Long.TYPE, "l" );
        NUMERIC_LITERALS.put( BigInteger.class, "d" );
        NUMERIC_LITERALS.put( Float.class, "f" );
        NUMERIC_LITERALS.put( Float.TYPE, "f" );
        NUMERIC_LITERALS.put( Double.class, "d" );
        NUMERIC_LITERALS.put( Double.TYPE, "d" );
        NUMERIC_LITERALS.put( BigInteger.class, "d" );
        NUMERIC_LITERALS.put( BigDecimal.class, "d" );
    }

    private static final Map<Class<?>, Object> NUMERIC_DEFAULTS = new HashMap<Class<?>, Object>();

    static
    {
        NUMERIC_DEFAULTS.put( Boolean.class, Boolean.FALSE );
        NUMERIC_DEFAULTS.put( Byte.class, (byte) 0 );
        NUMERIC_DEFAULTS.put( Short.class, (short) 0 );
        NUMERIC_DEFAULTS.put( Character.class, (char) 0 );
        NUMERIC_DEFAULTS.put( Integer.class, 0 );
        NUMERIC_DEFAULTS.put( Long.class, 0L );
        NUMERIC_DEFAULTS.put( Float.class, 0.0f );
        NUMERIC_DEFAULTS.put( Double.class, 0.0 );

        NUMERIC_DEFAULTS.put( BigInteger.class, new BigInteger( "0" ) );
        NUMERIC_DEFAULTS.put( BigDecimal.class, new BigDecimal( 0.0 ) );
    }

    static
    {
        PropertyAccessor p = new ArrayPropertyAccessor();

        setPropertyAccessor( Object.class, new ObjectPropertyAccessor() );
        setPropertyAccessor( byte[].class, p );
        setPropertyAccessor( short[].class, p );
        setPropertyAccessor( char[].class, p );
        setPropertyAccessor( int[].class, p );
        setPropertyAccessor( long[].class, p );
        setPropertyAccessor( float[].class, p );
        setPropertyAccessor( double[].class, p );
        setPropertyAccessor( Object[].class, p );
        setPropertyAccessor( List.class, new ListPropertyAccessor() );
        setPropertyAccessor( Map.class, new MapPropertyAccessor() );
        setPropertyAccessor( Set.class, new SetPropertyAccessor() );
        setPropertyAccessor( Iterator.class, new IteratorPropertyAccessor() );
        setPropertyAccessor( Enumeration.class, new EnumerationPropertyAccessor() );

        ElementsAccessor e = new ArrayElementsAccessor();

        setElementsAccessor( Object.class, new ObjectElementsAccessor() );
        setElementsAccessor( byte[].class, e );
        setElementsAccessor( short[].class, e );
        setElementsAccessor( char[].class, e );
        setElementsAccessor( int[].class, e );
        setElementsAccessor( long[].class, e );
        setElementsAccessor( float[].class, e );
        setElementsAccessor( double[].class, e );
        setElementsAccessor( Object[].class, e );
        setElementsAccessor( Collection.class, new CollectionElementsAccessor() );
        setElementsAccessor( Map.class, new MapElementsAccessor() );
        setElementsAccessor( Iterator.class, new IteratorElementsAccessor() );
        setElementsAccessor( Enumeration.class, new EnumerationElementsAccessor() );
        setElementsAccessor( Number.class, new NumberElementsAccessor() );

        NullHandler nh = new ObjectNullHandler();

        setNullHandler( Object.class, nh );
        setNullHandler( byte[].class, nh );
        setNullHandler( short[].class, nh );
        setNullHandler( char[].class, nh );
        setNullHandler( int[].class, nh );
        setNullHandler( long[].class, nh );
        setNullHandler( float[].class, nh );
        setNullHandler( double[].class, nh );
        setNullHandler( Object[].class, nh );

        MethodAccessor ma = new ObjectMethodAccessor();

        setMethodAccessor( Object.class, ma );
        setMethodAccessor( byte[].class, ma );
        setMethodAccessor( short[].class, ma );
        setMethodAccessor( char[].class, ma );
        setMethodAccessor( int[].class, ma );
        setMethodAccessor( long[].class, ma );
        setMethodAccessor( float[].class, ma );
        setMethodAccessor( double[].class, ma );
        setMethodAccessor( Object[].class, ma );

        _primitiveTypes.put( "boolean", Boolean.TYPE );
        _primitiveTypes.put( "byte", Byte.TYPE );
        _primitiveTypes.put( "short", Short.TYPE );
        _primitiveTypes.put( "char", Character.TYPE );
        _primitiveTypes.put( "int", Integer.TYPE );
        _primitiveTypes.put( "long", Long.TYPE );
        _primitiveTypes.put( "float", Float.TYPE );
        _primitiveTypes.put( "double", Double.TYPE );

        _primitiveDefaults.put( Boolean.TYPE, Boolean.FALSE );
        _primitiveDefaults.put( Boolean.class, Boolean.FALSE );
        _primitiveDefaults.put( Byte.TYPE, (byte) 0 );
        _primitiveDefaults.put( Byte.class, (byte) 0 );
        _primitiveDefaults.put( Short.TYPE, (short) 0 );
        _primitiveDefaults.put( Short.class, (short) 0 );
        _primitiveDefaults.put( Character.TYPE, (char) 0 );
        _primitiveDefaults.put( Integer.TYPE, 0 );
        _primitiveDefaults.put( Long.TYPE, 0L );
        _primitiveDefaults.put( Float.TYPE, 0.0f );
        _primitiveDefaults.put( Double.TYPE, 0.0 );

        _primitiveDefaults.put( BigInteger.class, new BigInteger( "0" ) );
        _primitiveDefaults.put( BigDecimal.class, new BigDecimal( 0.0 ) );
    }

    /**
     * Clears all of the cached reflection information normally used to improve the speed of expressions that operate on
     * the same classes or are executed multiple times.
     * <p>
     * <strong>Warning:</strong> Calling this too often can be a huge performance drain on your expressions - use with
     * care.
     * </p>
     */
    public static void clearCache()
    {
        _methodParameterTypesCache.clear();
        _ctorParameterTypesCache.clear();
        _propertyDescriptorCache.clear();
        _constructorCache.clear();
        _methodCache.clear();
        _invokePermissionCache.clear();
        _fieldCache.clear();
        _methodAccessCache.clear();
    }

    /**
     * @deprecated This always returns true now since OGNL requires Java 1.5.
     * @return Always returns true.
     */
    public static boolean isJdk15()
    {
        return true;
    }

    public static String getNumericValueGetter( Class<?> type )
    {
        return NUMERIC_VALUES.get( type );
    }

    public static Class<?> getPrimitiveWrapperClass( Class<?> primitiveClass )
    {
        return PRIMITIVE_WRAPPER_CLASSES.get( primitiveClass );
    }

    public static String getNumericCast( Class<? extends Number> type )
    {
        return NUMERIC_CASTS.get( type );
    }

    public static String getNumericLiteral( Class<? extends Number> type )
    {
        return NUMERIC_LITERALS.get( type );
    }

    public static void setCompiler( OgnlExpressionCompiler compiler )
    {
        _compiler = compiler;
    }

    /**
     * @deprecated use getCompiler(OgnlContext) instead
     */
    public static OgnlExpressionCompiler getCompiler()
    {
        return getCompiler( null );
    }

    public static OgnlExpressionCompiler getCompiler( OgnlContext ognlContext )
    {
        if ( _compiler == null )
        {
            try
            {
                OgnlRuntime.classForName( ognlContext, "javassist.ClassPool" );
                _compiler = new ExpressionCompiler();
            }
            catch ( ClassNotFoundException e )
            {
                throw new IllegalArgumentException(
                    "Javassist library is missing in classpath! Please add missed dependency!", e );
            }
        }
        return _compiler;
    }

    public static void compileExpression( OgnlContext context, Node expression, Object root )
        throws Exception
    {
        getCompiler( context ).compileExpression( context, expression, root );
    }

    /**
     * Gets the "target" class of an object for looking up accessors that are registered on the target. If the object is
     * a Class object this will return the Class itself, else it will return object's getClass() result.
     */
    public static Class<?> getTargetClass( Object o )
    {
        return ( o == null ) ? null : ( ( o instanceof Class ) ? (Class<?>) o : o.getClass() );
    }

    /**
     * Returns the base name (the class name without the package name prepended) of the object given.
     */
    public static String getBaseName( Object o )
    {
        return ( o == null ) ? null : getClassBaseName( o.getClass() );
    }

    /**
     * Returns the base name (the class name without the package name prepended) of the class given.
     */
    public static String getClassBaseName( Class<?> clazz )
    {
        String className = clazz.getName();
        return className.substring( className.lastIndexOf( '.' ) + 1 );
    }

    public static String getClassName( Object object, boolean fullyQualified )
    {
        if ( !( object instanceof Class ) )
        {
            object = object.getClass();
        }

        return getClassName( (Class<?>) object, fullyQualified );
    }

    public static String getClassName( Class<?> clazz, boolean fullyQualified )
    {
        return fullyQualified ? clazz.getName() : getClassBaseName( clazz );
    }

    /**
     * Returns the package name of the object's class.
     */
    public static String getPackageName( Object object )
    {
        return ( object == null ) ? null : getClassPackageName( object.getClass() );
    }

    /**
     * Returns the package name of the class given.
     */
    public static String getClassPackageName( Class<?> clazz )
    {
        String className = clazz.getName();
        int index = className.lastIndexOf( '.' );

        return ( index < 0 ) ? null : className.substring( 0, index );
    }

    /**
     * Returns a "pointer" string in the usual format for these things - 0x<hex digits>.
     */
    public static String getPointerString( int num )
    {
        String hex = Integer.toHexString( num ), pad;
        Integer l = hex.length();

        // stringBuilder.append(HEX_PREFIX);
        if ( ( pad = HEX_PADDING.get( l ) ) == null )
        {
            StringBuilder paddingStringBuilder = new StringBuilder();

            for ( int i = hex.length(); i < HEX_LENGTH; i++ )
            {
                paddingStringBuilder.append( '0' );
            }
            pad = paddingStringBuilder.toString();
            HEX_PADDING.put( l, pad );
        }
        return new StringBuilder().append( pad ).append( hex ).toString();
    }

    /**
     * Returns a "pointer" string in the usual format for these things - 0x<hex digits> for the object given. This will
     * always return a unique value for each object.
     */
    public static String getPointerString( Object object )
    {
        return getPointerString( ( object == null ) ? 0 : System.identityHashCode( object ) );
    }

    /**
     * Returns a unique descriptor string that includes the object's class and a unique integer identifier. If
     * fullyQualified is true then the class name will be fully qualified to include the package name, else it will be
     * just the class' base name.
     */
    public static String getUniqueDescriptor( Object object, boolean fullyQualified )
    {
        StringBuilder stringBuilder = new StringBuilder();

        if ( object != null )
        {
            if ( object instanceof Proxy )
            {
                Class<?> interfaceClass = object.getClass().getInterfaces()[0];

                String className = getClassName( interfaceClass, fullyQualified );
                stringBuilder.append( className ).append( '^' );
                object = Proxy.getInvocationHandler( object );
            }
            String className = getClassName( object, fullyQualified );
            String pointerString = getPointerString( object );
            stringBuilder.append( className ).append( '@' ).append( pointerString );
        }
        else
        {
            stringBuilder.append( NULL_OBJECT_STRING );
        }
        return stringBuilder.toString();
    }

    /**
     * Returns a unique descriptor string that includes the object's class' base name and a unique integer identifier.
     */
    public static String getUniqueDescriptor( Object object )
    {
        return getUniqueDescriptor( object, false );
    }

    /**
     * Utility to convert a List into an Object[] array. If the list is zero elements this will return a constant array;
     * toArray() on List always returns a new object and this is wasteful for our purposes.
     */
    public static <T> Object[] toArray( List<T> list )
    {
        Object[] array;
        int size = list.size();

        if ( size == 0 )
        {
            array = NoArguments;
        }
        else
        {
            array = getObjectArrayPool().create( size );
            for ( int i = 0; i < size; i++ )
            {
                array[i] = list.get( i );
            }
        }
        return array;
    }

    /**
     * Returns the parameter types of the given method.
     */
    public static Class<?>[] getParameterTypes( Method method )
        throws CacheException
    {
        return _methodParameterTypesCache.get( method );

    }

    /**
     * Finds the appropriate parameter types for the given {@link Method} and {@link Class} instance of the type the
     * method is associated with. Correctly finds generic types if running in >= 1.5 jre as well.
     *
     * @param type The class type the method is being executed against.
     * @param method    The method to find types for.
     * @return Array of parameter types for the given method.
     * @throws org.apache.commons.ognl.internal.CacheException
     */
    public static Class<?>[] findParameterTypes( Class<?> type, Method method )
        throws CacheException
    {
        if ( type == null )
        {
            return getParameterTypes( method );
        }

        if ( type.getGenericSuperclass() == null || !ParameterizedType.class.isInstance(
            type.getGenericSuperclass() ) || method.getDeclaringClass().getTypeParameters() == null )
        {
            return getParameterTypes( method );
        }

        return _genericMethodParameterTypesCache.get( new GenericMethodParameterTypeCacheEntry( method, type ) );
    }

    /**
     * Returns the parameter types of the given method.
     * @param constructor
     * @return
     * @throws org.apache.commons.ognl.internal.CacheException
     */
    public static Class<?>[] getParameterTypes( Constructor<?> constructor )
        throws CacheException
    {
        return _ctorParameterTypesCache.get( constructor );
    }

    /**
     * Gets the SecurityManager that OGNL uses to determine permissions for invoking methods.
     *
     * @return SecurityManager for OGNL
     */
    public static SecurityManager getSecurityManager()
    {
        return _securityManager;
    }

    /**
     * Sets the SecurityManager that OGNL uses to determine permissions for invoking methods.
     *
     * @param value SecurityManager to set
     */
    public static void setSecurityManager( SecurityManager value )
    {
        _securityManager = value;
        methodPermCacheEntryFactory.setSecurityManager( value );
    }

    /**
     * Permission will be named "invoke.<declaring-class>.<method-name>".
     * @param method
     * @return
     * @throws org.apache.commons.ognl.internal.CacheException
     */
    public static Permission getPermission( Method method )
        throws CacheException
    {
        return _invokePermissionCache.get( new PermissionCacheEntry( method ) );
    }

    public static Object invokeMethod( Object target, Method method, Object[] argsArray )
        throws InvocationTargetException, IllegalAccessException, CacheException
    {
        Object result;

        if ( _securityManager != null )
        {
            if ( !_methodPermCache.get( method ) )
            {
                throw new IllegalAccessException( "Method [" + method + "] cannot be accessed." );
            }
        }
        MethodAccessEntryValue entry = _methodAccessCache.get( method );

        if ( !entry.isAccessible() )
        {
            // only synchronize method invocation if it actually requires it
            synchronized ( method )
            {

                if ( entry.isNotPublic() && !entry.isAccessible() )
                {
                    method.setAccessible( true );
                }

                result = method.invoke( target, argsArray );

                if ( !entry.isAccessible() )
                {
                    method.setAccessible( false );
                }
            }
        }
        else
        {
            result = method.invoke( target, argsArray );
        }

        return result;
    }

    /**
     * Gets the class for a method argument that is appropriate for looking up methods by reflection, by looking for the
     * standard primitive wrapper classes and exchanging for them their underlying primitive class objects. Other
     * classes are passed through unchanged.
     *
     * @param arg an object that is being passed to a method
     * @return the class to use to look up the method
     */
    public static Class<?> getArgClass( Object arg )
    {
        if ( arg == null )
        {
            return null;
        }
        Class<?> clazz = arg.getClass();
        if ( clazz == Boolean.class )
        {
            return Boolean.TYPE;
        }
        else if ( clazz.getSuperclass() == Number.class )
        {
            if ( clazz == Integer.class )
            {
                return Integer.TYPE;
            }
            if ( clazz == Double.class )
            {
                return Double.TYPE;
            }
            if ( clazz == Byte.class )
            {
                return Byte.TYPE;
            }
            if ( clazz == Long.class )
            {
                return Long.TYPE;
            }
            if ( clazz == Float.class )
            {
                return Float.TYPE;
            }
            if ( clazz == Short.class )
            {
                return Short.TYPE;
            }
        }
        else if ( clazz == Character.class )
        {
            return Character.TYPE;
        }
        return clazz;
    }

    /**
     * Tells whether the given object is compatible with the given class ---that is, whether the given object can be
     * passed as an argument to a method or constructor whose parameter type is the given class. If object is null this
     * will return true because null is compatible with any type.
     */
    public static boolean isTypeCompatible( Object object, Class<?> clazz )
    {
        boolean result = true;

        if ( object != null )
        {
            if ( clazz.isPrimitive() )
            {
                if ( getArgClass( object ) != clazz )
                {
                    result = false;
                }
            }
            else if ( !clazz.isInstance( object ) )
            {
                result = false;
            }
        }
        return result;
    }

    /**
     * Tells whether the given array of objects is compatible with the given array of classes---that is, whether the
     * given array of objects can be passed as arguments to a method or constructor whose parameter types are the given
     * array of classes.
     */
    public static boolean areArgsCompatible( Object[] args, Class<?>[] classes )
    {
        return areArgsCompatible( args, classes, null );
    }

    public static boolean areArgsCompatible( Object[] args, Class<?>[] classes, Method method )
    {
        boolean result = true;
        boolean varArgs = method != null && method.isVarArgs();

        if ( args.length != classes.length && !varArgs )
        {
            result = false;
        }
        else if ( varArgs )
        {
            for ( int index = 0; result && ( index < args.length ); ++index )
            {
                if ( index >= classes.length )
                {
                    break;
                }

                result = isTypeCompatible( args[index], classes[index] );

                if ( !result && classes[index].isArray() )
                {
                    result = isTypeCompatible( args[index], classes[index].getComponentType() );
                }
            }
        }
        else
        {
            for ( int index = 0; result && ( index < args.length ); ++index )
            {
                result = isTypeCompatible( args[index], classes[index] );
            }
        }
        return result;
    }

    /**
     * Tells whether the first array of classes is more specific than the second. Assumes that the two arrays are of the
     * same length.
     */
    public static boolean isMoreSpecific( Class<?>[] classes1, Class<?>[] classes2 )
    {
        for ( int index = 0; index < classes1.length; ++index )
        {
            Class<?> class1 = classes1[index], class2 = classes2[index];
            if ( class1 != class2 )
            {
                if ( class1.isPrimitive() )
                {
                    return true;
                }
                else if ( class1.isAssignableFrom( class2 ) )
                {
                    return false;
                }
                else if ( class2.isAssignableFrom( class1 ) )
                {
                    return true;
                }
            }
        }

        // They are the same! So the first is not more specific than the second.
        return false;
    }

    /**
     * @deprecated This method is no longer used.
     */
    public static String getModifierString( int modifiers )
    {
        String modifierString;

        if ( Modifier.isPublic( modifiers ) )
        {
            modifierString = "public";
        }
        else if ( Modifier.isProtected( modifiers ) )
        {
            modifierString = "protected";
        }
        else if ( Modifier.isPrivate( modifiers ) )
        {
            modifierString = "private";
        }
        else
        {
            modifierString = "";
        }
        if ( Modifier.isStatic( modifiers ) )
        {
            modifierString = "static " + modifierString;
        }
        if ( Modifier.isFinal( modifiers ) )
        {
            modifierString = "final " + modifierString;
        }
        if ( Modifier.isNative( modifiers ) )
        {
            modifierString = "native " + modifierString;
        }
        if ( Modifier.isSynchronized( modifiers ) )
        {
            modifierString = "synchronized " + modifierString;
        }
        if ( Modifier.isTransient( modifiers ) )
        {
            modifierString = "transient " + modifierString;
        }
        return modifierString;
    }

    public static Class<?> classForName( OgnlContext context, String className )
        throws ClassNotFoundException
    {
        Class<?> result = _primitiveTypes.get( className );

        if ( result == null )
        {
            ClassResolver resolver;

            if ( ( context == null ) || ( ( resolver = context.getClassResolver() ) == null ) )
            {
                resolver = OgnlContext.DEFAULT_CLASS_RESOLVER;
            }
            result = resolver.classForName( className, context );
        }

        if ( result == null )
        {
            throw new ClassNotFoundException( "Unable to resolve class: " + className );
        }

        return result;
    }

    public static boolean isInstance( OgnlContext context, Object value, String className )
        throws OgnlException
    {
        try
        {
            Class<?> clazz = classForName( context, className );
            return clazz.isInstance( value );
        }
        catch ( ClassNotFoundException e )
        {
            throw new OgnlException( "No such class: " + className, e );
        }
    }

    public static Object getPrimitiveDefaultValue( Class<?> forClass )
        throws OgnlException
    {
        return _primitiveDefaults.get( forClass );
    }

    public static Object getNumericDefaultValue( Class<?> forClass )
    {
        return NUMERIC_DEFAULTS.get( forClass );
    }

    public static Object getConvertedType( OgnlContext context, Object target, Member member, String propertyName,
                                           Object value, Class<?> type )
        throws OgnlException
    {
        return context.getTypeConverter().convertValue( context, target, member, propertyName, value, type );
    }

    public static boolean getConvertedTypes( OgnlContext context, Object target, Member member, String propertyName,
                                             Class<?>[] parameterTypes, Object[] args, Object[] newArgs )
        throws OgnlException
    {
        boolean result = false;

        if ( parameterTypes.length == args.length )
        {
            result = true;
            for ( int i = 0; result && ( i <= parameterTypes.length - 1 ); i++ )
            {
                Object arg = args[i];
                Class<?> type = parameterTypes[i];

                if ( isTypeCompatible( arg, type ) )
                {
                    newArgs[i] = arg;
                }
                else
                {
                    Object convertedType = getConvertedType( context, target, member, propertyName, arg, type );

                    if ( convertedType == OgnlRuntime.NoConversionPossible )
                    {
                        result = false;
                    }
                    else
                    {
                        newArgs[i] = convertedType;
                    }
                }
            }
        }
        return result;
    }

    public static Method getConvertedMethodAndArgs( OgnlContext context, Object target, String propertyName,
                                                    List<Method> methods, Object[] args, Object[] newArgs )
        throws OgnlException
    {
        Method convertedMethod = null;
        TypeConverter typeConverter = context.getTypeConverter();

        if ( ( typeConverter != null ) && ( methods != null ) )
        {
            int methodsSize = methods.size();
            for ( int i = 0; ( convertedMethod == null ) && ( i < methodsSize ); i++ )
            {
                Method method = methods.get( i );
                Class<?>[] parameterTypes =
                    findParameterTypes( target != null ? target.getClass() : null, method );// getParameterTypes(method);

                if ( getConvertedTypes( context, target, method, propertyName, parameterTypes, args, newArgs ) )
                {
                    convertedMethod = method;
                }
            }
        }
        return convertedMethod;
    }

    public static Constructor<?> getConvertedConstructorAndArgs( OgnlContext context, Object target,
                                                                 List<Constructor<?>> constructors, Object[] args,
                                                                 Object[] newArgs )
        throws OgnlException
    {
        Constructor<?> constructor = null;
        TypeConverter typeConverter = context.getTypeConverter();

        if ( ( typeConverter != null ) && ( constructors != null ) )
        {
            for ( int i = 0; ( constructor == null ) && ( i < constructors.size() ); i++ )
            {
                Constructor<?> ctor = constructors.get( i );
                Class<?>[] parameterTypes = getParameterTypes( ctor );

                if ( getConvertedTypes( context, target, ctor, null, parameterTypes, args, newArgs ) )
                {
                    constructor = ctor;
                }
            }
        }
        return constructor;
    }

    /**
     * Gets the appropriate method to be called for the given target, method name and arguments. If successful this
     * method will return the Method within the target that can be called and the converted arguments in actualArgs. If
     * unsuccessful this method will return null and the actualArgs will be empty.
     *
     * @param context      The current execution context.
     * @param source       Target object to run against or method name.
     * @param target       Instance of object to be run against.
     * @param propertyName Name of property to get method of.
     * @param methods      List of current known methods.
     * @param args         Arguments originally passed in.
     * @param actualArgs   Converted arguments.
     * @return Best method match or null if none could be found.
     */
    public static Method getAppropriateMethod( OgnlContext context, Object source, Object target, String propertyName,
                                               List<Method> methods, Object[] args, Object[] actualArgs )
        throws OgnlException
    {
        Method appropriateMethod = null;
        Class<?>[] resultParameterTypes = null;

        if ( methods != null )
        {
            for ( Method method : methods )
            {
                Class<?> typeClass = target != null ? target.getClass() : null;
                if ( typeClass == null && source != null && Class.class.isInstance( source ) )
                {
                    typeClass = (Class<?>) source;
                }

                Class<?>[] mParameterTypes = findParameterTypes( typeClass, method );

                if ( areArgsCompatible( args, mParameterTypes, method ) &&
                    ( ( appropriateMethod == null ) || isMoreSpecific( mParameterTypes, resultParameterTypes ) ) )
                {
                    appropriateMethod = method;
                    resultParameterTypes = mParameterTypes;
                    System.arraycopy( args, 0, actualArgs, 0, args.length );

                    for ( int i = 0; i < mParameterTypes.length; i++ )
                    {
                        Class<?> type = mParameterTypes[i];

                        if ( type.isPrimitive() && ( actualArgs[i] == null ) )
                        {
                            actualArgs[i] = getConvertedType( context, source, appropriateMethod, propertyName, null, type );
                        }
                    }
                }
            }
        }

        if ( appropriateMethod == null )
        {
            appropriateMethod = getConvertedMethodAndArgs( context, target, propertyName, methods, args, actualArgs );
        }

        return appropriateMethod;
    }

    public static Object callAppropriateMethod( OgnlContext context, Object source, Object target, String methodName,
                                                String propertyName, List<Method> methods, Object[] args )
        throws MethodFailedException
    {
        Throwable cause = null;
        Object[] actualArgs = _objectArrayPool.create( args.length );

        try
        {
            Method method = getAppropriateMethod( context, source, target, propertyName, methods, args, actualArgs );

            if ( ( method == null ) || !isMethodAccessible( context, source, method, propertyName ) )
            {
                StringBuilder buffer = new StringBuilder();
                String className = "";

                if ( target != null )
                {
                    className = target.getClass().getName() + ".";
                }

                for ( int i = 0, ilast = args.length - 1; i <= ilast; i++ )
                {
                    Object arg = args[i];

                    buffer.append( ( arg == null ) ? NULL_STRING : arg.getClass().getName() );
                    if ( i < ilast )
                    {
                        buffer.append( ", " );
                    }
                }

                throw new NoSuchMethodException( className + methodName + "(" + buffer + ")" );
            }

            Object[] convertedArgs = actualArgs;

            if ( method.isVarArgs() )
            {
                Class<?>[] parmTypes = method.getParameterTypes();

                // split arguments in to two dimensional array for varargs reflection invocation
                // where it is expected that the parameter passed in to invoke the method
                // will look like "new Object[] { arrayOfNonVarArgsArguments, arrayOfVarArgsArguments }"

                for ( int i = 0; i < parmTypes.length; i++ )
                {
                    if ( parmTypes[i].isArray() )
                    {
                        convertedArgs = new Object[i + 1];
                        System.arraycopy( actualArgs, 0, convertedArgs, 0, convertedArgs.length );

                        Object[] varArgs;

                        // if they passed in varargs arguments grab them and dump in to new varargs array

                        if ( actualArgs.length > i )
                        {
                            List<Object> varArgsList = new ArrayList<Object>();
                            for ( int j = i; j < actualArgs.length; j++ )
                            {
                                if ( actualArgs[j] != null )
                                {
                                    varArgsList.add( actualArgs[j] );
                                }
                            }

                            varArgs = varArgsList.toArray();
                        }
                        else
                        {
                            varArgs = new Object[0];
                        }

                        convertedArgs[i] = varArgs;
                        break;
                    }
                }
            }

            return invokeMethod( target, method, convertedArgs );

        }
        catch ( NoSuchMethodException e )
        {
            cause = e;
        }
        catch ( IllegalAccessException e )
        {
            cause = e;
        }
        catch ( InvocationTargetException e )
        {
            cause = e.getTargetException();
        }
        catch ( OgnlException e )
        {
            cause = e;
        }
        finally
        {
            _objectArrayPool.recycle( actualArgs );
        }

        throw new MethodFailedException( source, methodName, cause );
    }

    public static Object callStaticMethod( OgnlContext context, String className, String methodName, Object[] args )
        throws OgnlException
    {
        try
        {
            Class<?> targetClass = classForName( context, className );
            if ( targetClass == null )
            {
                throw new ClassNotFoundException( "Unable to resolve class with name " + className );
            }

            MethodAccessor methodAccessor = getMethodAccessor( targetClass );

            return methodAccessor.callStaticMethod( context, targetClass, methodName, args );
        }
        catch ( ClassNotFoundException ex )
        {
            throw new MethodFailedException( className, methodName, ex );
        }
    }

    /**
     * Invokes the specified method against the target object.
     *
     * @param context    The current execution context.
     * @param target     The object to invoke the method on.
     * @param methodName Name of the method - as in "getValue" or "add", etc..
     * @param args       Optional arguments needed for method.
     * @return Result of invoking method.
     * @throws OgnlException For lots of different reasons.
     */
    public static Object callMethod( OgnlContext context, Object target, String methodName, Object[] args )
        throws OgnlException
    {
        if ( target == null )
        {
            throw new NullPointerException( "target is null for method " + methodName );
        }

        return getMethodAccessor( target.getClass() ).callMethod( context, target, methodName, args );
    }

    public static Object callConstructor( OgnlContext context, String className, Object[] args )
        throws OgnlException
    {
        Throwable cause = null;
        Object[] actualArgs = args;

        try
        {
            Constructor<?> ctor = null;
            Class<?>[] ctorParameterTypes = null;
            Class<?> target = classForName( context, className );
            List<Constructor<?>> constructors = getConstructors( target );

            for ( Constructor<?> constructor : constructors )
            {
                Class<?>[] cParameterTypes = getParameterTypes( constructor );

                if ( areArgsCompatible( args, cParameterTypes ) && ( ctor == null || isMoreSpecific( cParameterTypes,
                                                                                                     ctorParameterTypes ) ) )
                {
                    ctor = constructor;
                    ctorParameterTypes = cParameterTypes;
                }
            }
            if ( ctor == null )
            {
                actualArgs = _objectArrayPool.create( args.length );
                if ( ( ctor = getConvertedConstructorAndArgs( context, target, constructors, args, actualArgs ) )
                    == null )
                {
                    throw new NoSuchMethodException();
                }
            }
            if ( !context.getMemberAccess().isAccessible( context, target, ctor, null ) )
            {
                throw new IllegalAccessException( "access denied to " + target.getName() + "()" );
            }
            return ctor.newInstance( actualArgs );
        }
        catch ( ClassNotFoundException e )
        {
            cause = e;
        }
        catch ( NoSuchMethodException e )
        {
            cause = e;
        }
        catch ( IllegalAccessException e )
        {
            cause = e;
        }
        catch ( InvocationTargetException e )
        {
            cause = e.getTargetException();
        }
        catch ( InstantiationException e )
        {
            cause = e;
        }
        finally
        {
            if ( actualArgs != args )
            {
                _objectArrayPool.recycle( actualArgs );
            }
        }

        throw new MethodFailedException( className, "new", cause );
    }

    public static Object getMethodValue( OgnlContext context, Object target, String propertyName )
        throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException
    {
        return getMethodValue( context, target, propertyName, false );
    }

    /**
     * If the checkAccessAndExistence flag is true this method will check to see if the method exists and if it is
     * accessible according to the context's MemberAccess. If neither test passes this will return NotFound.
     */
    public static Object getMethodValue( OgnlContext context, Object target, String propertyName,
                                         boolean checkAccessAndExistence )
        throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException
    {
        Object methodValue = null;
        Class<? extends Object> targetClass = target == null ? null : target.getClass();
        Method method = getGetMethod( context, targetClass, propertyName );
        if ( method == null )
        {
            method = getReadMethod( targetClass, propertyName, 0 );
        }

        if ( checkAccessAndExistence )
        {
            if ( ( method == null ) || !context.getMemberAccess().isAccessible( context, target, method, propertyName ) )
            {
                methodValue = NotFound;
            }
        }
        if ( methodValue == null )
        {
            if ( method != null )
            {
                try
                {
                    methodValue = invokeMethod( target, method, NoArguments );
                }
                catch ( InvocationTargetException ex )
                {
                    throw new OgnlException( propertyName, ex.getTargetException() );
                }
            }
            else
            {
                throw new NoSuchMethodException( propertyName );
            }
        }
        return methodValue;
    }

    public static boolean setMethodValue( OgnlContext context, Object target, String propertyName, Object value )
        throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException
    {
        return setMethodValue( context, target, propertyName, value, false );
    }

    public static boolean setMethodValue( OgnlContext context, Object target, String propertyName, Object value,
                                          boolean checkAccessAndExistence )
        throws OgnlException, IllegalAccessException, NoSuchMethodException, IntrospectionException
    {
        boolean result = true;
        Method method = getSetMethod( context, ( target == null ) ? null : target.getClass(), propertyName );

        if ( checkAccessAndExistence )
        {
            if ( ( method == null ) || !context.getMemberAccess().isAccessible( context, target, method, propertyName ) )
            {
                result = false;
            }
        }

        if ( result )
        {
            if ( method != null )
            {
                Object[] args = _objectArrayPool.create( value );

                try
                {
                    callAppropriateMethod( context, target, target, method.getName(), propertyName,
                                           Collections.nCopies( 1, method ), args );
                }
                finally
                {
                    _objectArrayPool.recycle( args );
                }
            }
            else
            {
                result = false;
            }
        }

        return result;
    }

    public static List<Constructor<?>> getConstructors( Class<?> targetClass )
        throws OgnlException
    {
        return _constructorCache.get( targetClass );
    }

    /**
     * @param targetClass
     * @param staticMethods if true (false) returns only the (non-)static methods
     * @return Returns the map of methods for a given class
     * @throws OgnlException
     */
    public static Map<String, List<Method>> getMethods( Class<?> targetClass, boolean staticMethods )
        throws OgnlException
    {
        DeclaredMethodCacheEntry.MethodType type = staticMethods ?
            DeclaredMethodCacheEntry.MethodType.STATIC :
            DeclaredMethodCacheEntry.MethodType.NON_STATIC;
        return _methodCache.get( new DeclaredMethodCacheEntry( targetClass, type ) );
    }

    public static List<Method> getMethods( Class<?> targetClass, String name, boolean staticMethods )
        throws OgnlException
    {
        return getMethods( targetClass, staticMethods ).get( name );
    }

    public static Map<String, Field> getFields( Class<?> targetClass )
        throws OgnlException
    {
        return _fieldCache.get( targetClass );
    }

    public static Field getField( Class<?> inClass, String name )
        throws OgnlException
    {
        Field field = getFields( inClass ).get( name );

        if ( field == null )
        {
            // if field is null, it should search along the superclasses
            Class<?> superClass = inClass.getSuperclass();
            while ( superClass != null )
            {
                field = getFields( superClass ).get( name );
                if ( field != null )
                {
                    return field;
                }
                superClass = superClass.getSuperclass();
            }
        }
        return field;
    }

    public static Object getFieldValue( OgnlContext context, Object target, String propertyName )
        throws NoSuchFieldException, OgnlException
    {
        return getFieldValue( context, target, propertyName, false );
    }

    public static Object getFieldValue( OgnlContext context, Object target, String propertyName,
                                        boolean checkAccessAndExistence )
        throws NoSuchFieldException, OgnlException
    {
        Object result = null;
        Class<? extends Object> targetClass = target == null ? null : target.getClass();
        Field field = getField( targetClass, propertyName );

        if ( checkAccessAndExistence )
        {
            if ( ( field == null ) || !context.getMemberAccess().isAccessible( context, target, field, propertyName ) )
            {
                result = NotFound;
            }
        }
        if ( result == null )
        {
            if ( field == null )
            {
                throw new NoSuchFieldException( propertyName );
            }
            try
            {
                Object state;

                if ( !Modifier.isStatic( field.getModifiers() ) )
                {
                    state = context.getMemberAccess().setup( context, target, field, propertyName );
                    result = field.get( target );
                    context.getMemberAccess().restore( context, target, field, propertyName, state );
                }
                else
                {
                    throw new NoSuchFieldException( propertyName );
                }

            }
            catch ( IllegalAccessException ex )
            {
                throw new NoSuchFieldException( propertyName );
            }
        }
        return result;
    }

    public static boolean setFieldValue( OgnlContext context, Object target, String propertyName, Object value )
        throws OgnlException
    {
        boolean result = false;

        try
        {
            Class<?> targetClass = target == null ? null : target.getClass();
            Field field = getField( targetClass, propertyName );
            Object state;

            if ( ( field != null ) && !Modifier.isStatic( field.getModifiers() ) )
            {
                state = context.getMemberAccess().setup( context, target, field, propertyName );
                try
                {
                    if ( isTypeCompatible( value, field.getType() ) || (
                        ( value = getConvertedType( context, target, field, propertyName, value, field.getType() ) ) != null ) )
                    {
                        field.set( target, value );
                        result = true;
                    }
                }
                finally
                {
                    context.getMemberAccess().restore( context, target, field, propertyName, state );
                }
            }
        }
        catch ( IllegalAccessException ex )
        {
            throw new NoSuchPropertyException( target, propertyName, ex );
        }
        return result;
    }

    public static boolean isFieldAccessible( OgnlContext context, Object target, Class<?> inClass, String propertyName )
        throws OgnlException
    {
        return isFieldAccessible( context, target, getField( inClass, propertyName ), propertyName );
    }

    public static boolean isFieldAccessible( OgnlContext context, Object target, Field field, String propertyName )
    {
        return context.getMemberAccess().isAccessible( context, target, field, propertyName );
    }

    public static boolean hasField( OgnlContext context, Object target, Class<?> inClass, String propertyName )
        throws OgnlException
    {
        Field field = getField( inClass, propertyName );

        return ( field != null ) && isFieldAccessible( context, target, field, propertyName );
    }

    public static Object getStaticField( OgnlContext context, String className, String fieldName )
        throws OgnlException
    {
        Exception cause;
        try
        {
            Class<?> clazz = classForName( context, className );

            if ( clazz == null )
            {
                throw new OgnlException(
                    "Unable to find class " + className + " when resolving field name of " + fieldName );
            }

            /*
             * Check for virtual static field "class"; this cannot interfere with normal static fields because it is a
             * reserved word.
             */
            if ( "class".equals( fieldName ) )
            {
                return clazz;
            }
            else if ( clazz.isEnum() )
            {
                return Enum.valueOf( (Class<? extends Enum>) clazz, fieldName );
            }
            else
            {
                Field field = clazz.getField( fieldName );
                if ( !Modifier.isStatic(field.getModifiers()) )
                {
                    throw new OgnlException( "Field " + fieldName + " of class " + className + " is not static" );
                }

                return field.get( null );
            }
        }
        catch ( ClassNotFoundException e )
        {
            cause = e;
        }
        catch ( NoSuchFieldException e )
        {
            cause = e;
        }
        catch ( SecurityException e )
        {
            cause = e;
        }
        catch ( IllegalAccessException e )
        {
            cause = e;
        }

        throw new OgnlException( "Could not get static field " + fieldName + " from class " + className, cause );
    }

    /**
     * @param targetClass
     * @param propertyName
     * @param findSets
     * @return Returns the list of (g)setter of a class for a given property name
     * @throws OgnlException
     */
    public static List<Method> getDeclaredMethods( Class<?> targetClass, String propertyName, boolean findSets )
        throws OgnlException
    {
        String baseName = Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );
        List<Method> methods = new ArrayList<Method>();
        List<String> methodNames = new ArrayList<String>( 2 );
        if ( findSets )
        {
            methodNames.add( SET_PREFIX + baseName );
        }
        else
        {
            methodNames.add( IS_PREFIX + baseName );
            methodNames.add( GET_PREFIX + baseName );
        }
        for ( String methodName : methodNames )
        {
            List<Method> methodList = _methodCache.get( new DeclaredMethodCacheEntry( targetClass ) ).get( methodName );
            if ( methodList != null )
            {
                methods.addAll( methodList );
            }
        }

        return methods;
    }

    /**
     * Convenience used to check if a method is volatile or synthetic so as to avoid calling un-callable methods.
     *
     * @param method The method to check.
     * @return True if the method should be callable, false otherwise.
     */
    //TODO: the method was intented as private, so it'd need to move in a util class
    public static boolean isMethodCallable( Method method )
    {
        return !( method.isSynthetic() || Modifier.isVolatile( method.getModifiers() ) );

    }

    public static Method getGetMethod( OgnlContext unused, Class<?> targetClass, String propertyName )
        throws IntrospectionException, OgnlException
    {
        Method result = null;

        List<Method> methods = getDeclaredMethods( targetClass, propertyName, false /* find 'get' methods */ );

        if ( methods != null )
        {
            for ( Method method : methods )
            {
                Class<?>[] mParameterTypes = findParameterTypes( targetClass, method ); // getParameterTypes(method);

                if ( mParameterTypes.length == 0 )
                {
                    result = method;
                    break;
                }
            }
        }

        return result;
    }

    public static boolean isMethodAccessible( OgnlContext context, Object target, Method method, String propertyName )
    {
        return ( method != null ) && context.getMemberAccess().isAccessible( context, target, method, propertyName );
    }

    public static boolean hasGetMethod( OgnlContext context, Object target, Class<?> targetClass, String propertyName )
        throws IntrospectionException, OgnlException
    {
        return isMethodAccessible( context, target, getGetMethod( context, targetClass, propertyName ), propertyName );
    }

    public static Method getSetMethod( OgnlContext context, Class<?> targetClass, String propertyName )
        throws IntrospectionException, OgnlException
    {
        Method setMethod = null;

        List<Method> methods = getDeclaredMethods( targetClass, propertyName, true /* find 'set' methods */ );

        if ( methods != null )
        {
            for ( Method method : methods )
            {
                Class<?>[] mParameterTypes = findParameterTypes( targetClass, method ); // getParameterTypes(method);

                if ( mParameterTypes.length == 1 )
                {
                    setMethod = method;
                    break;
                }
            }
        }

        return setMethod;
    }

    public static boolean hasSetMethod( OgnlContext context, Object target, Class<?> targetClass, String propertyName )
        throws IntrospectionException, OgnlException
    {
        return isMethodAccessible( context, target, getSetMethod( context, targetClass, propertyName ), propertyName );
    }

    public static boolean hasGetProperty( OgnlContext context, Object target, Object oname )
        throws IntrospectionException, OgnlException
    {
        Class<?> targetClass = ( target == null ) ? null : target.getClass();
        String name = oname.toString();

        return hasGetMethod( context, target, targetClass, name ) || hasField( context, target, targetClass, name );
    }

    public static boolean hasSetProperty( OgnlContext context, Object target, Object oname )
        throws IntrospectionException, OgnlException
    {
        Class<?> targetClass = ( target == null ) ? null : target.getClass();
        String name = oname.toString();

        return hasSetMethod( context, target, targetClass, name ) || hasField( context, target, targetClass, name );
    }

    private static boolean indexMethodCheck( List<Method> methods )
        throws CacheException
    {
        boolean result = false;

        if ( methods.size() > 0 )
        {
            Method method = methods.get( 0 );
            Class<?>[] parameterTypes = getParameterTypes( method );
            int numParameterTypes = parameterTypes.length;
            Class<?> lastMethodClass = method.getDeclaringClass();

            result = true;
            for ( int i = 1; result && ( i < methods.size() ); i++ )
            {
                Class<?> clazz = methods.get( i ).getDeclaringClass();

                // Check to see if more than one method implemented per class
                if ( lastMethodClass == clazz )
                {
                    result = false;
                }
                else
                {
                    Class<?>[] mpt = getParameterTypes( method );
                    int mpc = parameterTypes.length;

                    if ( numParameterTypes != mpc )
                    {
                        result = false;
                    }
                    for ( int j = 0; j < numParameterTypes; j++ )
                    {
                        if ( parameterTypes[j] != mpt[j] )
                        {
                            result = false;
                            break;
                        }
                    }
                }
                lastMethodClass = clazz;
            }
        }
        return result;
    }

    //TODO: again, this method was local
    public static void findObjectIndexedPropertyDescriptors( Class<?> targetClass,
                                                             Map<String, PropertyDescriptor> intoMap )
        throws OgnlException
    {
        Map<String, List<Method>> allMethods = getMethods( targetClass, false );
        Map<String, List<Method>> pairs = new HashMap<String, List<Method>>( 101 );

        for ( Map.Entry<String, List<Method>> entry : allMethods.entrySet() )
        {
            String methodName = entry.getKey();
            List<Method> methods = entry.getValue();

            /*
             * Only process set/get where there is exactly one implementation of the method per class and those
             * implementations are all the same
             */
            if ( indexMethodCheck( methods ) )
            {
                boolean isGet = false, isSet;
                Method method = methods.get( 0 );

                if ( ( ( isSet = methodName.startsWith( SET_PREFIX ) ) || ( isGet =
                    methodName.startsWith( GET_PREFIX ) ) ) && ( methodName.length() > 3 ) )
                {
                    String propertyName = Introspector.decapitalize( methodName.substring( 3 ) );
                    Class<?>[] parameterTypes = getParameterTypes( method );
                    int parameterCount = parameterTypes.length;

                    if ( isGet && ( parameterCount == 1 ) && ( method.getReturnType() != Void.TYPE ) )
                    {
                        List<Method> pair = pairs.get( propertyName );

                        if ( pair == null )
                        {
                            pairs.put( propertyName, pair = new ArrayList<Method>() );
                        }
                        pair.add( method );
                    }
                    if ( isSet && ( parameterCount == 2 ) && ( method.getReturnType() == Void.TYPE ) )
                    {
                        List<Method> pair = pairs.get( propertyName );

                        if ( pair == null )
                        {
                            pairs.put( propertyName, pair = new ArrayList<Method>() );
                        }
                        pair.add( method );
                    }
                }
            }
        }

        for ( String propertyName : pairs.keySet() )
        {
            List<Method> methods = pairs.get( propertyName );

            if ( methods.size() == 2 )
            {
                Method method1 = methods.get( 0 ), method2 = methods.get( 1 ), setMethod =
                    ( method1.getParameterTypes().length == 2 ) ? method1 : method2, getMethod =
                    ( setMethod == method1 ) ? method2 : method1;
                Class<?> keyType = getMethod.getParameterTypes()[0], propertyType = getMethod.getReturnType();

                if ( keyType == setMethod.getParameterTypes()[0] )
                {
                    if ( propertyType == setMethod.getParameterTypes()[1] )
                    {
                        ObjectIndexedPropertyDescriptor propertyDescriptor;

                        try
                        {
                            propertyDescriptor =
                                new ObjectIndexedPropertyDescriptor( propertyName, propertyType, getMethod, setMethod );
                        }
                        catch ( Exception ex )
                        {
                            throw new OgnlException(
                                "creating object indexed property descriptor for '" + propertyName + "' in "
                                    + targetClass, ex );
                        }
                        intoMap.put( propertyName, propertyDescriptor );
                    }
                }

            }
        }
    }

    /**
     * This method returns the property descriptors for the given class as a Map.
     *
     * @param targetClass The class to get the descriptors for.
     * @return Map map of property descriptors for class.
     * @throws IntrospectionException on errors using {@link Introspector}.
     * @throws OgnlException          On general errors.
     */
    public static Map<String, PropertyDescriptor> getPropertyDescriptors( Class<?> targetClass )
        throws IntrospectionException, OgnlException
    {
        return _propertyDescriptorCache.get( targetClass );
    }

    /**
     * This method returns a PropertyDescriptor for the given class and property name using a Map lookup (using
     * getPropertyDescriptorsMap()).
     * @param targetClass a target class.
     * @param propertyName a property name.
     * @return the PropertyDescriptor for the given targetClass and propertyName.
     * @throws java.beans.IntrospectionException
     * @throws OgnlException
     */
    public static PropertyDescriptor getPropertyDescriptor( Class<?> targetClass, String propertyName )
        throws IntrospectionException, OgnlException
    {
        if ( targetClass == null )
        {
            return null;
        }

        return getPropertyDescriptors( targetClass ).get( propertyName );
    }

    public static PropertyDescriptor[] getPropertyDescriptorsArray( Class<?> targetClass )
        throws IntrospectionException, OgnlException
    {
        Collection<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors( targetClass ).values();
        return propertyDescriptors.toArray( new PropertyDescriptor[propertyDescriptors.size()] );
    }

    /**
     * Gets the property descriptor with the given name for the target class given.
     *
     * @param targetClass Class for which property descriptor is desired
     * @param name        Name of property
     * @return PropertyDescriptor of the named property or null if the class has no property with the given name
     * @throws java.beans.IntrospectionException
     * @throws OgnlException
     */
    public static PropertyDescriptor getPropertyDescriptorFromArray( Class<?> targetClass, String name )
        throws IntrospectionException, OgnlException
    {
        PropertyDescriptor result = null;
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptorsArray( targetClass );

        for ( int i = 0, icount = propertyDescriptors.length; ( result == null ) && ( i < icount ); i++ )
        {
            if ( propertyDescriptors[i].getName().compareTo( name ) == 0 )
            {
                result = propertyDescriptors[i];
            }
        }
        return result;
    }

    public static void setMethodAccessor( Class<?> cls, MethodAccessor accessor )
    {
        _methodAccessors.put( cls, accessor );
    }

    public static MethodAccessor getMethodAccessor( Class<?> cls )
        throws OgnlException
    {
        MethodAccessor answer = ClassCacheHandler.getHandler( cls, _methodAccessors );
        if ( answer != null )
        {
            return answer;
        }
        throw new OgnlException( "No method accessor for " + cls );
    }

    public static void setPropertyAccessor( Class<?> cls, PropertyAccessor accessor )
    {
        _propertyAccessors.put( cls, accessor );
    }

    public static PropertyAccessor getPropertyAccessor( Class<?> cls )
        throws OgnlException
    {
        PropertyAccessor answer = ClassCacheHandler.getHandler( cls, _propertyAccessors );
        if ( answer != null )
        {
            return answer;
        }

        throw new OgnlException( "No property accessor for class " + cls );
    }

    public static ElementsAccessor getElementsAccessor( Class<?> cls )
        throws OgnlException
    {
        ElementsAccessor answer = ClassCacheHandler.getHandler( cls, _elementsAccessors );
        if ( answer != null )
        {
            return answer;
        }
        throw new OgnlException( "No elements accessor for class " + cls );
    }

    public static void setElementsAccessor( Class<?> cls, ElementsAccessor accessor )
    {
        _elementsAccessors.put( cls, accessor );
    }

    public static NullHandler getNullHandler( Class<?> cls )
        throws OgnlException
    {
        NullHandler answer = ClassCacheHandler.getHandler( cls, _nullHandlers );
        if ( answer != null )
        {
            return answer;
        }
        throw new OgnlException( "No null handler for class " + cls );
    }

    public static void setNullHandler( Class<?> cls, NullHandler handler )
    {
        _nullHandlers.put( cls, handler );
    }

    public static Object getProperty( OgnlContext context, Object source, Object name )
        throws OgnlException
    {
        PropertyAccessor accessor;

        if ( source == null )
        {
            throw new OgnlException( "source is null for getProperty(null, \"" + name + "\")" );
        }
        if ( ( accessor = getPropertyAccessor( getTargetClass( source ) ) ) == null )
        {
            throw new OgnlException( "No property accessor for " + getTargetClass( source ).getName() );
        }

        return accessor.getProperty( context, source, name );
    }

    public static void setProperty( OgnlContext context, Object target, Object name, Object value )
        throws OgnlException
    {
        PropertyAccessor accessor;

        if ( target == null )
        {
            throw new OgnlException( "target is null for setProperty(null, \"" + name + "\", " + value + ")" );
        }
        if ( ( accessor = getPropertyAccessor( getTargetClass( target ) ) ) == null )
        {
            throw new OgnlException( "No property accessor for " + getTargetClass( target ).getName() );
        }

        accessor.setProperty( context, target, name, value );
    }

    /**
     * Determines the index property type, if any. Returns <code>INDEXED_PROPERTY_NONE</code> if the property is not
     * index-accessible as determined by OGNL or JavaBeans. If it is indexable then this will return whether it is a
     * JavaBeans indexed property, conforming to the indexed property patterns (returns
     * <code>INDEXED_PROPERTY_INT</code>) or if it conforms to the OGNL arbitrary object indexable (returns
     * <code>INDEXED_PROPERTY_OBJECT</code>).
     */
    public static int getIndexedPropertyType( OgnlContext context, Class<?> sourceClass, String name )
        throws OgnlException
    {
        int result = INDEXED_PROPERTY_NONE;

        try
        {
            PropertyDescriptor propertyDescriptor = getPropertyDescriptor( sourceClass, name );
            if ( propertyDescriptor != null )
            {
                if ( propertyDescriptor instanceof IndexedPropertyDescriptor )
                {
                    result = INDEXED_PROPERTY_INT;
                }
                else
                {
                    if ( propertyDescriptor instanceof ObjectIndexedPropertyDescriptor )
                    {
                        result = INDEXED_PROPERTY_OBJECT;
                    }
                }
            }
        }
        catch ( Exception ex )
        {
            throw new OgnlException( "problem determining if '" + name + "' is an indexed property", ex );
        }
        return result;
    }

    public static Object getIndexedProperty( OgnlContext context, Object source, String name, Object index )
        throws OgnlException
    {
        Object[] args = _objectArrayPool.create( index );

        try
        {
            PropertyDescriptor propertyDescriptor = getPropertyDescriptor( ( source == null ) ? null : source.getClass(), name );
            Method method;

            if ( propertyDescriptor instanceof IndexedPropertyDescriptor )
            {
                method = ( (IndexedPropertyDescriptor) propertyDescriptor ).getIndexedReadMethod();
            }
            else
            {
                if ( propertyDescriptor instanceof ObjectIndexedPropertyDescriptor )
                {
                    method = ( (ObjectIndexedPropertyDescriptor) propertyDescriptor ).getIndexedReadMethod();
                }
                else
                {
                    throw new OgnlException( "property '" + name + "' is not an indexed property" );
                }
            }

            return callMethod( context, source, method.getName(), args );

        }
        catch ( OgnlException ex )
        {
            throw ex;
        }
        catch ( Exception ex )
        {
            throw new OgnlException( "getting indexed property descriptor for '" + name + "'", ex );
        }
        finally
        {
            _objectArrayPool.recycle( args );
        }
    }

    public static void setIndexedProperty( OgnlContext context, Object source, String name, Object index, Object value )
        throws OgnlException
    {
        Object[] args = _objectArrayPool.create( index, value );

        try
        {
            PropertyDescriptor propertyDescriptor = getPropertyDescriptor( ( source == null ) ? null : source.getClass(), name );
            Method method;

            if ( propertyDescriptor instanceof IndexedPropertyDescriptor )
            {
                method = ( (IndexedPropertyDescriptor) propertyDescriptor ).getIndexedWriteMethod();
            }
            else
            {
                if ( propertyDescriptor instanceof ObjectIndexedPropertyDescriptor )
                {
                    method = ( (ObjectIndexedPropertyDescriptor) propertyDescriptor ).getIndexedWriteMethod();
                }
                else
                {
                    throw new OgnlException( "property '" + name + "' is not an indexed property" );
                }
            }

            callMethod( context, source, method.getName(), args );

        }
        catch ( OgnlException ex )
        {
            throw ex;
        }
        catch ( Exception ex )
        {
            throw new OgnlException( "getting indexed property descriptor for '" + name + "'", ex );
        }
        finally
        {
            _objectArrayPool.recycle( args );
        }
    }

    public static EvaluationPool getEvaluationPool()
    {
        return _evaluationPool;
    }

    public static ObjectArrayPool getObjectArrayPool()
    {
        return _objectArrayPool;
    }

    /**
     * Registers the specified {@link ClassCacheInspector} with all class reflection based internal caches. This may
     * have a significant performance impact so be careful using this in production scenarios.
     *
     * @param inspector The inspector instance that will be registered with all internal cache instances.
     */
    public static void setClassCacheInspector( ClassCacheInspector inspector )
    {
        _cacheInspector = inspector;

        _propertyDescriptorCache.setClassInspector( _cacheInspector );
        _constructorCache.setClassInspector( _cacheInspector );
        //TODO: methodCache and invokePC should allow to use classCacheInsecptor
//        _methodCache.setClassInspector( _cacheInspector );
//        _invokePermissionCache.setClassInspector( _cacheInspector );
        _fieldCache.setClassInspector( _cacheInspector );
    }

    public static Method getMethod( OgnlContext context, Class<?> target, String name, Node[] children,
                                    boolean includeStatic )
        throws Exception
    {
        Class<?>[] parms;
        if ( children != null && children.length > 0 )
        {
            parms = new Class[children.length];

            // used to reset context after loop
            Class<?> currType = context.getCurrentType();
            Class<?> currAccessor = context.getCurrentAccessor();
            Object cast = context.get( ExpressionCompiler.PRE_CAST );

            context.setCurrentObject( context.getRoot() );
            context.setCurrentType( context.getRoot() != null ? context.getRoot().getClass() : null );
            context.setCurrentAccessor( null );
            context.setPreviousType( null );

            for ( int i = 0; i < children.length; i++ )
            {
                children[i].toGetSourceString( context, context.getRoot() );
                parms[i] = context.getCurrentType();
            }

            context.put( ExpressionCompiler.PRE_CAST, cast );

            context.setCurrentType( currType );
            context.setCurrentAccessor( currAccessor );
            context.setCurrentObject( target );
        }
        else
        {
            parms = new Class[0];
        }

        List<Method> methods = OgnlRuntime.getMethods( target, name, includeStatic );
        if ( methods == null )
        {
            return null;
        }

        for ( Method method : methods )
        {
            boolean varArgs = method.isVarArgs();

            if ( parms.length != method.getParameterTypes().length && !varArgs )
            {
                continue;
            }

            Class<?>[] methodParameterTypes = method.getParameterTypes();
            boolean matched = true;
            for ( int i = 0; i < methodParameterTypes.length; i++ )
            {
                Class<?> methodParameterType = methodParameterTypes[i];
                if ( varArgs && methodParameterType.isArray() )
                {
                    continue;
                }

                Class<?> parm = parms[i];
                if ( parm == null )
                {
                    matched = false;
                    break;
                }

                if ( parm == methodParameterType)
                {
                    continue;
                }

                if ( methodParameterType.isPrimitive() && Character.TYPE != methodParameterType && Byte.TYPE != methodParameterType
                    && Number.class.isAssignableFrom(parm)
                    && OgnlRuntime.getPrimitiveWrapperClass(parm) == methodParameterType)
                {
                    continue;
                }

                matched = false;
                break;
            }

            if ( matched )
            {
                return method;
            }
        }

        return null;
    }

    /**
     * Finds the best possible match for a method on the specified target class with a matching name.
     * <p>
     * The name matched will also try different combinations like <code>is + name, has + name, get + name, etc..</code>
     * </p>
     *
     * @param target The class to find a matching method against.
     * @param name   The name of the method.
     * @return The most likely matching {@link Method}, or null if none could be found.
     */
    public static Method getReadMethod( Class<?> target, String name )
    {
        return getReadMethod( target, name, -1 );
    }

    public static Method getReadMethod( Class<?> target, String name, int numParms )
    {
        try
        {
            name = name.replaceAll( "\"", "" ).toLowerCase();

            BeanInfo info = Introspector.getBeanInfo( target );
            MethodDescriptor[] methodDescriptors = info.getMethodDescriptors();

            // exact matches first

            Method method = null;

            for ( MethodDescriptor methodDescriptor : methodDescriptors )
            {
                if ( !isMethodCallable( methodDescriptor.getMethod() ) )
                {
                    continue;
                }

                String methodName = methodDescriptor.getName();
                String lowerMethodName = methodName.toLowerCase();
                int methodParamLen = methodDescriptor.getMethod().getParameterTypes().length;

                if ( ( methodName.equalsIgnoreCase( name ) || lowerMethodName.equals( "get" + name )
                    || lowerMethodName.equals( "has" + name ) || lowerMethodName.equals( "is" + name ) )
                    && !methodName.startsWith( "set" ) )
                {
                    if ( numParms > 0 && methodParamLen == numParms )
                    {
                        return methodDescriptor.getMethod();
                    }
                    else if ( numParms < 0 )
                    {
                        if ( methodName.equals( name ) )
                        {
                            return methodDescriptor.getMethod();
                        }
                        else if ( method == null || ( method.getParameterTypes().length > methodParamLen ) )
                        {
                            method = methodDescriptor.getMethod();
                        }
                    }
                }
            }

            if ( method != null )
            {
                return method;
            }

            for ( MethodDescriptor methodDescriptor : methodDescriptors )
            {
                if ( !isMethodCallable( methodDescriptor.getMethod() ) )
                {
                    continue;
                }

                if ( methodDescriptor.getName().toLowerCase().endsWith( name ) && !methodDescriptor.getName().startsWith( "set" )
                    && methodDescriptor.getMethod().getReturnType() != Void.TYPE )
                {

                    if ( numParms > 0 && methodDescriptor.getMethod().getParameterTypes().length == numParms )
                    {
                        return methodDescriptor.getMethod();
                    }
                    else if ( numParms < 0 )
                    {
                        if ( ( method != null
                            && method.getParameterTypes().length > methodDescriptor.getMethod().getParameterTypes().length )
                            || method == null )
                        {
                            method = methodDescriptor.getMethod();
                        }
                    }
                }
            }

            if ( method != null )
            {
                return method;
            }

            // try one last time adding a get to beginning

            if ( !name.startsWith( "get" ) )
            {
                return OgnlRuntime.getReadMethod( target, "get" + name, numParms );
            }

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return null;
    }

    public static Method getWriteMethod( Class<?> target, String name )
    {
        return getWriteMethod( target, name, -1 );
    }

    public static Method getWriteMethod( Class<?> target, String name, int numParms )
    {
        try
        {
            name = name.replaceAll( "\"", "" );

            BeanInfo info = Introspector.getBeanInfo( target );
            MethodDescriptor[] methods = info.getMethodDescriptors();

            for ( MethodDescriptor method : methods )
            {
                if ( !isMethodCallable( method.getMethod() ) )
                {
                    continue;
                }

                if ( ( method.getName().equalsIgnoreCase( name ) || method.getName().toLowerCase().equals(
                    name.toLowerCase() ) || method.getName().toLowerCase().equals( "set" + name.toLowerCase() ) )
                    && !method.getName().startsWith( "get" ) )
                {

                    if ( numParms > 0 && method.getMethod().getParameterTypes().length == numParms )
                    {
                        return method.getMethod();
                    }
                    else if ( numParms < 0 )
                    {
                        return method.getMethod();
                    }
                }
            }

            // try again on pure class

            Method[] cmethods = target.getClass().getMethods();
            for ( Method cmethod : cmethods )
            {
                if ( !isMethodCallable( cmethod ) )
                {
                    continue;
                }

                if ( ( cmethod.getName().equalsIgnoreCase( name ) || cmethod.getName().toLowerCase().equals(
                    name.toLowerCase() ) || cmethod.getName().toLowerCase().equals( "set" + name.toLowerCase() ) )
                    && !cmethod.getName().startsWith( "get" ) )
                {

                    if ( numParms > 0 && cmethod.getParameterTypes().length == numParms )
                    {
                        return cmethod;
                    }
                    else if ( numParms < 0 )
                    {
                        return cmethod;
                    }
                }
            }

            // try one last time adding a set to beginning

            if ( !name.startsWith( "set" ) )
            {
                return OgnlRuntime.getReadMethod( target, "set" + name, numParms );
            }

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return null;
    }

    public static PropertyDescriptor getProperty( Class<?> target, String name )
    {
        try
        {
            BeanInfo info = Introspector.getBeanInfo( target );

            PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();

            for ( PropertyDescriptor propertyDescriptor : propertyDescriptors )
            {

                String propertyDescriptorName = propertyDescriptor.getName();
                if ( propertyDescriptorName.equalsIgnoreCase( name ) || propertyDescriptorName.toLowerCase().equals( name.toLowerCase() )
                    || propertyDescriptorName.toLowerCase().endsWith( name.toLowerCase() ) )
                {
                    return propertyDescriptor;
                }
            }

        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        return null;
    }

    public static boolean isBoolean( String expression )
    {
        if ( expression == null )
        {
            return false;
        }

        return "true".equals( expression ) || "false".equals( expression ) || "!true".equals( expression )
            || "!false".equals( expression ) || "(true)".equals( expression ) || "!(true)".equals( expression )
            || "(false)".equals( expression ) || "!(false)".equals( expression ) || expression.startsWith(
            "org.apache.commons.ognl.OgnlOps" );

    }

    /**
     * Compares the {@link OgnlContext#getCurrentType()} and {@link OgnlContext#getPreviousType()} class types on the
     * stack to determine if a numeric expression should force object conversion.
     * <p/>
     * <p/>
     * Normally used in conjunction with the <code>forceConversion</code> parameter of
     * {@link OgnlRuntime#getChildSource(OgnlContext, Object, Node, boolean)}.
     * </p>
     *
     * @param context The current context.
     * @return True, if the class types on the stack wouldn't be comparable in a pure numeric expression such as
     *         <code>o1 >= o2</code>.
     */
    public static boolean shouldConvertNumericTypes( OgnlContext context )
    {
        if ( context.getCurrentType() == null || context.getPreviousType() == null )
        {
            return true;
        }

        if ( context.getCurrentType() == context.getPreviousType() && context.getCurrentType().isPrimitive()
            && context.getPreviousType().isPrimitive() )
        {
            return false;
        }

        return context.getCurrentType() != null && !context.getCurrentType().isArray()
            && context.getPreviousType() != null && !context.getPreviousType().isArray();
    }

    /**
     * Attempts to get the java source string represented by the specific child expression via the
     * {@link JavaSource#toGetSourceString(OgnlContext, Object)} interface method.
     *
     * @param context The ognl context to pass to the child.
     * @param target  The current object target to use.
     * @param child   The child expression.
     * @return The result of calling {@link JavaSource#toGetSourceString(OgnlContext, Object)} plus additional enclosures
     *         of {@link OgnlOps#convertValue(Object, Class, boolean)} for conversions.
     * @throws OgnlException Mandatory exception throwing catching.. (blehh)
     */
    public static String getChildSource( OgnlContext context, Object target, Node child )
        throws OgnlException
    {
        return getChildSource( context, target, child, false );
    }

    /**
     * Attempts to get the java source string represented by the specific child expression via the
     * {@link JavaSource#toGetSourceString(OgnlContext, Object)} interface method.
     *
     * @param context         The ognl context to pass to the child.
     * @param target          The current object target to use.
     * @param child           The child expression.
     * @param forceConversion If true, forces {@link OgnlOps#convertValue(Object, Class)} conversions on the objects.
     * @return The result of calling {@link JavaSource#toGetSourceString(OgnlContext, Object)} plus additional enclosures
     *         of {@link OgnlOps#convertValue(Object, Class, boolean)} for conversions.
     * @throws OgnlException Mandatory exception throwing catching.. (blehh)
     */
    public static String getChildSource( OgnlContext context, Object target, Node child, boolean forceConversion )
        throws OgnlException
    {
        String pre = (String) context.get( "_currentChain" );
        if ( pre == null )
        {
            pre = "";
        }

        try
        {
            child.getValue( context, target );
        }
        catch ( NullPointerException e )
        {
            // ignore
        }
        catch ( ArithmeticException e )
        {
            context.setCurrentType( int.class );
            return "0";
        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        String source;

        try
        {
            source = child.toGetSourceString( context, target );
        }
        catch ( Throwable t )
        {
            throw OgnlOps.castToRuntime( t );
        }

        // handle root / method expressions that may not have proper root java source access

        if ( !ASTConst.class.isInstance( child ) && ( target == null || context.getRoot() != target ) )
        {
            source = pre + source;
        }

        if ( context.getRoot() != null )
        {
            source = ExpressionCompiler.getRootExpression( child, context.getRoot(), context ) + source;
            context.setCurrentAccessor( context.getRoot().getClass() );
        }

        if ( ASTChain.class.isInstance( child ) )
        {
            String cast = (String) context.remove( ExpressionCompiler.PRE_CAST );
            if ( cast == null )
            {
                cast = "";
            }

            source = cast + source;
        }

        if ( source == null || source.trim().length() < 1 )
        {
            source = "null";
        }

        return source;
    }
}
