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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.ognl.enhance.LocalReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * This class defines the execution context for an OGNL expression
 */
public class OgnlContext
    implements Map<String, Object>
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

    private static boolean defaultTraceEvaluations = false;

    private static boolean defaultKeepLastEvaluation = false;

    public static final DefaultClassResolver DEFAULT_CLASS_RESOLVER = new DefaultClassResolver();

    public static final TypeConverter DEFAULT_TYPE_CONVERTER = new DefaultTypeConverter();

    public static final MemberAccess DEFAULT_MEMBER_ACCESS = new DefaultMemberAccess( false );

    private static final Set<String> RESERVED_KEYS = new HashSet<String>( 11 );

    private Object root;

    private Object currentObject;

    private Node currentNode;

    private boolean traceEvaluations = defaultTraceEvaluations;

    private Evaluation rootEvaluation;

    private Evaluation currentEvaluation;

    private Evaluation lastEvaluation;

    private boolean keepLastEvaluation = defaultKeepLastEvaluation;

    private Map<String, Object> values = new HashMap<String, Object>( 23 );

    private ClassResolver classResolver = DEFAULT_CLASS_RESOLVER;

    private TypeConverter typeConverter = DEFAULT_TYPE_CONVERTER;

    private MemberAccess memberAccess = DEFAULT_MEMBER_ACCESS;

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
            s = System.getProperty( PROPERTY_KEY_PREFIX + ".traceEvaluations" );
            if ( s != null )
            {
                defaultTraceEvaluations = Boolean.valueOf( s.trim() );
            }
            s = System.getProperty( PROPERTY_KEY_PREFIX + ".keepLastEvaluation" );
            if ( s != null )
            {
                defaultKeepLastEvaluation = Boolean.valueOf( s.trim() );
            }
        }
        catch ( SecurityException ex )
        {
            // restricted access environment, just keep defaults
        }
    }

    private Stack<Class<?>> typeStack = new Stack<Class<?>>();

    private Stack<Class<?>> accessorStack = new Stack<Class<?>>();

    private int localReferenceCounter = 0;

    private Map<String, LocalReference> localReferenceMap = null;

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
            this.classResolver = classResolver;
        }
        if ( typeConverter != null )
        {
            this.typeConverter = typeConverter;
        }
        if ( memberAccess != null )
        {
            this.memberAccess = memberAccess;
        }
    }

    public OgnlContext( Map<String, Object> values )
    {
        super();
        this.values = values;
    }

    public OgnlContext( ClassResolver classResolver, TypeConverter typeConverter, MemberAccess memberAccess,
                        Map<String, Object> values )
    {
        this( classResolver, typeConverter, memberAccess );
        this.values = values;
    }

    public void setValues( Map<String, Object> value )
    {
        values.putAll( value );
    }

    public Map<String, Object> getValues()
    {
        return values;
    }

    public void setClassResolver( ClassResolver value )
    {
        if ( value == null )
        {
            throw new IllegalArgumentException( "cannot set ClassResolver to null" );
        }
        classResolver = value;
    }

    public ClassResolver getClassResolver()
    {
        return classResolver;
    }

    public void setTypeConverter( TypeConverter value )
    {
        if ( value == null )
        {
            throw new IllegalArgumentException( "cannot set TypeConverter to null" );
        }
        typeConverter = value;
    }

    public TypeConverter getTypeConverter()
    {
        return typeConverter;
    }

    public void setMemberAccess( MemberAccess value )
    {
        if ( value == null )
        {
            throw new IllegalArgumentException( "cannot set MemberAccess to null" );
        }
        memberAccess = value;
    }

    public MemberAccess getMemberAccess()
    {
        return memberAccess;
    }

    public void setRoot( Object value )
    {
        root = value;
        accessorStack.clear();
        typeStack.clear();
        currentObject = value;

        if ( currentObject != null )
        {
            setCurrentType( currentObject.getClass() );
        }
    }

    public Object getRoot()
    {
        return root;
    }

    public boolean getTraceEvaluations()
    {
        return traceEvaluations;
    }

    public void setTraceEvaluations( boolean value )
    {
        traceEvaluations = value;
    }

    public Evaluation getLastEvaluation()
    {
        return lastEvaluation;
    }

    public void setLastEvaluation( Evaluation value )
    {
        lastEvaluation = value;
    }

    /**
     * This method can be called when the last evaluation has been used and can be returned for reuse in the free pool
     * maintained by the runtime. This is not a necessary step, but is useful for keeping memory usage down. This will
     * recycle the last evaluation and then set the last evaluation to null.
     */
    public void recycleLastEvaluation()
    {
        lastEvaluation = null;
    }

    /**
     * Returns true if the last evaluation that was done on this context is retained and available through
     * <code>getLastEvaluation()</code>. The default is true.
     */
    public boolean getKeepLastEvaluation()
    {
        return keepLastEvaluation;
    }

    /**
     * Sets whether the last evaluation that was done on this context is retained and available through
     * <code>getLastEvaluation()</code>. The default is true.
     */
    public void setKeepLastEvaluation( boolean value )
    {
        keepLastEvaluation = value;
    }

    public void setCurrentObject( Object value )
    {
        currentObject = value;
    }

    public Object getCurrentObject()
    {
        return currentObject;
    }

    public void setCurrentAccessor( Class<?> type )
    {
        accessorStack.add( type );
    }

    public Class<?> getCurrentAccessor()
    {
        if ( accessorStack.isEmpty() )
        {
            return null;
        }

        return accessorStack.peek();
    }

    public Class<?> getPreviousAccessor()
    {
        if ( accessorStack.isEmpty() )
        {
            return null;
        }

        if ( accessorStack.size() > 1 )
        {
            return accessorStack.get( accessorStack.size() - 2 );
        }

        return null;
    }

    public Class<?> getFirstAccessor()
    {
        if ( accessorStack.isEmpty() )
        {
            return null;
        }

        return accessorStack.get( 0 );
    }

    /**
     * Gets the current class type being evaluated on the stack, as set by {@link #setCurrentType(Class)}.
     * 
     * @return The current object type, may be null.
     */
    public Class<?> getCurrentType()
    {
        if ( typeStack.isEmpty() )
        {
            return null;
        }

        return typeStack.peek();
    }

    public void setCurrentType( Class<?> type )
    {
        typeStack.add( type );
    }

    /**
     * Represents the last known object type on the evaluation stack, will be the value of the last known
     * {@link #getCurrentType()}.
     * 
     * @return The previous type of object on the stack, may be null.
     */
    public Class<?> getPreviousType()
    {
        if ( typeStack.isEmpty() )
        {
            return null;
        }

        if ( typeStack.size() > 1 )
        {
            return typeStack.get( typeStack.size() - 2 );
        }

        return null;
    }

    public void setPreviousType( Class<?> type )
    {
        if ( typeStack.isEmpty() || typeStack.size() < 2 )
        {
            return;
        }

        typeStack.set( typeStack.size() - 2, type );
    }

    public Class<?> getFirstType()
    {
        if ( typeStack.isEmpty() )
        {
            return null;
        }

        return typeStack.get( 0 );
    }

    public void setCurrentNode( Node value )
    {
        currentNode = value;
    }

    public Node getCurrentNode()
    {
        return currentNode;
    }

    /**
     * Gets the current Evaluation from the top of the stack. This is the Evaluation that is in process of evaluating.
     */
    public Evaluation getCurrentEvaluation()
    {
        return currentEvaluation;
    }

    public void setCurrentEvaluation( Evaluation value )
    {
        currentEvaluation = value;
    }

    /**
     * Gets the root of the evaluation stack. This Evaluation contains the node representing the root expression and the
     * source is the root source object.
     */
    public Evaluation getRootEvaluation()
    {
        return rootEvaluation;
    }

    public void setRootEvaluation( Evaluation value )
    {
        rootEvaluation = value;
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
            result = currentEvaluation;
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
        if ( currentEvaluation != null )
        {
            currentEvaluation.addChild( value );
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

        result = currentEvaluation;
        setCurrentEvaluation( result.getParent() );
        if ( currentEvaluation == null )
        {
            setLastEvaluation( getKeepLastEvaluation() ? result : null );
            setRootEvaluation( null );
            setCurrentNode( null );
        }
        return result;
    }

    public int incrementLocalReferenceCounter()
    {
        return ++localReferenceCounter;
    }

    public void addLocalReference( String key, LocalReference reference )
    {
        if ( localReferenceMap == null )
        {
            localReferenceMap = new LinkedHashMap<String, LocalReference>();
        }

        localReferenceMap.put( key, reference );
    }

    public Map<String, LocalReference> getLocalReferences()
    {
        return localReferenceMap;
    }

    /* ================= Map interface ================= */
    public int size()
    {
        return values.size();
    }

    public boolean isEmpty()
    {
        return values.isEmpty();
    }

    public boolean containsKey( Object key )
    {
        return values.containsKey( key );
    }

    public boolean containsValue( Object value )
    {
        return values.containsValue( value );
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
            result = values.get( key );
        }
        return result;
    }

    public Object put( String key, Object value )
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
                lastEvaluation = (Evaluation) value;
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
            result = values.put( key, value );
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
                result = lastEvaluation;
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
            result = values.remove( key );
        }
        return result;
    }

    public void putAll( Map<? extends String, ?> t )
    {
        for ( Entry<? extends String, ?> entry : t.entrySet() )
        {
            put( entry.getKey(), entry.getValue() );
        }
    }

    public void clear()
    {
        values.clear();
        typeStack.clear();
        accessorStack.clear();

        localReferenceCounter = 0;
        if ( localReferenceMap != null )
        {
            localReferenceMap.clear();
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

    public Set<String> keySet()
    {
        /* Should root, currentObject, classResolver, typeConverter & memberAccess be included here? */
        return values.keySet();
    }

    public Collection<Object> values()
    {
        /* Should root, currentObject, classResolver, typeConverter & memberAccess be included here? */
        return values.values();
    }

    public Set<Entry<String, Object>> entrySet()
    {
        /* Should root, currentObject, classResolver, typeConverter & memberAccess be included here? */
        return values.entrySet();
    }

    @Override
    public boolean equals( Object o )
    {
        return values.equals( o );
    }

    @Override
    public int hashCode()
    {
        return values.hashCode();
    }
}
