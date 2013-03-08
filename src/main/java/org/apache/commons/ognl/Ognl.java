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

import org.apache.commons.ognl.enhance.ExpressionAccessor;

import java.io.StringReader;
import java.util.Map;

/**
 * <p>
 * This class provides static methods for parsing and interpreting OGNL expressions.
 * </p>
 * <p>
 * The simplest use of the Ognl class is to get the value of an expression from an object, without extra context or
 * pre-parsing.
 * </p>
 * 
 * <pre>
 * 
 * import org.apache.commons.ognl.Ognl;
 * import org.apache.commons.ognl.OgnlException;
 * ...
 * try
 * {
 *     result = Ognl.getValue( expression, root );
 * }
 * catch ( OgnlException ex )
 * {
 *     // Report error or recover
 * }
 * 
 * </pre>
 * <p>
 * This will parse the expression given and evaluate it against the root object given, returning the result. If there is
 * an error in the expression, such as the property is not found, the exception is encapsulated into an
 * {@link org.apache.commons.ognl.OgnlException OgnlException}.
 * </p>
 * <p>
 * Other more sophisticated uses of Ognl can pre-parse expressions. This provides two advantages: in the case of
 * user-supplied expressions it allows you to catch parse errors before evaluation and it allows you to cache parsed
 * expressions into an AST for better speed during repeated use. The pre-parsed expression is always returned as an
 * <code>Object</code> to simplify use for programs that just wish to store the value for repeated use and do not care
 * that it is an AST. If it does care it can always safely cast the value to an <code>AST</code> type.
 * </p>
 * <p>
 * The Ognl class also takes a <I>context map</I> as one of the parameters to the set and get methods. This allows you
 * to put your own variables into the available namespace for OGNL expressions. The default context contains only the
 * <code>#root</code> and <code>#context</code> keys, which are required to be present. The
 * <code>addDefaultContext(Object, Map)</code> method will alter an existing <code>Map</code> to put the defaults in.
 * Here is an example that shows how to extract the <code>documentName</code> property out of the root object and append
 * a string with the current user name in parens:
 * </p>
 * 
 * <pre>
 * 
 * private Map&lt;String, Object&gt; context = new HashMap&lt;String, Object&gt;();
 * ...
 * public void setUserName( String value )
 * {
 *     context.put("userName", value);
 * }
 * ...
 * try
 * {
 *     // get value using our own custom context map
 *     result = Ognl.getValue( "documentName + \" (\" + ((#userName == null) ? \"&lt;nobody&gt;\" : #userName ) +
 * \")\"", context, root );
 * }
 * catch ( OgnlException ex )
 * {
 *     // Report error or recover
 * }
 * 
 * </pre>
 */
public abstract class Ognl
{

    /**
     * Parses the given OGNL expression and returns a tree representation of the expression that can be used by
     * <code>Ognl</code> static methods.
     * 
     * @param expression the OGNL expression to be parsed
     * @return a tree representation of the expression
     * @throws ExpressionSyntaxException if the expression is malformed
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static Object parseExpression( String expression )
        throws OgnlException
    {
        try
        {
            OgnlParser parser = new OgnlParser( new StringReader( expression ) );
            return parser.topLevelExpression();
        }
        catch ( ParseException e )
        {
            throw new ExpressionSyntaxException( expression, e );
        }
        catch ( TokenMgrError e )
        {
            throw new ExpressionSyntaxException( expression, e );
        }
    }

    /**
     * Parses and compiles the given expression using the {@link org.apache.commons.ognl.enhance.OgnlExpressionCompiler}
     * returned from
     * {@link org.apache.commons.ognl.OgnlRuntime#getCompiler(OgnlContext)}.
     * 
     * @param context The context to use.
     * @param root The root object for the given expression.
     * @param expression The expression to compile.
     * @return The node with a compiled accessor set on {@link org.apache.commons.ognl.Node#getAccessor()} if
     * compilation was successfull.
     *         In instances where compilation wasn't possible because of a partially null expression the
     *         {@link ExpressionAccessor} instance may be null and the compilation of this expression still possible at
     *         some as yet indertermined point in the future.
     * @throws Exception If a compilation error occurs.
     */
    public static Node compileExpression( OgnlContext context, Object root, String expression )
        throws Exception
    {
        Node expr = (Node) Ognl.parseExpression( expression );

        OgnlRuntime.compileExpression( context, expr, root );

        return expr;
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     * 
     * @param root the root of the object graph
     * @return a new Map with the keys <code>root</code> and <code>context</code> set appropriately
     */
    public static Map<String, Object> createDefaultContext( Object root )
    {
        return addDefaultContext( root, null, null, null, new OgnlContext() );
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     * 
     * @param root The root of the object graph.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @return a new OgnlContext with the keys <code>root</code> and <code>context</code> set appropriately
     */
    public static Map<String, Object> createDefaultContext( Object root, ClassResolver classResolver )
    {
        return addDefaultContext( root, classResolver, null, null, new OgnlContext() );
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     * 
     * @param root The root of the object graph.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @param converter Converter used to convert return types of an expression in to their desired types.
     * @return a new Map with the keys <code>root</code> and <code>context</code> set appropriately
     */
    public static Map<String, Object> createDefaultContext( Object root, ClassResolver classResolver,
                                                            TypeConverter converter )
    {
        return addDefaultContext( root, classResolver, converter, null, new OgnlContext() );
    }

    /**
     * Creates and returns a new standard naming context for evaluating an OGNL expression.
     * 
     * @param root The root of the object graph.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @param converter Converter used to convert return types of an expression in to their desired types.
     * @param memberAccess Java security handling object to determine semantics for accessing normally private/protected
     *            methods / fields.
     * @return a new Map with the keys <code>root</code> and <code>context</code> set appropriately
     */
    public static Map<String, Object> createDefaultContext( Object root, ClassResolver classResolver,
                                                            TypeConverter converter, MemberAccess memberAccess )
    {
        return addDefaultContext( root, classResolver, converter, memberAccess, new OgnlContext() );
    }

    /**
     * Appends the standard naming context for evaluating an OGNL expression into the context given so that cached maps
     * can be used as a context.
     * 
     * @param root the root of the object graph
     * @param context the context to which OGNL context will be added.
     * @return Context Map with the keys <code>root</code> and <code>context</code> set appropriately
     */
    public static Map<String, Object> addDefaultContext( Object root, Map<String, Object> context )
    {
        return addDefaultContext( root, null, null, null, context );
    }

    /**
     * Appends the standard naming context for evaluating an OGNL expression into the context given so that cached maps
     * can be used as a context.
     * 
     * @param root The root of the object graph.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @param context The context to which OGNL context will be added.
     * @return Context Map with the keys <code>root</code> and <code>context</code> set appropriately
     */
    public static Map<String, Object> addDefaultContext( Object root, ClassResolver classResolver,
                                                         Map<String, Object> context )
    {
        return addDefaultContext( root, classResolver, null, null, context );
    }

    /**
     * Appends the standard naming context for evaluating an OGNL expression into the context given so that cached maps
     * can be used as a context.
     * 
     * @param root The root of the object graph.
     * @param classResolver The resolver used to instantiate {@link Class} instances referenced in the expression.
     * @param converter Converter used to convert return types of an expression in to their desired types.
     * @param context The context to which OGNL context will be added.
     * @return Context Map with the keys <code>root</code> and <code>context</code> set appropriately
     */
    public static Map<String, Object> addDefaultContext( Object root, ClassResolver classResolver,
                                                         TypeConverter converter, Map<String, Object> context )
    {
        return addDefaultContext( root, classResolver, converter, null, context );
    }

    /**
     * Appends the standard naming context for evaluating an OGNL expression into the context given so that cached maps
     * can be used as a context.
     * 
     * @param root the root of the object graph
     * @param classResolver The class loading resolver that should be used to resolve class references.
     * @param converter The type converter to be used by default.
     * @param memberAccess Definition for handling private/protected access.
     * @param context Default context to use, if not an {@link OgnlContext} will be dumped into a new
     *            {@link OgnlContext} object.
     * @return Context Map with the keys <code>root</code> and <code>context</code> set appropriately
     */
    public static Map<String, Object> addDefaultContext( Object root, ClassResolver classResolver,
                                                         TypeConverter converter, MemberAccess memberAccess,
                                                         Map<String, Object> context )
    {
        OgnlContext result;

        if ( !( context instanceof OgnlContext ) )
        {
            result = new OgnlContext();
            result.setValues( context );
        }
        else
        {
            result = (OgnlContext) context;
        }
        if ( classResolver != null )
        {
            result.setClassResolver( classResolver );
        }
        if ( converter != null )
        {
            result.setTypeConverter( converter );
        }
        if ( memberAccess != null )
        {
            result.setMemberAccess( memberAccess );
        }

        result.setRoot( root );
        return result;
    }

    /**
     * Configures the {@link ClassResolver} to use for the given context. Will be used during expression parsing /
     * execution to resolve class names.
     * 
     * @param context The context to place the resolver.
     * @param classResolver The resolver to use to resolve classes.
     */
    public static void setClassResolver( Map<String, Object> context, ClassResolver classResolver )
    {
        context.put( OgnlContext.CLASS_RESOLVER_CONTEXT_KEY, classResolver );
    }

    /**
     * Gets the previously stored {@link ClassResolver} for the given context - if any.
     * 
     * @param context The context to get the configured resolver from.
     * @return The resolver instance, or null if none found.
     */
    public static ClassResolver getClassResolver( Map<String, Object> context )
    {
        return (ClassResolver) context.get( OgnlContext.CLASS_RESOLVER_CONTEXT_KEY );
    }

    /**
     * Configures the type converter to use for a given context. This will be used to convert into / out of various java
     * class types.
     * 
     * @param context The context to configure it for.
     * @param converter The converter to use.
     */
    public static void setTypeConverter( Map<String, Object> context, TypeConverter converter )
    {
        context.put( OgnlContext.TYPE_CONVERTER_CONTEXT_KEY, converter );
    }

    /**
     * Gets the currently configured {@link TypeConverter} for the given context - if any.
     * 
     * @param context The context to get the converter from.
     * @return The converter - or null if none found.
     */
    public static TypeConverter getTypeConverter( Map<String, Object> context )
    {
        return (TypeConverter) context.get( OgnlContext.TYPE_CONVERTER_CONTEXT_KEY );
    }

    /**
     * Configures the specified context with a {@link MemberAccess} instance for handling field/method protection
     * levels.
     * 
     * @param context The context to configure.
     * @param memberAccess The access resolver to configure the context with.
     */
    public static void setMemberAccess( Map<String, Object> context, MemberAccess memberAccess )
    {
        context.put( OgnlContext.MEMBER_ACCESS_CONTEXT_KEY, memberAccess );
    }

    /**
     * Gets the currently stored {@link MemberAccess} object for the given context - if any.
     * 
     * @param context The context to get the object from.
     * @return The configured {@link MemberAccess} instance in the specified context - or null if none found.
     */
    public static MemberAccess getMemberAccess( Map<String, Object> context )
    {
        return (MemberAccess) context.get( OgnlContext.MEMBER_ACCESS_CONTEXT_KEY );
    }

    /**
     * Sets the root object to use for all expressions in the given context - doesn't necessarily replace root object
     * instances explicitly passed in to other expression resolving methods on this class.
     * 
     * @param context The context to store the root object in.
     * @param root The root object.
     */
    public static void setRoot( Map<String, Object> context, Object root )
    {
        context.put( OgnlContext.ROOT_CONTEXT_KEY, root );
    }

    /**
     * Gets the stored root object for the given context - if any.
     * 
     * @param context The context to get the root object from.
     * @return The root object - or null if none found.
     */
    public static Object getRoot( Map<String, Object> context )
    {
        return context.get( OgnlContext.ROOT_CONTEXT_KEY );
    }

    /**
     * Gets the last {@link Evaluation} executed on the given context.
     * 
     * @param context The context to get the evaluation from.
     * @return The {@link Evaluation} - or null if none was found.
     */
    public static Evaluation getLastEvaluation( Map<String, Object> context )
    {
        return (Evaluation) context.get( OgnlContext.LAST_EVALUATION_CONTEXT_KEY );
    }

    /**
     * Evaluates the given OGNL expression tree to extract a value from the given root object. The default context is
     * set for the given context and root via <code>addDefaultContext()</code>.
     * 
     * @param tree the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param context the naming context for the evaluation
     * @param root the root object for the OGNL expression
     * @return the result of evaluating the expression
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static <T> T getValue( Object tree, Map<String, Object> context, Object root )
        throws OgnlException
    {
        return Ognl.<T> getValue( tree, context, root, null );
    }

    /**
     * Evaluates the given OGNL expression tree to extract a value from the given root object. The default context is
     * set for the given context and root via <code>addDefaultContext()</code>.
     * 
     * @param tree the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param context the naming context for the evaluation
     * @param root the root object for the OGNL expression
     * @param resultType the converted type of the resultant object, using the context's type converter
     * @return the result of evaluating the expression
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    @SuppressWarnings( "unchecked" ) // will cause CCE if types are not compatible
    public static <T> T getValue( Object tree, Map<String, Object> context, Object root, Class<T> resultType )
        throws OgnlException
    {
        T result;
        OgnlContext ognlContext = (OgnlContext) addDefaultContext( root, context );

        Node node = (Node) tree;

        if ( node.getAccessor() != null )
        {
            result = (T) node.getAccessor().get( ognlContext, root );
        }
        else
        {
            result = (T) node.getValue( ognlContext, root );
        }

        if ( resultType != null )
        {
            result = getTypeConverter( context ).convertValue( context, root, null, null, result, resultType );
        }
        return result;
    }

    /**
     * Gets the value represented by the given pre-compiled expression on the specified root object.
     * 
     * @param expression The pre-compiled expression, as found in {@link Node#getAccessor()}.
     * @param context The ognl context.
     * @param root The object to retrieve the expression value from.
     * @return The value.
     */
    @SuppressWarnings( "unchecked" ) // will cause CCE if types are not compatible
    public static <T> T getValue( ExpressionAccessor expression, OgnlContext context, Object root )
    {
        return (T) expression.get( context, root );
    }

    /**
     * Gets the value represented by the given pre-compiled expression on the specified root object.
     * 
     * @param expression The pre-compiled expression, as found in {@link Node#getAccessor()}.
     * @param context The ognl context.
     * @param root The object to retrieve the expression value from.
     * @param resultType The desired object type that the return value should be converted to using the
     *            {@link #getTypeConverter(java.util.Map)} .
     * @return The value.
     */
    public static <T> T getValue( ExpressionAccessor expression, OgnlContext context, Object root, Class<T> resultType )
        throws OgnlException
    {
        return getTypeConverter( context ).convertValue( context, root, null, null, expression.get( context, root ),
                                                         resultType );
    }

    /**
     * Evaluates the given OGNL expression to extract a value from the given root object in a given context
     * 
     * @see #parseExpression(String)
     * @see #getValue(Object,Object)
     * @param expression the OGNL expression to be parsed
     * @param context the naming context for the evaluation
     * @param root the root object for the OGNL expression
     * @return the result of evaluating the expression
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static <T> T getValue( String expression, Map<String, Object> context, Object root )
        throws OgnlException
    {
        return Ognl.<T> getValue( expression, context, root, null );
    }

    /**
     * Evaluates the given OGNL expression to extract a value from the given root object in a given context
     * 
     * @see #parseExpression(String)
     * @see #getValue(Object,Object)
     * @param expression the OGNL expression to be parsed
     * @param context the naming context for the evaluation
     * @param root the root object for the OGNL expression
     * @param resultType the converted type of the resultant object, using the context's type converter
     * @return the result of evaluating the expression
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static <T> T getValue( String expression, Map<String, Object> context, Object root, Class<T> resultType )
        throws OgnlException
    {
        return Ognl.<T> getValue( parseExpression( expression ), context, root, resultType );
    }

    /**
     * Evaluates the given OGNL expression tree to extract a value from the given root object.
     * 
     * @param tree the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param root the root object for the OGNL expression
     * @return the result of evaluating the expression
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static <T> T getValue( Object tree, Object root )
        throws OgnlException
    {
        return Ognl.<T> getValue( tree, root, null );
    }

    /**
     * Evaluates the given OGNL expression tree to extract a value from the given root object.
     * 
     * @param tree the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param root the root object for the OGNL expression
     * @param resultType the converted type of the resultant object, using the context's type converter
     * @return the result of evaluating the expression
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static <T> T getValue( Object tree, Object root, Class<T> resultType )
        throws OgnlException
    {
        return Ognl.<T> getValue( tree, createDefaultContext( root ), root, resultType );
    }

    /**
     * Convenience method that combines calls to <code> parseExpression </code> and <code> getValue</code>.
     * 
     * @see #parseExpression(String)
     * @see #getValue(Object,Object)
     * @param expression the OGNL expression to be parsed
     * @param root the root object for the OGNL expression
     * @return the result of evaluating the expression
     * @throws ExpressionSyntaxException if the expression is malformed
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static <T> T getValue( String expression, Object root )
        throws OgnlException
    {
        return Ognl.<T> getValue( expression, root, null );
    }

    /**
     * Convenience method that combines calls to <code> parseExpression </code> and <code> getValue</code>.
     * 
     * @see #parseExpression(String)
     * @see #getValue(Object,Object)
     * @param expression the OGNL expression to be parsed
     * @param root the root object for the OGNL expression
     * @param resultType the converted type of the resultant object, using the context's type converter
     * @return the result of evaluating the expression
     * @throws ExpressionSyntaxException if the expression is malformed
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static <T> T getValue( String expression, Object root, Class<T> resultType )
        throws OgnlException
    {
        return Ognl.<T> getValue( parseExpression( expression ), root, resultType );
    }

    /**
     * Evaluates the given OGNL expression tree to insert a value into the object graph rooted at the given root object.
     * The default context is set for the given context and root via <code>addDefaultContext()</code>.
     * 
     * @param tree the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param context the naming context for the evaluation
     * @param root the root object for the OGNL expression
     * @param value the value to insert into the object graph
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static void setValue( Object tree, Map<String, Object> context, Object root, Object value )
        throws OgnlException
    {
        OgnlContext ognlContext = (OgnlContext) addDefaultContext( root, context );
        Node n = (Node) tree;

        if ( n.getAccessor() != null )
        {
            n.getAccessor().set( ognlContext, root, value );
            return;
        }

        n.setValue( ognlContext, root, value );
    }

    /**
     * Sets the value given using the pre-compiled expression on the specified root object.
     * 
     * @param expression The pre-compiled expression, as found in {@link Node#getAccessor()}.
     * @param context The ognl context.
     * @param root The object to set the expression value on.
     * @param value The value to set.
     */
    public static void setValue( ExpressionAccessor expression, OgnlContext context, Object root, Object value )
    {
        expression.set( context, root, value );
    }

    /**
     * Evaluates the given OGNL expression to insert a value into the object graph rooted at the given root object given
     * the context.
     * 
     * @param expression the OGNL expression to be parsed
     * @param root the root object for the OGNL expression
     * @param context the naming context for the evaluation
     * @param value the value to insert into the object graph
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static void setValue( String expression, Map<String, Object> context, Object root, Object value )
        throws OgnlException
    {
        setValue( parseExpression( expression ), context, root, value );
    }

    /**
     * Evaluates the given OGNL expression tree to insert a value into the object graph rooted at the given root object.
     * 
     * @param tree the OGNL expression tree to evaluate, as returned by parseExpression()
     * @param root the root object for the OGNL expression
     * @param value the value to insert into the object graph
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static void setValue( Object tree, Object root, Object value )
        throws OgnlException
    {
        setValue( tree, createDefaultContext( root ), root, value );
    }

    /**
     * Convenience method that combines calls to <code> parseExpression </code> and <code> setValue</code>.
     * 
     * @see #parseExpression(String)
     * @see #setValue(Object,Object,Object)
     * @param expression the OGNL expression to be parsed
     * @param root the root object for the OGNL expression
     * @param value the value to insert into the object graph
     * @throws ExpressionSyntaxException if the expression is malformed
     * @throws MethodFailedException if the expression called a method which failed
     * @throws NoSuchPropertyException if the expression referred to a nonexistent property
     * @throws InappropriateExpressionException if the expression can't be used in this context
     * @throws OgnlException if there is a pathological environmental problem
     */
    public static void setValue( String expression, Object root, Object value )
        throws OgnlException
    {
        setValue( parseExpression( expression ), root, value );
    }

    /**
     * Checks if the specified {@link Node} instance represents a constant expression.
     * 
     * @param tree The {@link Node} to check.
     * @param context The context to use.
     * @return True if the node is a constant - false otherwise.
     * @throws OgnlException If an error occurs checking the expression.
     */
    public static boolean isConstant( Object tree, Map<String, Object> context )
        throws OgnlException
    {
        return ( (SimpleNode) tree ).isConstant( (OgnlContext) addDefaultContext( null, context ) );
    }

    /**
     * Checks if the specified expression represents a constant expression.
     * 
     * @param expression The expression to check.
     * @param context The context to use.
     * @return True if the node is a constant - false otherwise.
     * @throws OgnlException If an error occurs checking the expression.
     */
    public static boolean isConstant( String expression, Map<String, Object> context )
        throws OgnlException
    {
        return isConstant( parseExpression( expression ), context );
    }

    /**
     * Same as {@link #isConstant(Object, java.util.Map)} - only the {@link Map} context is created for you.
     * 
     * @param tree The {@link Node} to check.
     * @return True if the node represents a constant expression - false otherwise.
     * @throws OgnlException If an exception occurs.
     */
    public static boolean isConstant( Object tree )
        throws OgnlException
    {
        return isConstant( tree, createDefaultContext( null ) );
    }

    /**
     * Same as {@link #isConstant(String, java.util.Map)} - only the {@link Map} instance is created for you.
     * 
     * @param expression The expression to check.
     * @return True if the expression represents a constant - false otherwise.
     * @throws OgnlException If an exception occurs.
     */
    public static boolean isConstant( String expression )
        throws OgnlException
    {
        return isConstant( parseExpression( expression ), createDefaultContext( null ) );
    }

    public static boolean isSimpleProperty( Object tree, Map<String, Object> context )
        throws OgnlException
    {
        return ( (SimpleNode) tree ).isSimpleProperty( (OgnlContext) addDefaultContext( null, context ) );
    }

    public static boolean isSimpleProperty( String expression, Map<String, Object> context )
        throws OgnlException
    {
        return isSimpleProperty( parseExpression( expression ), context );
    }

    public static boolean isSimpleProperty( Object tree )
        throws OgnlException
    {
        return isSimpleProperty( tree, createDefaultContext( null ) );
    }

    public static boolean isSimpleProperty( String expression )
        throws OgnlException
    {
        return isSimpleProperty( parseExpression( expression ), createDefaultContext( null ) );
    }

    public static boolean isSimpleNavigationChain( Object tree, Map<String, Object> context )
        throws OgnlException
    {
        return ( (SimpleNode) tree ).isSimpleNavigationChain( (OgnlContext) addDefaultContext( null, context ) );
    }

    public static boolean isSimpleNavigationChain( String expression, Map<String, Object> context )
        throws OgnlException
    {
        return isSimpleNavigationChain( parseExpression( expression ), context );
    }

    public static boolean isSimpleNavigationChain( Object tree )
        throws OgnlException
    {
        return isSimpleNavigationChain( tree, createDefaultContext( null ) );
    }

    public static boolean isSimpleNavigationChain( String expression )
        throws OgnlException
    {
        return isSimpleNavigationChain( parseExpression( expression ), createDefaultContext( null ) );
    }

    /** You can't make one of these. */
    private Ognl()
    {
    }
}
