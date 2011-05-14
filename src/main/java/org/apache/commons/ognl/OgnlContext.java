/*
 * $Id$
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.ognl;

import org.apache.commons.ognl.enhance.LocalReference;

import java.util.*;

/**
 * This class defines the execution context for an OGNL expression
 * 
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class OgnlContext
    extends Object
    implements Map
{

    public static final String CONTEXT_CONTEXT_KEY = "context";

    public static final String ROOT_CONTEXT_KEY = "root";

    public static final String THIS_CONTEXT_KEY = "this";

    public static final String TRACE_EVALUATIONS_CONTEXT_KEY = "_traceEvaluations";

    public static final String LAST_EVALUATION_CONTEXT_KEY = "_lastEvaluation";

    public static final String KEEP_LAST_EVALUATION_CONTEXT_KEY = "_keepLastEvaluation";

    public static final String CLASS_RESOLVER_CONTEXT_KEY = "_classResolver";

    public static final String TYPE_CONVERTER_CONTEXT_KEY = "_typeConverter";

    public static final String MEMBER_ACCESS_CONTEXT_KEY = "_memberAccess";

    private static final String PROPERTY_KEY_PREFIX = "ognl";

    private static boolean DEFAULT_TRACE_EVALUATIONS = false;

    private static boolean DEFAULT_KEEP_LAST_EVALUATION = false;

    public static final ClassResolver DEFAULT_CLASS_RESOLVER = new DefaultClassResolver();

    public static final TypeConverter DEFAULT_TYPE_CONVERTER = new DefaultTypeConverter();

    public static final MemberAccess DEFAULT_MEMBER_ACCESS = new DefaultMemberAccess( false );

    private static final Set<String> RESERVED_KEYS = new HashSet<String>( 11 );

    private Object _root;

    private Object _currentObject;

    private Node _currentNode;

    private boolean _traceEvaluations = DEFAULT_TRACE_EVALUATIONS;

    private Evaluation _rootEvaluation;

    private Evaluation _currentEvaluation;

    private Evaluation _lastEvaluation;

    private boolean _keepLastEvaluation = DEFAULT_KEEP_LAST_EVALUATION;

    private Map _values = new HashMap( 23 );

    private ClassResolver _classResolver = DEFAULT_CLASS_RESOLVER;

    private TypeConverter _typeConverter = DEFAULT_TYPE_CONVERTER;

    private MemberAccess _memberAccess = DEFAULT_MEMBER_ACCESS;

    static
    {
        String s;

        RESERVED_KEYS.add( CONTEXT_CONTEXT_KEY );
        RESERVED_KEYS.add( ROOT_CONTEXT_KEY );
        RESERVED_KEYS.add( THIS_CONTEXT_KEY );
        RESERVED_KEYS.add( TRACE_EVALUATIONS_CONTEXT_KEY );
        RESERVED_KEYS.add( LAST_EVALUATION_CONTEXT_KEY );
        RESERVED_KEYS.add( KEEP_LAST_EVALUATION_CONTEXT_KEY );
        RESERVED_KEYS.add( CLASS_RESOLVER_CONTEXT_KEY );
        RESERVED_KEYS.add( TYPE_CONVERTER_CONTEXT_KEY );
        RESERVED_KEYS.add( MEMBER_ACCESS_CONTEXT_KEY );

        try
        {
            if ( ( s = System.getProperty( PROPERTY_KEY_PREFIX + ".traceEvaluations" ) ) != null )
            {
                DEFAULT_TRACE_EVALUATIONS = Boolean.valueOf( s.trim() ).booleanValue();
            }
            if ( ( s = System.getProperty( PROPERTY_KEY_PREFIX + ".keepLastEvaluation" ) ) != null )
            {
                DEFAULT_KEEP_LAST_EVALUATION = Boolean.valueOf( s.trim() ).booleanValue();
            }
        }
        catch ( SecurityException ex )
        {
            // restricted access environment, just keep defaults
        }
    }

    private Stack<Class<?>> _typeStack = new Stack<Class<?>>();

    private Stack<Class<?>> _accessorStack = new Stack<Class<?>>();

    private int _localReferenceCounter = 0;

    private Map<String, LocalReference> _localReferenceMap = null;

    /**
     * Constructs a new OgnlContext with the default class resolver, type converter and member access.
     */
    public OgnlContext()
    {
    }

    /**
     * Constructs a new OgnlContext with the given class resolver, type converter and member access. If any of these
     * parameters is null the default will be used.
     */
    public OgnlContext( ClassResolver classResolver, TypeConverter typeConverter, MemberAccess memberAccess )
    {
        this();
        if ( classResolver != null )
        {
            this._classResolver = classResolver;
        }
        if ( typeConverter != null )
        {
            this._typeConverter = typeConverter;
        }
        if ( memberAccess != null )
        {
            this._memberAccess = memberAccess;
        }
    }

    public OgnlContext( Map values )
    {
        super();
        this._values = values;
    }

    public OgnlContext( ClassResolver classResolver, TypeConverter typeConverter, MemberAccess memberAccess, Map values )
    {
        this( classResolver, typeConverter, memberAccess );
        this._values = values;
    }

    public void setValues( Map value )
    {
        for ( Iterator it = value.keySet().iterator(); it.hasNext(); )
        {
            Object k = it.next();

            _values.put( k, value.get( k ) );
        }
    }

    public Map getValues()
    {
        return _values;
    }

    public void setClassResolver( ClassResolver value )
    {
        if ( value == null )
        {
            throw new IllegalArgumentException( "cannot set ClassResolver to null" );
        }
        _classResolver = value;
    }

    public ClassResolver getClassResolver()
    {
        return _classResolver;
    }

    public void setTypeConverter( TypeConverter value )
    {
        if ( value == null )
        {
            throw new IllegalArgumentException( "cannot set TypeConverter to null" );
        }
        _typeConverter = value;
    }

    public TypeConverter getTypeConverter()
    {
        return _typeConverter;
    }

    public void setMemberAccess( MemberAccess value )
    {
        if ( value == null )
        {
            throw new IllegalArgumentException( "cannot set MemberAccess to null" );
        }
        _memberAccess = value;
    }

    public MemberAccess getMemberAccess()
    {
        return _memberAccess;
    }

    public void setRoot( Object value )
    {
        _root = value;
        _accessorStack.clear();
        _typeStack.clear();
        _currentObject = value;

        if ( _currentObject != null )
        {
            setCurrentType( _currentObject.getClass() );
        }
    }

    public Object getRoot()
    {
        return _root;
    }

    public boolean getTraceEvaluations()
    {
        return _traceEvaluations;
    }

    public void setTraceEvaluations( boolean value )
    {
        _traceEvaluations = value;
    }

    public Evaluation getLastEvaluation()
    {
        return _lastEvaluation;
    }

    public void setLastEvaluation( Evaluation value )
    {
        _lastEvaluation = value;
    }

    /**
     * This method can be called when the last evaluation has been used and can be returned for reuse in the free pool
     * maintained by the runtime. This is not a necessary step, but is useful for keeping memory usage down. This will
     * recycle the last evaluation and then set the last evaluation to null.
     */
    public void recycleLastEvaluation()
    {
        OgnlRuntime.getEvaluationPool().recycleAll( _lastEvaluation );
        _lastEvaluation = null;
    }

    /**
     * Returns true if the last evaluation that was done on this context is retained and available through
     * <code>getLastEvaluation()</code>. The default is true.
     */
    public boolean getKeepLastEvaluation()
    {
        return _keepLastEvaluation;
    }

    /**
     * Sets whether the last evaluation that was done on this context is retained and available through
     * <code>getLastEvaluation()</code>. The default is true.
     */
    public void setKeepLastEvaluation( boolean value )
    {
        _keepLastEvaluation = value;
    }

    public void setCurrentObject( Object value )
    {
        _currentObject = value;
    }

    public Object getCurrentObject()
    {
        return _currentObject;
    }

    public void setCurrentAccessor( Class<?> type )
    {
        _accessorStack.add( type );
    }

    public Class<?> getCurrentAccessor()
    {
        if ( _accessorStack.isEmpty() )
        {
            return null;
        }

        return _accessorStack.peek();
    }

    public Class<?> getPreviousAccessor()
    {
        if ( _accessorStack.isEmpty() )
        {
            return null;
        }

        if ( _accessorStack.size() > 1 )
        {
            return _accessorStack.get( _accessorStack.size() - 2 );
        }

        return null;
    }

    public Class<?> getFirstAccessor()
    {
        if ( _accessorStack.isEmpty() )
        {
            return null;
        }

        return _accessorStack.get( 0 );
    }

    /**
     * Gets the current class type being evaluated on the stack, as set by {@link #setCurrentType(Class)}.
     * 
     * @return The current object type, may be null.
     */
    public Class<?> getCurrentType()
    {
        if ( _typeStack.isEmpty() )
        {
            return null;
        }

        return _typeStack.peek();
    }

    public void setCurrentType( Class<?> type )
    {
        _typeStack.add( type );
    }

    /**
     * Represents the last known object type on the evaluation stack, will be the value of the last known
     * {@link #getCurrentType()}.
     * 
     * @return The previous type of object on the stack, may be null.
     */
    public Class<?> getPreviousType()
    {
        if ( _typeStack.isEmpty() )
        {
            return null;
        }

        if ( _typeStack.size() > 1 )
        {
            return _typeStack.get( _typeStack.size() - 2 );
        }

        return null;
    }

    public void setPreviousType( Class<?> type )
    {
        if ( _typeStack.isEmpty() || _typeStack.size() < 2 )
        {
            return;
        }

        _typeStack.set( _typeStack.size() - 2, type );
    }

    public Class<?> getFirstType()
    {
        if ( _typeStack.isEmpty() )
        {
            return null;
        }

        return _typeStack.get( 0 );
    }

    public void setCurrentNode( Node value )
    {
        _currentNode = value;
    }

    public Node getCurrentNode()
    {
        return _currentNode;
    }

    /**
     * Gets the current Evaluation from the top of the stack. This is the Evaluation that is in process of evaluating.
     */
    public Evaluation getCurrentEvaluation()
    {
        return _currentEvaluation;
    }

    public void setCurrentEvaluation( Evaluation value )
    {
        _currentEvaluation = value;
    }

    /**
     * Gets the root of the evaluation stack. This Evaluation contains the node representing the root expression and the
     * source is the root source object.
     */
    public Evaluation getRootEvaluation()
    {
        return _rootEvaluation;
    }

    public void setRootEvaluation( Evaluation value )
    {
        _rootEvaluation = value;
    }

    /**
     * Returns the Evaluation at the relative index given. This should be zero or a negative number as a relative
     * reference back up the evaluation stack. Therefore getEvaluation(0) returns the current Evaluation.
     */
    public Evaluation getEvaluation( int relativeIndex )
    {
        Evaluation result = null;

        if ( relativeIndex <= 0 )
        {
            result = _currentEvaluation;
            while ( ( ++relativeIndex < 0 ) && ( result != null ) )
            {
                result = result.getParent();
            }
        }
        return result;
    }

    /**
     * Pushes a new Evaluation onto the stack. This is done before a node evaluates. When evaluation is complete it
     * should be popped from the stack via <code>popEvaluation()</code>.
     */
    public void pushEvaluation( Evaluation value )
    {
        if ( _currentEvaluation != null )
        {
            _currentEvaluation.addChild( value );
        }
        else
        {
            setRootEvaluation( value );
        }
        setCurrentEvaluation( value );
    }

    /**
     * Pops the current Evaluation off of the top of the stack. This is done after a node has completed its evaluation.
     */
    public Evaluation popEvaluation()
    {
        Evaluation result;

        result = _currentEvaluation;
        setCurrentEvaluation( result.getParent() );
        if ( _currentEvaluation == null )
        {
            setLastEvaluation( getKeepLastEvaluation() ? result : null );
            setRootEvaluation( null );
            setCurrentNode( null );
        }
        return result;
    }

    public int incrementLocalReferenceCounter()
    {
        return ++_localReferenceCounter;
    }

    public void addLocalReference( String key, LocalReference reference )
    {
        if ( _localReferenceMap == null )
        {
            _localReferenceMap = new LinkedHashMap<String, LocalReference>();
        }

        _localReferenceMap.put( key, reference );
    }

    public Map<String, LocalReference> getLocalReferences()
    {
        return _localReferenceMap;
    }

    /* ================= Map interface ================= */
    public int size()
    {
        return _values.size();
    }

    public boolean isEmpty()
    {
        return _values.isEmpty();
    }

    public boolean containsKey( Object key )
    {
        return _values.containsKey( key );
    }

    public boolean containsValue( Object value )
    {
        return _values.containsValue( value );
    }

    public Object get( Object key )
    {
        Object result = null;

        // FIXME: complexity is O(n)
        if ( RESERVED_KEYS.contains( key ) )
        {
            if ( THIS_CONTEXT_KEY.equals( key ) )
            {
                result = getCurrentObject();
            }
            else if ( ROOT_CONTEXT_KEY.equals( key ) )
            {
                result = getRoot();
            }
            else if ( CONTEXT_CONTEXT_KEY.equals( key ) )
            {
                result = this;
            }
            else if ( TRACE_EVALUATIONS_CONTEXT_KEY.equals( key ) )
            {
                result = getTraceEvaluations() ? Boolean.TRUE : Boolean.FALSE;
            }
            else if ( LAST_EVALUATION_CONTEXT_KEY.equals( key ) )
            {
                result = getLastEvaluation();
            }
            else if ( KEEP_LAST_EVALUATION_CONTEXT_KEY.equals( key ) )
            {
                result = getKeepLastEvaluation() ? Boolean.TRUE : Boolean.FALSE;
            }
            else if ( CLASS_RESOLVER_CONTEXT_KEY.equals( key ) )
            {
                result = getClassResolver();
            }
            else if ( TYPE_CONVERTER_CONTEXT_KEY.equals( key ) )
            {
                result = getTypeConverter();
            }
            else if ( MEMBER_ACCESS_CONTEXT_KEY.equals( key ) )
            {
                result = getMemberAccess();
            }
        }
        else
        {
            result = _values.get( key );
        }
        return result;
    }

    public Object put( Object key, Object value )
    {
        Object result = null;

        // FIXME: complexity is O(n)
        if ( RESERVED_KEYS.contains( key ) )
        {
            if ( CONTEXT_CONTEXT_KEY.equals( key ) )
            {
                throw new IllegalArgumentException( "can't change " + CONTEXT_CONTEXT_KEY + " in context" );
            }

            if ( THIS_CONTEXT_KEY.equals( key ) )
            {
                result = getCurrentObject();
                setCurrentObject( value );
            }
            else if ( ROOT_CONTEXT_KEY.equals( key ) )
            {
                result = getRoot();
                setRoot( value );
            }
            else if ( TRACE_EVALUATIONS_CONTEXT_KEY.equals( key ) )
            {
                result = getTraceEvaluations() ? Boolean.TRUE : Boolean.FALSE;
                setTraceEvaluations( OgnlOps.booleanValue( value ) );
            }
            else if ( LAST_EVALUATION_CONTEXT_KEY.equals( key ) )
            {
                result = getLastEvaluation();
                _lastEvaluation = (Evaluation) value;
            }
            else if ( KEEP_LAST_EVALUATION_CONTEXT_KEY.equals( key ) )
            {
                result = getKeepLastEvaluation() ? Boolean.TRUE : Boolean.FALSE;
                setKeepLastEvaluation( OgnlOps.booleanValue( value ) );
            }
            else if ( CLASS_RESOLVER_CONTEXT_KEY.equals( key ) )
            {
                result = getClassResolver();
                setClassResolver( (ClassResolver) value );
            }
            else if ( TYPE_CONVERTER_CONTEXT_KEY.equals( key ) )
            {
                result = getTypeConverter();
                setTypeConverter( (TypeConverter) value );
            }
            else if ( MEMBER_ACCESS_CONTEXT_KEY.equals( key ) )
            {
                result = getMemberAccess();
                setMemberAccess( (MemberAccess) value );
            }
        }
        else
        {
            result = _values.put( key, value );
        }

        return result;
    }

    public Object remove( Object key )
    {
        Object result = null;

        // FIXME: complexity is O(n)
        if ( RESERVED_KEYS.contains( key ) )
        {
            if ( CONTEXT_CONTEXT_KEY.equals( key ) || TRACE_EVALUATIONS_CONTEXT_KEY.equals( key )
                || KEEP_LAST_EVALUATION_CONTEXT_KEY.equals( key ) )
            {
                throw new IllegalArgumentException( "can't remove " + key + " from context" );
            }

            if ( THIS_CONTEXT_KEY.equals( key ) )
            {
                result = getCurrentObject();
                setCurrentObject( null );
            }
            else if ( ROOT_CONTEXT_KEY.equals( key ) )
            {
                result = getRoot();
                setRoot( null );
            }
            else if ( LAST_EVALUATION_CONTEXT_KEY.equals( key ) )
            {
                result = _lastEvaluation;
                setLastEvaluation( null );
            }
            else if ( CLASS_RESOLVER_CONTEXT_KEY.equals( key ) )
            {
                result = getClassResolver();
                setClassResolver( null );
            }
            else if ( TYPE_CONVERTER_CONTEXT_KEY.equals( key ) )
            {
                result = getTypeConverter();
                setTypeConverter( null );
            }
            else if ( MEMBER_ACCESS_CONTEXT_KEY.equals( key ) )
            {
                result = getMemberAccess();
                setMemberAccess( null );
            }
        }
        else
        {
            result = _values.remove( key );
        }
        return result;
    }

    public void putAll( Map t )
    {
        for ( Iterator it = t.keySet().iterator(); it.hasNext(); )
        {
            Object k = it.next();

            put( k, t.get( k ) );
        }
    }

    public void clear()
    {
        _values.clear();
        _typeStack.clear();
        _accessorStack.clear();

        _localReferenceCounter = 0;
        if ( _localReferenceMap != null )
        {
            _localReferenceMap.clear();
        }

        setRoot( null );
        setCurrentObject( null );
        setRootEvaluation( null );
        setCurrentEvaluation( null );
        setLastEvaluation( null );
        setCurrentNode( null );
        setClassResolver( DEFAULT_CLASS_RESOLVER );
        setTypeConverter( DEFAULT_TYPE_CONVERTER );
        setMemberAccess( DEFAULT_MEMBER_ACCESS );
    }

    public Set keySet()
    {
        /* Should root, currentObject, classResolver, typeConverter & memberAccess be included here? */
        return _values.keySet();
    }

    public Collection values()
    {
        /* Should root, currentObject, classResolver, typeConverter & memberAccess be included here? */
        return _values.values();
    }

    public Set entrySet()
    {
        /* Should root, currentObject, classResolver, typeConverter & memberAccess be included here? */
        return _values.entrySet();
    }

    @Override
    public boolean equals( Object o )
    {
        return _values.equals( o );
    }

    @Override
    public int hashCode()
    {
        return _values.hashCode();
    }
}
