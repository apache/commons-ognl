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

package org.apache.commons.ognl.enhance;

import org.apache.commons.ognl.Node;
import org.apache.commons.ognl.OgnlContext;

import java.lang.reflect.Method;

/**
 * Core interface implemented by expression compiler instances.
 */
public interface OgnlExpressionCompiler
{

    /** Static constant used in conjunction with {@link OgnlContext} to store temporary references. */
    String ROOT_TYPE = "-ognl-root-type";

    /**
     * The core method executed to compile a specific expression. It is expected that this expression always return a
     * {@link Node} with a non null {@link org.apache.commons.ognl.Node#getAccessor()} instance - unless an exception is
     * thrown by the method or the statement wasn't compilable in this instance because of missing/null objects in the
     * expression. These instances may in some cases continue to call this compilation method until the expression is
     * resolvable.
     *
     * @param context The context of execution.
     * @param expression The pre-parsed root expression node to compile.
     * @param root The root object for the expression - may be null in many instances so some implementations may exit
     * @throws Exception If an error occurs compiling the expression and no strategy has been implemented to handle
     *             incremental expression compilation for incomplete expression members.
     */
    void compileExpression( OgnlContext context, Node expression, Object root )
        throws Exception;

    /**
     * Gets a javassist safe class string for the given class instance. This is especially useful for handling array vs.
     * normal class casting strings.
     *
     * @param clazz The class to get a string equivalent javassist compatible string reference for.
     * @return The string equivalent of the class.
     */
    String getClassName( Class<?> clazz );

    /**
     * Used in places where the preferred {@link #getSuperOrInterfaceClass(java.lang.reflect.Method, Class)} isn't
     * possible because the method isn't known for a class. Attempts to upcast the given class to the next available
     * non-private accessible class so that compiled expressions can reference the interface class of an instance so as
     * not to be compiled in to overly specific statements.
     *
     * @param clazz The class to attempt to find a compatible interface for.
     * @return The same class if no higher level interface could be matched against or the interface equivalent class.
     */
    Class<?> getInterfaceClass( Class<?> clazz );

    /**
     * For the given {@link Method} and class finds the highest level interface class this combination can be cast to.
     *
     * @param m The method the class must implement.
     * @param clazz The current class being worked with.
     * @return The highest level interface / class that the referenced {@link Method} is declared in.
     */
    Class<?> getSuperOrInterfaceClass( Method m, Class<?> clazz );

    /**
     * For a given root object type returns the base class type to be used in root referenced expressions. This helps in
     * some instances where the root objects themselves are compiled javassist instances that need more generic class
     * equivalents to cast to.
     *
     * @param rootNode The root expression node.
     * @param context The current execution context.
     * @return The root expression class type to cast to for this node.
     */
    Class<?> getRootExpressionClass( Node rootNode, OgnlContext context );

    /**
     * Used primarily by AST types like {@link org.apache.commons.ognl.ASTChain} where <code>foo.bar.id</code> type
     * references may need to be cast multiple times in order to properly resolve the members in a compiled statement.
     * <p>
     * This method should be using the various {@link org.apache.commons.ognl.OgnlContext#getCurrentType()} /
     * {@link org.apache.commons.ognl.OgnlContext#getCurrentAccessor()} methods to inspect the type stack and properly
     * cast to the right classes - but only when necessary.
     * </p>
     *
     * @param context The current execution context.
     * @param expression The node being checked for casting.
     * @param body The Java source string generated by the given node.
     * @return The body string parameter plus any additional casting syntax needed to make the expression resolvable.
     */
    String castExpression( OgnlContext context, Node expression, String body );

    /**
     * Method is used for expressions where multiple inner parameter method calls in generated Java source strings cause
     * javassit failures. It is hacky and cumbersome to have to generate expressions this way but it's the only current
     * known way to make javassist happy.
     * <p>
     * Takes an expression block generated by a node and creates a new method on the base object being compiled so that
     * sufficiently complicated sub expression blocks can be broken out in to distinct methods to be referenced by the
     * core accessor / setter methods in the base compiled root object.
     * </p>
     *
     * @param context The current execution context.
     * @param expression The Java source expression to dump in to a seperate method reference.
     * @param type The return type that should be specified for the new method.
     * @return The method name that will be used to reference the sub expression in place of the actual sub expression
     *         itself.
     */
    String createLocalReference( OgnlContext context, String expression, Class<?> type );
}
