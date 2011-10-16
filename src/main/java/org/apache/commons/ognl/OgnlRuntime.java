/*
 * $Id$
 *
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

package org.apache.commons.ognl;

import org.apache.commons.ognl.enhance.ExpressionCompiler;
import org.apache.commons.ognl.enhance.OgnlExpressionCompiler;
import org.apache.commons.ognl.internal.Cache;
import org.apache.commons.ognl.internal.CacheException;
import org.apache.commons.ognl.internal.ClassCache;
import org.apache.commons.ognl.internal.ClassCacheHandler;
import org.apache.commons.ognl.internal.ConcurrentClassCache;
import org.apache.commons.ognl.internal.ConcurrentHashMapCache;
import org.apache.commons.ognl.internal.entry.CacheEntryFactory;
import org.apache.commons.ognl.internal.entry.DeclaredMethodCacheEntry;
import org.apache.commons.ognl.internal.entry.DeclaredMethodCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.FiedlCacheEntryFactory;
import org.apache.commons.ognl.internal.entry.GenericMethodParameterTypeCacheEntry;
import org.apache.commons.ognl.internal.entry.GenericMethodParameterTypeFactory;
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
import java.lang.reflect.Type;
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
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class OgnlRuntime
{

    /**
     * Constant expression used to indicate that a given method / property couldn't be found during reflection
     * operations.
     */
    public static final Object NotFound = new Object( );

    public static final Object[] NoArguments = new Object[]{ };

    public static final Class<?>[] NoArgumentTypes = new Class<?>[]{ };

    /**
     * Token returned by TypeConverter for no conversion possible
     */
    public static final Object NoConversionPossible = "ognl.NoConversionPossible";

    /**
     * Not an indexed property
     */
    public static int INDEXED_PROPERTY_NONE = 0;

    /**
     * JavaBeans IndexedProperty
     */
    public static int INDEXED_PROPERTY_INT = 1;

    /**
     * OGNL ObjectIndexedProperty
     */
    public static int INDEXED_PROPERTY_OBJECT = 2;

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
    private static final Map<Integer, String> HEX_PADDING = new HashMap<Integer, String>( );

    private static final int HEX_LENGTH = 8;

    /**
     * Returned by <CODE>getUniqueDescriptor()</CODE> when the object is <CODE>null</CODE>.
     */
    private static final String NULL_OBJECT_STRING = "<null>";

    /**
     * Used to store the result of determining if current jvm is 1.5 language compatible.
     */
    private static boolean _jdk15 = false;

    private static boolean _jdkChecked = false;

    static final ClassCache<MethodAccessor> _methodAccessors = new ConcurrentClassCache<MethodAccessor>( );

    static final ClassCache<PropertyAccessor> _propertyAccessors = new ConcurrentClassCache<PropertyAccessor>( );

    static final ClassCache<ElementsAccessor> _elementsAccessors = new ConcurrentClassCache<ElementsAccessor>( );

    static final ClassCache<NullHandler> _nullHandlers = new ConcurrentClassCache<NullHandler>( );

    static final ClassCache<Map<String, PropertyDescriptor>> _propertyDescriptorCache =
        new ConcurrentClassCache<Map<String, PropertyDescriptor>>(new PropertyDescriptorCacheEntryFactory());

    static final ClassCache<List<Constructor<?>>> _constructorCache = new ConcurrentClassCache<List<Constructor<?>>>(new CacheEntryFactory<Class<?>, List<Constructor<?>>>( )
    {
        public List<Constructor<?>> create( Class<?> key )
            throws CacheException
        {
            return Arrays.asList( key.getConstructors( ) );
        }
    } );

    static final ConcurrentHashMapCache<DeclaredMethodCacheEntry, Map<String, List<Method>>> _methodCache =
        new ConcurrentHashMapCache<DeclaredMethodCacheEntry, Map<String, List<Method>>>(
            new DeclaredMethodCacheEntryFactory( ) );

    static final ConcurrentHashMapCache<PermissionCacheEntry, Permission> _invokePermissionCache =
        new ConcurrentHashMapCache<PermissionCacheEntry, Permission>( new PermissionCacheEntryFactory( ) );

    static final ClassCache<Map<String, Field>> _fieldCache =
        new ConcurrentClassCache<Map<String, Field>>( new FiedlCacheEntryFactory( ) );

    static final Map<String, Class<?>> _primitiveTypes = new HashMap<String, Class<?>>( 101 );

    static final ClassCache _primitiveDefaults = new ConcurrentClassCache( );

    static final Cache<Method, Class<?>[]> _methodParameterTypesCache = new ConcurrentHashMapCache<Method, Class<?>[]>( new CacheEntryFactory<Method, Class<?>[]>( )
    {
        public Class<?>[] create( Method key )
            throws CacheException
        {
            return key.getParameterTypes( );
        }
    } );

    static final Cache<GenericMethodParameterTypeCacheEntry, Class<?>[]> _genericMethodParameterTypesCache = new ConcurrentHashMapCache<GenericMethodParameterTypeCacheEntry, Class<?>[]>( new GenericMethodParameterTypeFactory( ) );;

    static final Map<Constructor<?>, Class<?>[]> _ctorParameterTypesCache =
        new HashMap<Constructor<?>, Class<?>[]>( 101 );

    static SecurityManager _securityManager = System.getSecurityManager( );

    static final EvaluationPool _evaluationPool = new EvaluationPool( );

    static final ObjectArrayPool _objectArrayPool = new ObjectArrayPool( );

    static final IntHashMap<Integer, Boolean> _methodAccessCache = new IntHashMap<Integer, Boolean>( );

    static final IntHashMap<Integer, Boolean> _methodPermCache = new IntHashMap<Integer, Boolean>( );

    static ClassCacheInspector _cacheInspector;

    /**
     * Expression compiler used by {@link Ognl#compileExpression(OgnlContext, Object, String)} calls.
     */
    private static OgnlExpressionCompiler _compiler;

    /**
     * Lazy loading of Javassist library
     */
    static
    {
        try
        {
            Class.forName( "javassist.ClassPool" );
            _compiler = new ExpressionCompiler( );
        }
        catch ( ClassNotFoundException e )
        {
            throw new IllegalArgumentException(
                "Javassist library is missing in classpath! Please add missed dependency!", e );
        }
    }

    private static Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_CLASSES = new IdentityHashMap<Class<?>, Class<?>>( );

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
        new HashMap<Class<? extends Number>, String>( );

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

    private static final Map<Class<?>, String> NUMERIC_VALUES = new HashMap<Class<?>, String>( );

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
        new HashMap<Class<? extends Number>, String>( );

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

    private static final Map<Class<?>, Object> NUMERIC_DEFAULTS = new HashMap<Class<?>, Object>( );

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
        PropertyAccessor p = new ArrayPropertyAccessor( );

        setPropertyAccessor( Object.class, new ObjectPropertyAccessor( ) );
        setPropertyAccessor( byte[].class, p );
        setPropertyAccessor( short[].class, p );
        setPropertyAccessor( char[].class, p );
        setPropertyAccessor( int[].class, p );
        setPropertyAccessor( long[].class, p );
        setPropertyAccessor( float[].class, p );
        setPropertyAccessor( double[].class, p );
        setPropertyAccessor( Object[].class, p );
        setPropertyAccessor( List.class, new ListPropertyAccessor( ) );
        setPropertyAccessor( Map.class, new MapPropertyAccessor( ) );
        setPropertyAccessor( Set.class, new SetPropertyAccessor( ) );
        setPropertyAccessor( Iterator.class, new IteratorPropertyAccessor( ) );
        setPropertyAccessor( Enumeration.class, new EnumerationPropertyAccessor( ) );

        ElementsAccessor e = new ArrayElementsAccessor( );

        setElementsAccessor( Object.class, new ObjectElementsAccessor( ) );
        setElementsAccessor( byte[].class, e );
        setElementsAccessor( short[].class, e );
        setElementsAccessor( char[].class, e );
        setElementsAccessor( int[].class, e );
        setElementsAccessor( long[].class, e );
        setElementsAccessor( float[].class, e );
        setElementsAccessor( double[].class, e );
        setElementsAccessor( Object[].class, e );
        setElementsAccessor( Collection.class, new CollectionElementsAccessor( ) );
        setElementsAccessor( Map.class, new MapElementsAccessor( ) );
        setElementsAccessor( Iterator.class, new IteratorElementsAccessor( ) );
        setElementsAccessor( Enumeration.class, new EnumerationElementsAccessor( ) );
        setElementsAccessor( Number.class, new NumberElementsAccessor( ) );

        NullHandler nh = new ObjectNullHandler( );

        setNullHandler( Object.class, nh );
        setNullHandler( byte[].class, nh );
        setNullHandler( short[].class, nh );
        setNullHandler( char[].class, nh );
        setNullHandler( int[].class, nh );
        setNullHandler( long[].class, nh );
        setNullHandler( float[].class, nh );
        setNullHandler( double[].class, nh );
        setNullHandler( Object[].class, nh );

        MethodAccessor ma = new ObjectMethodAccessor( );

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
    public static void clearCache( )
    {
        _methodParameterTypesCache.clear( );
        _ctorParameterTypesCache.clear( );
        _propertyDescriptorCache.clear( );
        _constructorCache.clear( );
        _methodCache.clear( );
        _invokePermissionCache.clear( );
        _fieldCache.clear( );
//        _setterMethods.clear( );
//        _getterMethods.clear( );
        _methodAccessCache.clear( );
    }

    /**
     * Checks if the current jvm is java language >= 1.5 compatible.
     *
     * @return True if jdk15 features are present.
     */
    public static boolean isJdk15( )
    {
        if ( _jdkChecked )
        {
            return _jdk15;
        }

        try
        {
            Class.forName( "java.lang.annotation.Annotation" );
            _jdk15 = true;
        }
        catch ( Exception e )
        { /* ignore */
        }

        _jdkChecked = true;

        return _jdk15;
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

    public static OgnlExpressionCompiler getCompiler( )
    {
        return _compiler;
    }

    public static void compileExpression( OgnlContext context, Node expression, Object root )
        throws Exception
    {
        _compiler.compileExpression( context, expression, root );
    }

    /**
     * Gets the "target" class of an object for looking up accessors that are registered on the target. If the object is
     * a Class object this will return the Class itself, else it will return object's getClass() result.
     */
    public static Class<?> getTargetClass( Object o )
    {
        return ( o == null ) ? null : ( ( o instanceof Class ) ? (Class<?>) o : o.getClass( ) );
    }

    /**
     * Returns the base name (the class name without the package name prepended) of the object given.
     */
    public static String getBaseName( Object o )
    {
        return ( o == null ) ? null : getClassBaseName( o.getClass( ) );
    }

    /**
     * Returns the base name (the class name without the package name prepended) of the class given.
     */
    public static String getClassBaseName( Class<?> c )
    {
        String s = c.getName( );

        return s.substring( s.lastIndexOf( '.' ) + 1 );
    }

    public static String getClassName( Object o, boolean fullyQualified )
    {
        if ( !( o instanceof Class ) )
        {
            o = o.getClass( );
        }

        return getClassName( (Class<?>) o, fullyQualified );
    }

    public static String getClassName( Class<?> c, boolean fullyQualified )
    {
        return fullyQualified ? c.getName( ) : getClassBaseName( c );
    }

    /**
     * Returns the package name of the object's class.
     */
    public static String getPackageName( Object o )
    {
        return ( o == null ) ? null : getClassPackageName( o.getClass( ) );
    }

    /**
     * Returns the package name of the class given.
     */
    public static String getClassPackageName( Class<?> c )
    {
        String s = c.getName( );
        int i = s.lastIndexOf( '.' );

        return ( i < 0 ) ? null : s.substring( 0, i );
    }

    /**
     * Returns a "pointer" string in the usual format for these things - 0x<hex digits>.
     */
    public static String getPointerString( int num )
    {
        StringBuffer result = new StringBuffer( );
        String hex = Integer.toHexString( num ), pad;
        Integer l = hex.length( );

        // result.append(HEX_PREFIX);
        if ( ( pad = HEX_PADDING.get( l ) ) == null )
        {
            StringBuffer pb = new StringBuffer( );

            for ( int i = hex.length( ); i < HEX_LENGTH; i++ )
            {
                pb.append( '0' );
            }
            pad = new String( pb );
            HEX_PADDING.put( l, pad );
        }
        result.append( pad );
        result.append( hex );
        return new String( result );
    }

    /**
     * Returns a "pointer" string in the usual format for these things - 0x<hex digits> for the object given. This will
     * always return a unique value for each object.
     */
    public static String getPointerString( Object o )
    {
        return getPointerString( ( o == null ) ? 0 : System.identityHashCode( o ) );
    }

    /**
     * Returns a unique descriptor string that includes the object's class and a unique integer identifier. If
     * fullyQualified is true then the class name will be fully qualified to include the package name, else it will be
     * just the class' base name.
     */
    public static String getUniqueDescriptor( Object object, boolean fullyQualified )
    {
        StringBuffer result = new StringBuffer( );

        if ( object != null )
        {
            if ( object instanceof Proxy )
            {
                Class<?> interfaceClass = object.getClass( ).getInterfaces( )[0];

                result.append( getClassName( interfaceClass, fullyQualified ) );
                result.append( '^' );
                object = Proxy.getInvocationHandler( object );
            }
            result.append( getClassName( object, fullyQualified ) );
            result.append( '@' );
            result.append( getPointerString( object ) );
        }
        else
        {
            result.append( NULL_OBJECT_STRING );
        }
        return new String( result );
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
        Object[] result;
        int size = list.size( );

        if ( size == 0 )
        {
            result = NoArguments;
        }
        else
        {
            result = getObjectArrayPool( ).create( list.size( ) );
            for ( int i = 0; i < size; i++ )
            {
                result[i] = list.get( i );
            }
        }
        return result;
    }

    /**
     * Returns the parameter types of the given method.
     */
    public static Class<?>[] getParameterTypes( Method m )
        throws CacheException
    {
        return _methodParameterTypesCache.get( m );

    }

    /**
     * Finds the appropriate parameter types for the given {@link Method} and {@link Class} instance of the type the
     * method is associated with. Correctly finds generic types if running in >= 1.5 jre as well.
     *
     * @param type The class type the method is being executed against.
     * @param m    The method to find types for.
     * @return Array of parameter types for the given method.
     */
    public static Class<?>[] findParameterTypes( Class<?> type, Method m )
        throws CacheException
    {
        if ( type == null )
        {
            return getParameterTypes( m );
        }

        if ( !isJdk15( ) || type.getGenericSuperclass( ) == null || !ParameterizedType.class.isInstance(
            type.getGenericSuperclass( ) ) || m.getDeclaringClass( ).getTypeParameters( ) == null )
        {
            return getParameterTypes( m );
        }

        Class<?>[] types = _genericMethodParameterTypesCache.get( new GenericMethodParameterTypeCacheEntry( m, type ) );

        /*if (  types != null )
        {
            ParameterizedType genericSuperclass = (ParameterizedType) type.getGenericSuperclass( );
            if ( Arrays.equals( types, genericSuperclass.getActualTypeArguments( ) ) )
            {
                return types;
            }
        }
*/
            return types;
    }

    static Class<?> findType( Type[] types, Class<?> type )
    {
        for ( Type t : types )
        {
            if ( Class.class.isInstance( t ) && type.isAssignableFrom( (Class<?>) t ) )
            {
                return (Class<?>) t;
            }
        }

        return null;
    }

    /**
     * Returns the parameter types of the given method.
     */
    public static Class<?>[] getParameterTypes( Constructor<?> c )
    {
        synchronized ( _ctorParameterTypesCache )
        {
            Class<?>[] result;

            if ( ( result = _ctorParameterTypesCache.get( c ) ) == null )
            {
                _ctorParameterTypesCache.put( c, result = c.getParameterTypes( ) );
            }
            return result;
        }
    }

    /**
     * Gets the SecurityManager that OGNL uses to determine permissions for invoking methods.
     *
     * @return SecurityManager for OGNL
     */
    public static SecurityManager getSecurityManager( )
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
    }

    /**
     * Permission will be named "invoke.<declaring-class>.<method-name>".
     */
    public static Permission getPermission( final Method method )
        throws OgnlException
    {
        return _invokePermissionCache.get( new PermissionCacheEntry( method ) );
    }

    public static Object invokeMethod( Object target, Method method, Object[] argsArray )
        throws IllegalAccessException, OgnlException, InvocationTargetException
    {
        boolean syncInvoke = false;
        boolean checkPermission = false;
        int mHash = method.hashCode( );

        // only synchronize method invocation if it actually requires it

        synchronized ( method )
        {
            if ( _methodAccessCache.get( mHash ) == null || _methodAccessCache.get( mHash ) == Boolean.TRUE )
            {
                syncInvoke = true;
            }

            if ( _securityManager != null && _methodPermCache.get( mHash ) == null
                || _methodPermCache.get( mHash ) == Boolean.FALSE )
            {
                checkPermission = true;
            }
        }

        Object result;
        boolean wasAccessible = true;

        if ( syncInvoke )
        {
            synchronized ( method )
            {
                if ( checkPermission )
                {
                    try
                    {
                        _securityManager.checkPermission( getPermission( method ) );
                        _methodPermCache.put( mHash, Boolean.TRUE );
                    }
                    catch ( SecurityException ex )
                    {
                        _methodPermCache.put( mHash, Boolean.FALSE );
                        throw new IllegalAccessException( "Method [" + method + "] cannot be accessed." );
                    }
                }

                if ( !Modifier.isPublic( method.getModifiers( ) ) || !Modifier.isPublic(
                    method.getDeclaringClass( ).getModifiers( ) ) )
                {
                    if ( !( wasAccessible = method.isAccessible( ) ) )
                    {
                        method.setAccessible( true );
                        _methodAccessCache.put( mHash, Boolean.TRUE );
                    }
                    else
                    {
                        _methodAccessCache.put( mHash, Boolean.FALSE );
                    }
                }
                else
                {
                    _methodAccessCache.put( mHash, Boolean.FALSE );
                }

                result = method.invoke( target, argsArray );

                if ( !wasAccessible )
                {
                    method.setAccessible( false );
                }
            }
        }
        else
        {
            if ( checkPermission )
            {
                try
                {
                    _securityManager.checkPermission( getPermission( method ) );
                    _methodPermCache.put( mHash, Boolean.TRUE );
                }
                catch ( SecurityException ex )
                {
                    _methodPermCache.put( mHash, Boolean.FALSE );
                    throw new IllegalAccessException( "Method [" + method + "] cannot be accessed." );
                }
            }

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
        Class<?> c = arg.getClass( );
        if ( c == Boolean.class )
        {
            return Boolean.TYPE;
        }
        else if ( c.getSuperclass( ) == Number.class )
        {
            if ( c == Integer.class )
            {
                return Integer.TYPE;
            }
            if ( c == Double.class )
            {
                return Double.TYPE;
            }
            if ( c == Byte.class )
            {
                return Byte.TYPE;
            }
            if ( c == Long.class )
            {
                return Long.TYPE;
            }
            if ( c == Float.class )
            {
                return Float.TYPE;
            }
            if ( c == Short.class )
            {
                return Short.TYPE;
            }
        }
        else if ( c == Character.class )
        {
            return Character.TYPE;
        }
        return c;
    }

    /**
     * Tells whether the given object is compatible with the given class ---that is, whether the given object can be
     * passed as an argument to a method or constructor whose parameter type is the given class. If object is null this
     * will return true because null is compatible with any type.
     */
    public static boolean isTypeCompatible( Object object, Class<?> c )
    {
        boolean result = true;

        if ( object != null )
        {
            if ( c.isPrimitive( ) )
            {
                if ( getArgClass( object ) != c )
                {
                    result = false;
                }
            }
            else if ( !c.isInstance( object ) )
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

    public static boolean areArgsCompatible( Object[] args, Class<?>[] classes, Method m )
    {
        boolean result = true;
        boolean varArgs = m != null && isJdk15( ) && m.isVarArgs( );

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

                if ( !result && classes[index].isArray( ) )
                {
                    result = isTypeCompatible( args[index], classes[index].getComponentType( ) );
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
            Class<?> c1 = classes1[index], c2 = classes2[index];
            if ( c1 != c2 )
            {
                if ( c1.isPrimitive( ) )
                {
                    return true;
                }
                else if ( c1.isAssignableFrom( c2 ) )
                {
                    return false;
                }
                else if ( c2.isAssignableFrom( c1 ) )
                {
                    return true;
                }
            }
        }

        // They are the same! So the first is not more specific than the second.
        return false;
    }

    public static String getModifierString( int modifiers )
    {
        String result;

        if ( Modifier.isPublic( modifiers ) )
        {
            result = "public";
        }
        else if ( Modifier.isProtected( modifiers ) )
        {
            result = "protected";
        }
        else if ( Modifier.isPrivate( modifiers ) )
        {
            result = "private";
        }
        else
        {
            result = "";
        }
        if ( Modifier.isStatic( modifiers ) )
        {
            result = "static " + result;
        }
        if ( Modifier.isFinal( modifiers ) )
        {
            result = "final " + result;
        }
        if ( Modifier.isNative( modifiers ) )
        {
            result = "native " + result;
        }
        if ( Modifier.isSynchronized( modifiers ) )
        {
            result = "synchronized " + result;
        }
        if ( Modifier.isTransient( modifiers ) )
        {
            result = "transient " + result;
        }
        return result;
    }

    public static Class<?> classForName( OgnlContext context, String className )
        throws ClassNotFoundException
    {
        Class<?> result = _primitiveTypes.get( className );

        if ( result == null )
        {
            ClassResolver resolver;

            if ( ( context == null ) || ( ( resolver = context.getClassResolver( ) ) == null ) )
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
            Class<?> c = classForName( context, className );
            return c.isInstance( value );
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
        return context.getTypeConverter( ).convertValue( context, target, member, propertyName, value, type );
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
                    Object v = getConvertedType( context, target, member, propertyName, arg, type );

                    if ( v == OgnlRuntime.NoConversionPossible )
                    {
                        result = false;
                    }
                    else
                    {
                        newArgs[i] = v;
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
        Method result = null;
        TypeConverter converter = context.getTypeConverter( );

        if ( ( converter != null ) && ( methods != null ) )
        {
            for ( int i = 0; ( result == null ) && ( i < methods.size( ) ); i++ )
            {
                Method m = methods.get( i );
                Class<?>[] parameterTypes =
                    findParameterTypes( target != null ? target.getClass( ) : null, m );// getParameterTypes(m);

                if ( getConvertedTypes( context, target, m, propertyName, parameterTypes, args, newArgs ) )
                {
                    result = m;
                }
            }
        }
        return result;
    }

    public static Constructor<?> getConvertedConstructorAndArgs( OgnlContext context, Object target,
                                                                 List<Constructor<?>> constructors, Object[] args,
                                                                 Object[] newArgs )
        throws OgnlException
    {
        Constructor<?> result = null;
        TypeConverter converter = context.getTypeConverter( );

        if ( ( converter != null ) && ( constructors != null ) )
        {
            for ( int i = 0; ( result == null ) && ( i < constructors.size( ) ); i++ )
            {
                Constructor<?> ctor = constructors.get( i );
                Class<?>[] parameterTypes = getParameterTypes( ctor );

                if ( getConvertedTypes( context, target, ctor, null, parameterTypes, args, newArgs ) )
                {
                    result = ctor;
                }
            }
        }
        return result;
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
        Method result = null;
        Class<?>[] resultParameterTypes = null;

        if ( methods != null )
        {
            for ( Method m : methods )
            {
                Class<?> typeClass = target != null ? target.getClass( ) : null;
                if ( typeClass == null && source != null && Class.class.isInstance( source ) )
                {
                    typeClass = (Class<?>) source;
                }

                Class<?>[] mParameterTypes = findParameterTypes( typeClass, m );

                if ( areArgsCompatible( args, mParameterTypes, m ) && ( ( result == null ) || isMoreSpecific(
                    mParameterTypes, resultParameterTypes ) ) )
                {
                    result = m;
                    resultParameterTypes = mParameterTypes;
                    System.arraycopy( args, 0, actualArgs, 0, args.length );

                    for ( int j = 0; j < mParameterTypes.length; j++ )
                    {
                        Class<?> type = mParameterTypes[j];

                        if ( type.isPrimitive( ) && ( actualArgs[j] == null ) )
                        {
                            actualArgs[j] = getConvertedType( context, source, result, propertyName, null, type );
                        }
                    }
                }
            }
        }

        if ( result == null )
        {
            result = getConvertedMethodAndArgs( context, target, propertyName, methods, args, actualArgs );
        }

        return result;
    }

    public static Object callAppropriateMethod( OgnlContext context, Object source, Object target, String methodName,
                                                String propertyName, List<Method> methods, Object[] args )
        throws MethodFailedException
    {
        Throwable reason = null;
        Object[] actualArgs = _objectArrayPool.create( args.length );

        try
        {
            Method method = getAppropriateMethod( context, source, target, propertyName, methods, args, actualArgs );

            if ( ( method == null ) || !isMethodAccessible( context, source, method, propertyName ) )
            {
                StringBuilder buffer = new StringBuilder( );
                String className = "";

                if ( target != null )
                {
                    className = target.getClass( ).getName( ) + ".";
                }

                for ( int i = 0, ilast = args.length - 1; i <= ilast; i++ )
                {
                    Object arg = args[i];

                    buffer.append( ( arg == null ) ? NULL_STRING : arg.getClass( ).getName( ) );
                    if ( i < ilast )
                    {
                        buffer.append( ", " );
                    }
                }

                throw new NoSuchMethodException( className + methodName + "(" + buffer + ")" );
            }

            Object[] convertedArgs = actualArgs;

            if ( isJdk15( ) && method.isVarArgs( ) )
            {
                Class<?>[] parmTypes = method.getParameterTypes( );

                // split arguments in to two dimensional array for varargs reflection invocation
                // where it is expected that the parameter passed in to invoke the method
                // will look like "new Object[] { arrayOfNonVarArgsArguments, arrayOfVarArgsArguments }"

                for ( int i = 0; i < parmTypes.length; i++ )
                {
                    if ( parmTypes[i].isArray( ) )
                    {
                        convertedArgs = new Object[i + 1];
                        System.arraycopy( actualArgs, 0, convertedArgs, 0, convertedArgs.length );

                        Object[] varArgs;

                        // if they passed in varargs arguments grab them and dump in to new varargs array

                        if ( actualArgs.length > i )
                        {
                            List<Object> varArgsList = new ArrayList<Object>( );
                            for ( int j = i; j < actualArgs.length; j++ )
                            {
                                if ( actualArgs[j] != null )
                                {
                                    varArgsList.add( actualArgs[j] );
                                }
                            }

                            varArgs = varArgsList.toArray( );
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
            reason = e;
        }
        catch ( IllegalAccessException e )
        {
            reason = e;
        }
        catch ( InvocationTargetException e )
        {
            reason = e.getTargetException( );
        }
        catch ( OgnlException e )
        {
            reason = e;
        }
        finally
        {
            _objectArrayPool.recycle( actualArgs );
        }

        throw new MethodFailedException( source, methodName, reason );
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

            MethodAccessor ma = getMethodAccessor( targetClass );

            return ma.callStaticMethod( context, targetClass, methodName, args );
        }
        catch ( ClassNotFoundException ex )
        {
            throw new MethodFailedException( className, methodName, ex );
        }
    }

    /**
     * Invokes the specified method against the target object.
     *
     * @param context      The current execution context.
     * @param target       The object to invoke the method on.
     * @param methodName   Name of the method - as in "getValue" or "add", etc..
     * @param propertyName Name of the property to call instead?
     * @param args         Optional arguments needed for method.
     * @return Result of invoking method.
     * @throws OgnlException For lots of different reasons.
     * @deprecated Use {@link #callMethod(OgnlContext, Object, String, Object[])} instead.
     */
    public static Object callMethod( OgnlContext context, Object target, String methodName, String propertyName,
                                     Object[] args )
        throws OgnlException
    {
        return callMethod( context, target, methodName == null ? propertyName : methodName, args );
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

        return getMethodAccessor( target.getClass( ) ).callMethod( context, target, methodName, args );
    }

    public static Object callConstructor( OgnlContext context, String className, Object[] args )
        throws OgnlException
    {
        Throwable reason = null;
        Object[] actualArgs = args;

        try
        {
            Constructor<?> ctor = null;
            Class<?>[] ctorParameterTypes = null;
            Class<?> target = classForName( context, className );
            List<Constructor<?>> constructors = getConstructors( target );

            for ( Constructor<?> c : constructors )
            {
                Class<?>[] cParameterTypes = getParameterTypes( c );

                if ( areArgsCompatible( args, cParameterTypes ) && ( ctor == null || isMoreSpecific( cParameterTypes,
                                                                                                     ctorParameterTypes ) ) )
                {
                    ctor = c;
                    ctorParameterTypes = cParameterTypes;
                }
            }
            if ( ctor == null )
            {
                actualArgs = _objectArrayPool.create( args.length );
                if ( ( ctor = getConvertedConstructorAndArgs( context, target, constructors, args, actualArgs ) )
                    == null )
                {
                    throw new NoSuchMethodException( );
                }
            }
            if ( !context.getMemberAccess( ).isAccessible( context, target, ctor, null ) )
            {
                throw new IllegalAccessException( "access denied to " + target.getName( ) + "()" );
            }
            return ctor.newInstance( actualArgs );
        }
        catch ( ClassNotFoundException e )
        {
            reason = e;
        }
        catch ( NoSuchMethodException e )
        {
            reason = e;
        }
        catch ( IllegalAccessException e )
        {
            reason = e;
        }
        catch ( InvocationTargetException e )
        {
            reason = e.getTargetException( );
        }
        catch ( InstantiationException e )
        {
            reason = e;
        }
        finally
        {
            if ( actualArgs != args )
            {
                _objectArrayPool.recycle( actualArgs );
            }
        }

        throw new MethodFailedException( className, "new", reason );
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
        Object result = null;
        Method m = getGetMethod( context, ( target == null ) ? null : target.getClass( ), propertyName );
        if ( m == null )
        {
            m = getReadMethod( ( target == null ) ? null : target.getClass( ), propertyName, 0 );
        }

        if ( checkAccessAndExistence )
        {
            if ( ( m == null ) || !context.getMemberAccess( ).isAccessible( context, target, m, propertyName ) )
            {
                result = NotFound;
            }
        }
        if ( result == null )
        {
            if ( m != null )
            {
                try
                {
                    result = invokeMethod( target, m, NoArguments );
                }
                catch ( InvocationTargetException ex )
                {
                    throw new OgnlException( propertyName, ex.getTargetException( ) );
                }
            }
            else
            {
                throw new NoSuchMethodException( propertyName );
            }
        }
        return result;
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
        Method m = getSetMethod( context, ( target == null ) ? null : target.getClass( ), propertyName );

        if ( checkAccessAndExistence )
        {
            if ( ( m == null ) || !context.getMemberAccess( ).isAccessible( context, target, m, propertyName ) )
            {
                result = false;
            }
        }

        if ( result )
        {
            if ( m != null )
            {
                Object[] args = _objectArrayPool.create( value );

                try
                {
                    callAppropriateMethod( context, target, target, m.getName( ), propertyName,
                                           Collections.nCopies( 1, m ), args );
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

    public static List<Constructor<?>> getConstructors( final Class<?> targetClass )
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
        DeclaredMethodCacheEntry.MethodType type;
        if ( staticMethods )
        {
            type = DeclaredMethodCacheEntry.MethodType.STATIC;
        }
        else
        {
            type = DeclaredMethodCacheEntry.MethodType.NON_STATIC;

        }
        return _methodCache.get( new DeclaredMethodCacheEntry( targetClass, type) );
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
        Field o = getFields( inClass ).get( name );

        if ( o == null )
        {
            // if o is null, it should search along the superclasses
            Class<?> sc = inClass.getSuperclass( );
            while ( ( sc != null ) )
            {
                o = getFields( sc ).get( name );
                if ( o != null )
                {
                    return o;
                }
                sc = sc.getSuperclass( );
            }
        }
        return o;
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
        Field f = getField( ( target == null ) ? null : target.getClass( ), propertyName );

        if ( checkAccessAndExistence )
        {
            if ( ( f == null ) || !context.getMemberAccess( ).isAccessible( context, target, f, propertyName ) )
            {
                result = NotFound;
            }
        }
        if ( result == null )
        {
            if ( f == null )
            {
                throw new NoSuchFieldException( propertyName );
            }
            try
            {
                Object state;

                if ( !Modifier.isStatic( f.getModifiers( ) ) )
                {
                    state = context.getMemberAccess( ).setup( context, target, f, propertyName );
                    result = f.get( target );
                    context.getMemberAccess( ).restore( context, target, f, propertyName, state );
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
            Field f = getField( ( target == null ) ? null : target.getClass( ), propertyName );
            Object state;

            if ( ( f != null ) && !Modifier.isStatic( f.getModifiers( ) ) )
            {
                state = context.getMemberAccess( ).setup( context, target, f, propertyName );
                try
                {
                    if ( isTypeCompatible( value, f.getType( ) ) || (
                        ( value = getConvertedType( context, target, f, propertyName, value, f.getType( ) ) )
                            != null ) )
                    {
                        f.set( target, value );
                        result = true;
                    }
                }
                finally
                {
                    context.getMemberAccess( ).restore( context, target, f, propertyName, state );
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
        return context.getMemberAccess( ).isAccessible( context, target, field, propertyName );
    }

    public static boolean hasField( OgnlContext context, Object target, Class<?> inClass, String propertyName )
        throws OgnlException
    {
        Field f = getField( inClass, propertyName );

        return ( f != null ) && isFieldAccessible( context, target, f, propertyName );
    }

    public static Object getStaticField( OgnlContext context, String className, String fieldName )
        throws OgnlException
    {
        Exception reason;
        try
        {
            Class<?> c = classForName( context, className );

            if ( c == null )
            {
                throw new OgnlException(
                    "Unable to find class " + className + " when resolving field name of " + fieldName );
            }

            /*
             * Check for virtual static field "class"; this cannot interfere with normal static fields because it is a
             * reserved word.
             */
            if ( fieldName.equals( "class" ) )
            {
                return c;
            }
            else if ( OgnlRuntime.isJdk15( ) && c.isEnum( ) )
            {
                @SuppressWarnings( "unchecked" ) // see the if condition
                    Enum<?> ret = Enum.valueOf( (Class<? extends Enum>) c, fieldName );
                return ret;
            }
            else
            {
                Field f = c.getField( fieldName );
                if ( !Modifier.isStatic( f.getModifiers( ) ) )
                {
                    throw new OgnlException( "Field " + fieldName + " of class " + className + " is not static" );
                }

                return f.get( null );
            }
        }
        catch ( ClassNotFoundException e )
        {
            reason = e;
        }
        catch ( NoSuchFieldException e )
        {
            reason = e;
        }
        catch ( SecurityException e )
        {
            reason = e;
        }
        catch ( IllegalAccessException e )
        {
            reason = e;
        }

        throw new OgnlException( "Could not get static field " + fieldName + " from class " + className, reason );
    }

    /**
     *
     * @param targetClass
     * @param propertyName
     * @param findSets
     * @return Returns the list of (g)setter of a class for a given property name
     * @throws OgnlException
     */
    public static List<Method> getDeclaredMethods( final Class<?> targetClass, final String propertyName,
                                                   final boolean findSets )
        throws OgnlException
    {
        String baseName = Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );
        List<Method> result = new ArrayList<Method>( );
        List<String> find = new ArrayList<String>( 2 );
        if(findSets)
        {
            find.add( SET_PREFIX + baseName);
        } else
        {
            find.add( IS_PREFIX + baseName );
            find.add(GET_PREFIX + baseName );
            
        }
        for ( String s : find )
        {
            List<Method> methodList = _methodCache.get( new DeclaredMethodCacheEntry( targetClass ) ).get( s );
            if(methodList!=null)
                result.addAll( methodList );
        }

        return result;
            
    }

    /**
     * Convenience used to check if a method is volatile or synthetic so as to avoid calling un-callable methods.
     *
     * @param m The method to check.
     * @return True if the method should be callable, false otherwise.
     */
    //TODO: the method was intented as private, so it'd need to move in a util class
    public static boolean isMethodCallable( Method m )
    {
        return !( ( isJdk15( ) && m.isSynthetic( ) ) || Modifier.isVolatile( m.getModifiers( ) ) );

    }

    public static Method getGetMethod( OgnlContext context, Class<?> targetClass, String propertyName )
        throws IntrospectionException, OgnlException
    {
        Method result = null;

        List<Method> methods = getDeclaredMethods( targetClass, propertyName, false /* find 'get' methods */ );

        if ( methods != null )
        {
            for ( Method m : methods )
            {
                Class<?>[] mParameterTypes = findParameterTypes( targetClass, m ); // getParameterTypes(m);

                if ( mParameterTypes.length == 0 )
                {
                    result = m;
                    break;
                }
            }
        }

        return result;
    }

    public static boolean isMethodAccessible( OgnlContext context, Object target, Method method, String propertyName )
    {
        return ( method != null ) && context.getMemberAccess( ).isAccessible( context, target, method, propertyName );
    }

    public static boolean hasGetMethod( OgnlContext context, Object target, Class<?> targetClass, String propertyName )
        throws IntrospectionException, OgnlException
    {
        return isMethodAccessible( context, target, getGetMethod( context, targetClass, propertyName ), propertyName );
    }

    public static Method getSetMethod( OgnlContext context, Class<?> targetClass, String propertyName )
        throws IntrospectionException, OgnlException
    {
        Method result = null;

        List<Method> methods = getDeclaredMethods( targetClass, propertyName, true /* find 'set' methods */ );

        if ( methods != null )
        {
            for ( Method m : methods )
            {
                Class<?>[] mParameterTypes = findParameterTypes( targetClass, m ); // getParameterTypes(m);

                if ( mParameterTypes.length == 1 )
                {
                    result = m;
                    break;
                }
            }
        }

        return result;
    }

    public static boolean hasSetMethod( OgnlContext context, Object target, Class<?> targetClass, String propertyName )
        throws IntrospectionException, OgnlException
    {
        return isMethodAccessible( context, target, getSetMethod( context, targetClass, propertyName ), propertyName );
    }

    public static boolean hasGetProperty( OgnlContext context, Object target, Object oname )
        throws IntrospectionException, OgnlException
    {
        Class<?> targetClass = ( target == null ) ? null : target.getClass( );
        String name = oname.toString( );

        return hasGetMethod( context, target, targetClass, name ) || hasField( context, target, targetClass, name );
    }

    public static boolean hasSetProperty( OgnlContext context, Object target, Object oname )
        throws IntrospectionException, OgnlException
    {
        Class<?> targetClass = ( target == null ) ? null : target.getClass( );
        String name = oname.toString( );

        return hasSetMethod( context, target, targetClass, name ) || hasField( context, target, targetClass, name );
    }

    private static boolean indexMethodCheck( List<Method> methods )
        throws CacheException
    {
        boolean result = false;

        if ( methods.size( ) > 0 )
        {
            Method fm = methods.get( 0 );
            Class<?>[] fmpt = getParameterTypes( fm );
            int fmpc = fmpt.length;
            Class<?> lastMethodClass = fm.getDeclaringClass( );

            result = true;
            for ( int i = 1; result && ( i < methods.size( ) ); i++ )
            {
                Method m = methods.get( i );
                Class<?> c = m.getDeclaringClass( );

                // Check to see if more than one method implemented per class
                if ( lastMethodClass == c )
                {
                    result = false;
                }
                else
                {
                    Class<?>[] mpt = getParameterTypes( fm );
                    int mpc = fmpt.length;

                    if ( fmpc != mpc )
                    {
                        result = false;
                    }
                    for ( int j = 0; j < fmpc; j++ )
                    {
                        if ( fmpt[j] != mpt[j] )
                        {
                            result = false;
                            break;
                        }
                    }
                }
                lastMethodClass = c;
            }
        }
        return result;
    }

    public static void findObjectIndexedPropertyDescriptors( Class<?> targetClass,
                                                             Map<String, PropertyDescriptor> intoMap )
        throws OgnlException
    {
        Map<String, List<Method>> allMethods = getMethods( targetClass, false );
        Map<String, List<Method>> pairs = new HashMap<String, List<Method>>( 101 );

        for ( String methodName : allMethods.keySet( ) )
        {
            List<Method> methods = allMethods.get( methodName );

            /*
             * Only process set/get where there is exactly one implementation of the method per class and those
             * implementations are all the same
             */
            if ( indexMethodCheck( methods ) )
            {
                boolean isGet = false, isSet;
                Method m = methods.get( 0 );

                if ( ( ( isSet = methodName.startsWith( SET_PREFIX ) ) || ( isGet =
                    methodName.startsWith( GET_PREFIX ) ) ) && ( methodName.length( ) > 3 ) )
                {
                    String propertyName = Introspector.decapitalize( methodName.substring( 3 ) );
                    Class<?>[] parameterTypes = getParameterTypes( m );
                    int parameterCount = parameterTypes.length;

                    if ( isGet && ( parameterCount == 1 ) && ( m.getReturnType( ) != Void.TYPE ) )
                    {
                        List<Method> pair = pairs.get( propertyName );

                        if ( pair == null )
                        {
                            pairs.put( propertyName, pair = new ArrayList<Method>( ) );
                        }
                        pair.add( m );
                    }
                    if ( isSet && ( parameterCount == 2 ) && ( m.getReturnType( ) == Void.TYPE ) )
                    {
                        List<Method> pair = pairs.get( propertyName );

                        if ( pair == null )
                        {
                            pairs.put( propertyName, pair = new ArrayList<Method>( ) );
                        }
                        pair.add( m );
                    }
                }
            }
        }

        for ( String propertyName : pairs.keySet( ) )
        {
            List<Method> methods = pairs.get( propertyName );

            if ( methods.size( ) == 2 )
            {
                Method method1 = methods.get( 0 ), method2 = methods.get( 1 ), setMethod =
                    ( method1.getParameterTypes( ).length == 2 ) ? method1 : method2, getMethod =
                    ( setMethod == method1 ) ? method2 : method1;
                Class<?> keyType = getMethod.getParameterTypes( )[0], propertyType = getMethod.getReturnType( );

                if ( keyType == setMethod.getParameterTypes( )[0] )
                {
                    if ( propertyType == setMethod.getParameterTypes( )[1] )
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
    public static Map<String, PropertyDescriptor> getPropertyDescriptors( final Class<?> targetClass )
        throws IntrospectionException, OgnlException
    {
        return _propertyDescriptorCache.get( targetClass );
    }

    /**
     * This method returns a PropertyDescriptor for the given class and property name using a Map lookup (using
     * getPropertyDescriptorsMap()).
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
        return (PropertyDescriptor[]) getPropertyDescriptors( targetClass ).entrySet( ).toArray( );
    }

    /**
     * Gets the property descriptor with the given name for the target class given.
     *
     * @param targetClass Class for which property descriptor is desired
     * @param name        Name of property
     * @return PropertyDescriptor of the named property or null if the class has no property with the given name
     */
    public static PropertyDescriptor getPropertyDescriptorFromArray( Class<?> targetClass, String name )
        throws IntrospectionException, OgnlException
    {
        PropertyDescriptor result = null;
        PropertyDescriptor[] pda = getPropertyDescriptorsArray( targetClass );

        for ( int i = 0, icount = pda.length; ( result == null ) && ( i < icount ); i++ )
        {
            if ( pda[i].getName( ).compareTo( name ) == 0 )
            {
                result = pda[i];
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
            throw new OgnlException( "No property accessor for " + getTargetClass( source ).getName( ) );
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
            throw new OgnlException( "No property accessor for " + getTargetClass( target ).getName( ) );
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
            PropertyDescriptor pd = getPropertyDescriptor( sourceClass, name );
            if ( pd != null )
            {
                if ( pd instanceof IndexedPropertyDescriptor )
                {
                    result = INDEXED_PROPERTY_INT;
                }
                else
                {
                    if ( pd instanceof ObjectIndexedPropertyDescriptor )
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
            PropertyDescriptor pd = getPropertyDescriptor( ( source == null ) ? null : source.getClass( ), name );
            Method m;

            if ( pd instanceof IndexedPropertyDescriptor )
            {
                m = ( (IndexedPropertyDescriptor) pd ).getIndexedReadMethod( );
            }
            else
            {
                if ( pd instanceof ObjectIndexedPropertyDescriptor )
                {
                    m = ( (ObjectIndexedPropertyDescriptor) pd ).getIndexedReadMethod( );
                }
                else
                {
                    throw new OgnlException( "property '" + name + "' is not an indexed property" );
                }
            }

            return callMethod( context, source, m.getName( ), args );

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
            PropertyDescriptor pd = getPropertyDescriptor( ( source == null ) ? null : source.getClass( ), name );
            Method m;

            if ( pd instanceof IndexedPropertyDescriptor )
            {
                m = ( (IndexedPropertyDescriptor) pd ).getIndexedWriteMethod( );
            }
            else
            {
                if ( pd instanceof ObjectIndexedPropertyDescriptor )
                {
                    m = ( (ObjectIndexedPropertyDescriptor) pd ).getIndexedWriteMethod( );
                }
                else
                {
                    throw new OgnlException( "property '" + name + "' is not an indexed property" );
                }
            }

            callMethod( context, source, m.getName( ), args );

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

    public static EvaluationPool getEvaluationPool( )
    {
        return _evaluationPool;
    }

    public static ObjectArrayPool getObjectArrayPool( )
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
//        _methodCache.setClassInspector( _cacheInspector );
//        _invokePermissionCache.setClassInspector( _cacheInspector );
        _fieldCache.setClassInspector( _cacheInspector );
//        _setterMethods.setClassInspector( _cacheInspector );
//        _getterMethods.setClassInspector( _cacheInspector );
    }

    public static Method getMethod( OgnlContext context, Class<?> target, String name, Node[] children,
                                    boolean includeStatic )
        throws OgnlException
    {
        Class<?>[] parms;
        if ( children != null && children.length > 0 )
        {
            parms = new Class[children.length];

            // used to reset context after loop
            Class<?> currType = context.getCurrentType( );
            Class<?> currAccessor = context.getCurrentAccessor( );
            Object cast = context.get( ExpressionCompiler.PRE_CAST );

            context.setCurrentObject( context.getRoot( ) );
            context.setCurrentType( context.getRoot( ) != null ? context.getRoot( ).getClass( ) : null );
            context.setCurrentAccessor( null );
            context.setPreviousType( null );

            for ( int i = 0; i < children.length; i++ )
            {
                children[i].toGetSourceString( context, context.getRoot( ) );
                parms[i] = context.getCurrentType( );
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

        for ( Method m : methods )
        {
            boolean varArgs = isJdk15( ) && m.isVarArgs( );

            if ( parms.length != m.getParameterTypes( ).length && !varArgs )
            {
                continue;
            }

            Class<?>[] mparms = m.getParameterTypes( );
            boolean matched = true;
            for ( int p = 0; p < mparms.length; p++ )
            {
                if ( varArgs && mparms[p].isArray( ) )
                {
                    continue;
                }

                if ( parms[p] == null )
                {
                    matched = false;
                    break;
                }

                if ( parms[p] == mparms[p] )
                {
                    continue;
                }

                if ( mparms[p].isPrimitive( ) && Character.TYPE != mparms[p] && Byte.TYPE != mparms[p]
                    && Number.class.isAssignableFrom( parms[p] )
                    && OgnlRuntime.getPrimitiveWrapperClass( parms[p] ) == mparms[p] )
                {
                    continue;
                }

                matched = false;
                break;
            }

            if ( matched )
            {
                return m;
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
            name = name.replaceAll( "\"", "" ).toLowerCase( );

            BeanInfo info = Introspector.getBeanInfo( target );
            MethodDescriptor[] methods = info.getMethodDescriptors( );

            // exact matches first

            Method m = null;

            for ( MethodDescriptor method : methods )
            {
                if ( !isMethodCallable( method.getMethod( ) ) )
                {
                    continue;
                }

                String methodName = method.getName( );
                String lowerMethodName = methodName.toLowerCase( );
                int methodParamLen = method.getMethod( ).getParameterTypes( ).length;

                if ( ( methodName.equalsIgnoreCase( name ) || lowerMethodName.equals( "get" + name )
                    || lowerMethodName.equals( "has" + name ) || lowerMethodName.equals( "is" + name ) )
                    && !methodName.startsWith( "set" ) )
                {
                    if ( numParms > 0 && methodParamLen == numParms )
                    {
                        return method.getMethod( );
                    }
                    else if ( numParms < 0 )
                    {
                        if ( methodName.equals( name ) )
                        {
                            return method.getMethod( );
                        }
                        else if ( m == null || ( m.getParameterTypes( ).length > methodParamLen ) )
                        {
                            m = method.getMethod( );
                        }
                    }
                }
            }

            if ( m != null )
            {
                return m;
            }

            for ( MethodDescriptor method : methods )
            {
                if ( !isMethodCallable( method.getMethod( ) ) )
                {
                    continue;
                }

                if ( method.getName( ).toLowerCase( ).endsWith( name ) && !method.getName( ).startsWith( "set" )
                    && method.getMethod( ).getReturnType( ) != Void.TYPE )
                {

                    if ( numParms > 0 && method.getMethod( ).getParameterTypes( ).length == numParms )
                    {
                        return method.getMethod( );
                    }
                    else if ( numParms < 0 )
                    {
                        if ( ( m != null
                            && m.getParameterTypes( ).length > method.getMethod( ).getParameterTypes( ).length )
                            || m == null )
                        {
                            m = method.getMethod( );
                        }
                    }
                }
            }

            if ( m != null )
            {
                return m;
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
            MethodDescriptor[] methods = info.getMethodDescriptors( );

            for ( MethodDescriptor method : methods )
            {
                if ( !isMethodCallable( method.getMethod( ) ) )
                {
                    continue;
                }

                if ( ( method.getName( ).equalsIgnoreCase( name ) || method.getName( ).toLowerCase( ).equals(
                    name.toLowerCase( ) ) || method.getName( ).toLowerCase( ).equals( "set" + name.toLowerCase( ) ) )
                    && !method.getName( ).startsWith( "get" ) )
                {

                    if ( numParms > 0 && method.getMethod( ).getParameterTypes( ).length == numParms )
                    {
                        return method.getMethod( );
                    }
                    else if ( numParms < 0 )
                    {
                        return method.getMethod( );
                    }
                }
            }

            // try again on pure class

            Method[] cmethods = target.getClass( ).getMethods( );
            for ( Method cmethod : cmethods )
            {
                if ( !isMethodCallable( cmethod ) )
                {
                    continue;
                }

                if ( ( cmethod.getName( ).equalsIgnoreCase( name ) || cmethod.getName( ).toLowerCase( ).equals(
                    name.toLowerCase( ) ) || cmethod.getName( ).toLowerCase( ).equals( "set" + name.toLowerCase( ) ) )
                    && !cmethod.getName( ).startsWith( "get" ) )
                {

                    if ( numParms > 0 && cmethod.getParameterTypes( ).length == numParms )
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

            PropertyDescriptor[] pds = info.getPropertyDescriptors( );

            for ( PropertyDescriptor pd : pds )
            {

                if ( pd.getName( ).equalsIgnoreCase( name ) || pd.getName( ).toLowerCase( ).equals(
                    name.toLowerCase( ) ) || pd.getName( ).toLowerCase( ).endsWith( name.toLowerCase( ) ) )
                {
                    return pd;
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

        if ( "true".equals( expression ) || "false".equals( expression ) || "!true".equals( expression )
            || "!false".equals( expression ) || "(true)".equals( expression ) || "!(true)".equals( expression )
            || "(false)".equals( expression ) || "!(false)".equals( expression ) || expression.startsWith(
            "org.apache.commons.ognl.OgnlOps" ) )
        {
            return true;
        }

        return false;
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
        if ( context.getCurrentType( ) == null || context.getPreviousType( ) == null )
        {
            return true;
        }

        if ( context.getCurrentType( ) == context.getPreviousType( ) && context.getCurrentType( ).isPrimitive( )
            && context.getPreviousType( ).isPrimitive( ) )
        {
            return false;
        }

        return context.getCurrentType( ) != null && !context.getCurrentType( ).isArray( )
            && context.getPreviousType( ) != null && !context.getPreviousType( ).isArray( );
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

        if ( !ASTConst.class.isInstance( child ) && ( target == null || context.getRoot( ) != target ) )
        {
            source = pre + source;
        }

        if ( context.getRoot( ) != null )
        {
            source = ExpressionCompiler.getRootExpression( child, context.getRoot( ), context ) + source;
            context.setCurrentAccessor( context.getRoot( ).getClass( ) );
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

        if ( source == null || source.trim( ).length( ) < 1 )
        {
            source = "null";
        }

        return source;
    }
}
