package org.apache.commons.ognl.enhance;

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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.apache.commons.ognl.ASTAnd;
import org.apache.commons.ognl.ASTChain;
import org.apache.commons.ognl.ASTConst;
import org.apache.commons.ognl.ASTCtor;
import org.apache.commons.ognl.ASTList;
import org.apache.commons.ognl.ASTMethod;
import org.apache.commons.ognl.ASTOr;
import org.apache.commons.ognl.ASTProperty;
import org.apache.commons.ognl.ASTRootVarRef;
import org.apache.commons.ognl.ASTStaticField;
import org.apache.commons.ognl.ASTStaticMethod;
import org.apache.commons.ognl.ASTThisVarRef;
import org.apache.commons.ognl.ASTVarRef;
import org.apache.commons.ognl.ClassResolver;
import org.apache.commons.ognl.ExpressionNode;
import org.apache.commons.ognl.Node;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.OgnlRuntime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

/**
 * Responsible for managing/providing functionality related to compiling generated java source expressions via bytecode
 * enhancements for a given ognl expression.
 */
public class ExpressionCompiler
    implements OgnlExpressionCompiler
{

    /**
     * Key used to store any java source string casting statements in the {@link OgnlContext} during class compilation.
     */
    public static final String PRE_CAST = "_preCast";

    /**
     * {@link ClassLoader} instances.
     */
    protected Map<ClassResolver, EnhancedClassLoader> loaders = new HashMap<ClassResolver, EnhancedClassLoader>();

    /**
     * Javassist class definition poool.
     */
    protected ClassPool pool;

    protected int classCounter = 0;

    /**
     * Used by {@link #castExpression(org.apache.commons.ognl.OgnlContext, org.apache.commons.ognl.Node, String)} to
     * store the cast java source string in to the current {@link org.apache.commons.ognl.OgnlContext}. This will either
     * add to the existing string present if it already exists or create a new instance and store it using the static
     * key of {@link #PRE_CAST}.
     *
     * @param context The current execution context.
     * @param cast The java source string to store in to the context.
     */
    public static void addCastString( OgnlContext context, String cast )
    {
        String value = (String) context.get( PRE_CAST );

        if ( value != null )
        {
            value = cast + value;
        }
        else
        {
            value = cast;
        }

        context.put( PRE_CAST, value );
    }

    /**
     * Returns the appropriate casting expression (minus parens) for the specified class type.
     * <p/>
     * For instance, if given an {@link Integer} object the string <code>"java.lang.Integer"</code> would be returned.
     * For an array of primitive ints <code>"int[]"</code> and so on..
     * </p>
     *
     * @param type The class to cast a string expression for.
     * @return The converted raw string version of the class name.
     */
    public static String getCastString( Class<?> type )
    {
        if ( type == null )
        {
            return null;
        }

        return type.isArray() ? type.getComponentType().getName() + "[]" : type.getName();
    }

    /**
     * Convenience method called by many different property/method resolving AST types to get a root expression
     * resolving string for the given node. The callers are mostly ignorant and rely on this method to properly
     * determine if the expression should be cast at all and take the appropriate actions if it should.
     *
     * @param expression The node to check and generate a root expression to if necessary.
     * @param root The root object for this execution.
     * @param context The current execution context.
     * @return Either an empty string or a root path java source string compatible with javassist compilations from the
     *         root object up to the specified {@link Node}.
     */
    public static String getRootExpression( Node expression, Object root, OgnlContext context )
    {
        String rootExpr = "";

        if ( !shouldCast( expression ) )
        {
            return rootExpr;
        }

        if ( ( !(expression instanceof ASTList) && !(expression instanceof ASTVarRef)
            && !(expression instanceof ASTStaticMethod) && !(expression instanceof ASTStaticField)
            && !(expression instanceof ASTConst) && !(expression instanceof ExpressionNode)
            && !(expression instanceof ASTCtor)
            && root != null ) || ( root != null && expression instanceof ASTRootVarRef) )
        {

            Class<?> castClass = OgnlRuntime.getCompiler( context ).getRootExpressionClass( expression, context );

            if ( castClass.isArray() || expression instanceof ASTRootVarRef || expression instanceof ASTThisVarRef)
            {
                rootExpr = "((" + getCastString( castClass ) + ")$2)";

                if ( expression instanceof ASTProperty && !( (ASTProperty) expression ).isIndexedAccess() )
                {
                    rootExpr += ".";
                }
            }
            else if ( ( expression instanceof ASTProperty && ( (ASTProperty) expression ).isIndexedAccess() )
                || expression instanceof ASTChain)
            {
                rootExpr = "((" + getCastString( castClass ) + ")$2)";
            }
            else
            {
                rootExpr = "((" + getCastString( castClass ) + ")$2).";
            }
        }

        return rootExpr;
    }

    /**
     * Used by {@link #getRootExpression(org.apache.commons.ognl.Node, Object, org.apache.commons.ognl.OgnlContext)} to
     * determine if the expression needs to be cast at all.
     *
     * @param expression The node to check against.
     * @return Yes if the node type should be cast - false otherwise.
     */
    public static boolean shouldCast( Node expression )
    {
        if (expression instanceof ASTChain)
        {
            Node child = expression.jjtGetChild( 0 );
            if ( child instanceof ASTConst || child instanceof ASTStaticMethod
                || child instanceof ASTStaticField || ( child instanceof ASTVarRef
                && !(child instanceof ASTRootVarRef)) )
            {
                return false;
            }
        }

        return !(expression instanceof ASTConst);
    }

    /**
     * {@inheritDoc}
     */
    public String castExpression( OgnlContext context, Node expression, String body )
    {
        //TODO: ok - so this looks really f-ed up ...and it is ..eh if you can do it better I'm all for it :)

        if ( context.getCurrentAccessor() == null || context.getPreviousType() == null
            || context.getCurrentAccessor().isAssignableFrom( context.getPreviousType() ) || (
            context.getCurrentType() != null && context.getCurrentObject() != null
                && context.getCurrentType().isAssignableFrom( context.getCurrentObject().getClass() )
                && context.getCurrentAccessor().isAssignableFrom( context.getPreviousType() ) ) || body == null
            || body.trim().length() < 1 || ( context.getCurrentType() != null && context.getCurrentType().isArray() && (
            context.getPreviousType() == null || context.getPreviousType() != Object.class ) )
            || expression instanceof ASTOr || expression instanceof ASTAnd
            || expression instanceof ASTRootVarRef || context.getCurrentAccessor() == Class.class || (
            context.get( ExpressionCompiler.PRE_CAST ) != null && ( (String) context.get(
                ExpressionCompiler.PRE_CAST ) ).startsWith( "new" ) ) || expression instanceof ASTStaticField
            || expression instanceof ASTStaticMethod || ( expression instanceof OrderedReturn
            && ( (OrderedReturn) expression ).getLastExpression() != null ) )
        {
            return body;
        }

        /*
         * System.out.println("castExpression() with expression " + expression + " expr class: " + expression.getClass()
         * + " currentType is: " + context.getCurrentType() + " previousType: " + context.getPreviousType() +
         * "\n current Accessor: " + context.getCurrentAccessor() + " previous Accessor: " +
         * context.getPreviousAccessor() + " current object " + context.getCurrentObject());
         */

        ExpressionCompiler.addCastString( context,
                                          "((" + ExpressionCompiler.getCastString( context.getCurrentAccessor() )
                                              + ")" );

        return ")" + body;
    }

    /**
     * {@inheritDoc}
     */
    public String getClassName( Class<?> clazz )
    {
        if ( "java.util.AbstractList$Itr".equals( clazz.getName() ) )
        {
            return Iterator.class.getName();
        }

        if ( Modifier.isPublic( clazz.getModifiers() ) && clazz.isInterface() )
        {
            return clazz.getName();
        }

        Class<?>[] interfaces = clazz.getInterfaces();

        for ( Class<?> intface : interfaces )
        {
            if ( intface.getName().indexOf( "util.List" ) > 0 )
            {
                return intface.getName();
            }
            if ( intface.getName().indexOf( "Iterator" ) > 0 )
            {
                return intface.getName();
            }
        }

        if ( clazz.getSuperclass() != null && clazz.getSuperclass().getInterfaces().length > 0 )
        {
            return getClassName( clazz.getSuperclass() );
        }

        return clazz.getName();
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getSuperOrInterfaceClass( Method m, Class<?> clazz )
    {
        if ( clazz.getInterfaces() != null && clazz.getInterfaces().length > 0 )
        {
            Class<?>[] intfs = clazz.getInterfaces();
            Class<?> intClass;

            for ( Class<?> intf : intfs )
            {
                intClass = getSuperOrInterfaceClass( m, intf );

                if ( intClass != null )
                {
                    return intClass;
                }

                if ( Modifier.isPublic( intf.getModifiers() ) && containsMethod( m, intf ) )
                {
                    return intf;
                }
            }
        }

        if ( clazz.getSuperclass() != null )
        {
            Class<?> superClass = getSuperOrInterfaceClass( m, clazz.getSuperclass() );

            if ( superClass != null )
            {
                return superClass;
            }
        }

        if ( Modifier.isPublic( clazz.getModifiers() ) && containsMethod( m, clazz ) )
        {
            return clazz;
        }

        return null;
    }

    /**
     * Helper utility method used by compiler to help resolve class->method mappings during method calls to
     * {@link OgnlExpressionCompiler#getSuperOrInterfaceClass(java.lang.reflect.Method, Class)}.
     *
     * @param m The method to check for existance of.
     * @param clazz The class to check for the existance of a matching method definition to the method passed in.
     * @return True if the class contains the specified method, false otherwise.
     */
    public boolean containsMethod( Method m, Class<?> clazz )
    {
        Method[] methods = clazz.getMethods();

        for ( Method method : methods )
        {
            if ( method.getName().equals( m.getName() ) && method.getReturnType() == m.getReturnType() )
            {
                Class<?>[] parms = m.getParameterTypes();

                Class<?>[] mparms = method.getParameterTypes();
                if (mparms.length != parms.length)
                {
                    continue;
                }

                boolean parmsMatch = true;
                for ( int p = 0; p < parms.length; p++ )
                {
                    if ( parms[p] != mparms[p] )
                    {
                        parmsMatch = false;
                        break;
                    }
                }

                if ( !parmsMatch )
                {
                    continue;
                }

                Class<?>[] exceptions = m.getExceptionTypes();

                Class<?>[] mexceptions = method.getExceptionTypes();
                if (mexceptions.length != exceptions.length)
                {
                    continue;
                }

                boolean exceptionsMatch = true;
                for ( int e = 0; e < exceptions.length; e++ )
                {
                    if ( exceptions[e] != mexceptions[e] )
                    {
                        exceptionsMatch = false;
                        break;
                    }
                }

                if ( !exceptionsMatch )
                {
                    continue;
                }

                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getInterfaceClass( Class<?> clazz )
    {
        if ( "java.util.AbstractList$Itr".equals( clazz.getName() ) )
        {
            return Iterator.class;
        }

        if ( Modifier.isPublic( clazz.getModifiers() ) && clazz.isInterface() || clazz.isPrimitive() )
        {
            return clazz;
        }

        Class<?>[] intf = clazz.getInterfaces();

        for ( Class<?> anIntf : intf )
        {
            if ( List.class.isAssignableFrom( anIntf ) )
            {
                return List.class;
            }
            if ( Iterator.class.isAssignableFrom( anIntf ) )
            {
                return Iterator.class;
            }
            if ( Map.class.isAssignableFrom( anIntf ) )
            {
                return Map.class;
            }
            if ( Set.class.isAssignableFrom( anIntf ) )
            {
                return Set.class;
            }
            if ( Collection.class.isAssignableFrom( anIntf ) )
            {
                return Collection.class;
            }
        }

        if ( clazz.getSuperclass() != null && clazz.getSuperclass().getInterfaces().length > 0 )
        {
            return getInterfaceClass( clazz.getSuperclass() );
        }

        return clazz;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getRootExpressionClass( Node rootNode, OgnlContext context )
    {
        if ( context.getRoot() == null )
        {
            return null;
        }

        Class<?> ret = context.getRoot().getClass();

        if ( context.getFirstAccessor() != null && context.getFirstAccessor().isInstance( context.getRoot() ) )
        {
            ret = context.getFirstAccessor();
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public void compileExpression( OgnlContext context, Node expression, Object root )
        throws Exception
    {
        // System.out.println("Compiling expr class " + expression.getClass().getName() + " and root " + root);

        if ( expression.getAccessor() != null )
        {
            return;
        }

        String getBody, setBody;

        EnhancedClassLoader loader = getClassLoader( context );
        ClassPool classPool = getClassPool( context, loader );

        CtClass newClass = classPool.makeClass(
            expression.getClass().getName() + expression.hashCode() + classCounter++ + "Accessor" );
        newClass.addInterface( getCtClass( ExpressionAccessor.class ) );

        CtClass ognlClass = getCtClass( OgnlContext.class );
        CtClass objClass = getCtClass( Object.class );

        CtMethod valueGetter = new CtMethod( objClass, "get", new CtClass[] { ognlClass, objClass }, newClass );
        CtMethod valueSetter =
            new CtMethod( CtClass.voidType, "set", new CtClass[] { ognlClass, objClass, objClass }, newClass );

        CtField nodeMember = null; // will only be set if uncompilable exception is thrown

        CtClass nodeClass = getCtClass( Node.class );
        CtMethod setExpression = null;

        try
        {

            getBody = generateGetter( context, newClass, objClass, classPool, valueGetter, expression, root );

        }
        catch ( UnsupportedCompilationException uc )
        {
            nodeMember = new CtField( nodeClass, "_node", newClass );
            newClass.addField( nodeMember );

            getBody = generateOgnlGetter( newClass, valueGetter, nodeMember );

            setExpression = CtNewMethod.setter( "setExpression", nodeMember );
            newClass.addMethod( setExpression );
        }

        try
        {

            setBody = generateSetter( context, newClass, objClass, classPool, valueSetter, expression, root );

        }
        catch ( UnsupportedCompilationException uc )
        {
            if ( nodeMember == null )
            {
                nodeMember = new CtField( nodeClass, "_node", newClass );
                newClass.addField( nodeMember );
            }

            setBody = generateOgnlSetter( newClass, valueSetter, nodeMember );

            if ( setExpression == null )
            {
                setExpression = CtNewMethod.setter( "setExpression", nodeMember );
                newClass.addMethod( setExpression );
            }
        }

        try
        {
            newClass.addConstructor( CtNewConstructor.defaultConstructor( newClass ) );

            Class<?> clazz = classPool.toClass( newClass );
            newClass.detach();

            expression.setAccessor( (ExpressionAccessor) clazz.newInstance() );

            // need to set expression on node if the field was just defined.

            if ( nodeMember != null )
            {
                expression.getAccessor().setExpression( expression );
            }

        }
        catch ( Throwable t )
        {
            throw new RuntimeException( "Error compiling expression on object " + root + " with expression node "
                + expression + " getter body: " + getBody + " setter body: " + setBody, t );
        }

    }

    protected String generateGetter( OgnlContext context, CtClass newClass, CtClass objClass, ClassPool classPool,
                                     CtMethod valueGetter, Node expression, Object root )
        throws Exception
    {
        String pre = "";
        String post = "";
        String body;

        context.setRoot( root );

        // the ExpressionAccessor API has to reference the generic Object class for get/set operations, so this sets up
        // that known
        // type beforehand

        context.remove( PRE_CAST );

        // Recursively generate the java source code representation of the top level expression

        String getterCode = expression.toGetSourceString( context, root );

        if ( getterCode == null || getterCode.trim().isEmpty()
            && !ASTVarRef.class.isAssignableFrom( expression.getClass() ) )
        {
            getterCode = "null";
        }

        String castExpression = (String) context.get( PRE_CAST );

        if ( context.getCurrentType() == null || context.getCurrentType().isPrimitive()
            || Character.class.isAssignableFrom( context.getCurrentType() )
            || Object.class == context.getCurrentType() )
        {
            pre = pre + " ($w) (";
            post = post + ")";
        }

        String rootExpr = !"null".equals( getterCode ) ? getRootExpression( expression, root, context ) : "";

        String noRoot = (String) context.remove( "_noRoot" );
        if ( noRoot != null )
        {
            rootExpr = "";
        }

        createLocalReferences( context, classPool, newClass, objClass, valueGetter.getParameterTypes() );

        if ( expression instanceof OrderedReturn
            && ( (OrderedReturn) expression ).getLastExpression() != null )
        {
            body = "{ " + ( expression instanceof ASTMethod || expression instanceof ASTChain
                ? rootExpr
                : "" ) + ( castExpression != null ? castExpression : "" )
                + ( (OrderedReturn) expression ).getCoreExpression() + " return " + pre
                + ( (OrderedReturn) expression ).getLastExpression() + post + ";}";

        }
        else
        {

            body =
                "{  return " + pre + ( castExpression != null ? castExpression : "" ) + rootExpr + getterCode + post
                    + ";}";
        }

        body = body.replaceAll( "\\.\\.", "." );

        // System.out.println("Getter Body: ===================================\n" + body);
        valueGetter.setBody( body );
        newClass.addMethod( valueGetter );

        return body;
    }

    /**
     * {@inheritDoc}
     */
    public String createLocalReference( OgnlContext context, String expression, Class<?> type )
    {
        String referenceName = "ref" + context.incrementLocalReferenceCounter();
        context.addLocalReference( referenceName, new LocalReferenceImpl( referenceName, expression, type ) );

        String castString = "";
        if ( !type.isPrimitive() )
        {
            castString = "(" + ExpressionCompiler.getCastString( type ) + ") ";
        }

        return castString + referenceName + "($$)";
    }

    void createLocalReferences( OgnlContext context, ClassPool classPool, CtClass clazz, CtClass unused,
                                CtClass[] params )
        throws NotFoundException, CannotCompileException
    {
        Map<String, LocalReference> referenceMap = context.getLocalReferences();
        if ( referenceMap == null || referenceMap.isEmpty() )
        {
            return;
        }

        Iterator<LocalReference> it = referenceMap.values().iterator();
        while( it.hasNext() )
        {
            LocalReference ref = it.next();
            String widener = ref.getType().isPrimitive() ? " " : " ($w) ";

            String body = format( "{ return %s %s; }", widener, ref.getExpression() ).replaceAll( "\\.\\.", "." );

            // System.out.println("adding method " + ref.getName() + " with body:\n" + body + " and return type: " +
            // ref.getType());

            CtMethod method =
                new CtMethod( classPool.get( getCastString( ref.getType() ) ), ref.getName(), params, clazz );
            method.setBody( body );

            clazz.addMethod( method );
            it.remove();
        }
    }

    protected String generateSetter( OgnlContext context, CtClass newClass, CtClass objClass, ClassPool classPool,
                                     CtMethod valueSetter, Node expression, Object root )
        throws Exception
    {
        if ( expression instanceof ExpressionNode || expression instanceof ASTConst)
        {
            throw new UnsupportedCompilationException( "Can't compile expression/constant setters." );
        }

        context.setRoot( root );
        context.remove( PRE_CAST );

        String body;

        String setterCode = expression.toSetSourceString( context, root );
        String castExpression = (String) context.get( PRE_CAST );

        if ( setterCode == null || setterCode.trim().length() < 1 )
        {
            throw new UnsupportedCompilationException( "Can't compile null setter body." );
        }

        if ( root == null )
        {
            throw new UnsupportedCompilationException( "Can't compile setters with a null root object." );
        }

        String pre = getRootExpression( expression, root, context );

        String noRoot = (String) context.remove( "_noRoot" );
        if ( noRoot != null )
        {
            pre = "";
        }

        createLocalReferences( context, classPool, newClass, objClass, valueSetter.getParameterTypes() );

        body = "{" + ( castExpression != null ? castExpression : "" ) + pre + setterCode + ";}";

        body = body.replaceAll( "\\.\\.", "." );

        // System.out.println("Setter Body: ===================================\n" + body);

        valueSetter.setBody( body );
        newClass.addMethod( valueSetter );

        return body;
    }

    /**
     * Fail safe getter creation when normal compilation fails.
     *
     * @param clazz The javassist class the new method should be attached to.
     * @param valueGetter The method definition the generated code will be contained within.
     * @param node The root expression node.
     * @return The generated source string for this method, the method will still be added via the javassist API either
     *         way so this is really a convenience for exception reporting / debugging.
     * @throws Exception If a javassist error occurs.
     */
    protected String generateOgnlGetter( CtClass clazz, CtMethod valueGetter, CtField node )
        throws Exception
    {
        String body = "return " + node.getName() + ".getValue($1, $2);";

        valueGetter.setBody( body );
        clazz.addMethod( valueGetter );

        return body;
    }

    /**
     * Fail safe setter creation when normal compilation fails.
     *
     * @param clazz The javassist class the new method should be attached to.
     * @param valueSetter The method definition the generated code will be contained within.
     * @param node The root expression node.
     * @return The generated source string for this method, the method will still be added via the javassist API either
     *         way so this is really a convenience for exception reporting / debugging.
     * @throws Exception If a javassist error occurs.
     */
    protected String generateOgnlSetter( CtClass clazz, CtMethod valueSetter, CtField node )
        throws Exception
    {
        String body = node.getName() + ".setValue($1, $2, $3);";

        valueSetter.setBody( body );
        clazz.addMethod( valueSetter );

        return body;
    }

    /**
     * Creates a {@link ClassLoader} instance compatible with the javassist classloader and normal OGNL class resolving
     * semantics.
     *
     * @param context The current execution context.
     * @return The created {@link ClassLoader} instance.
     */
    protected EnhancedClassLoader getClassLoader( OgnlContext context )
    {
        EnhancedClassLoader ret = loaders.get( context.getClassResolver() );

        if ( ret != null )
        {
            return ret;
        }

        ClassLoader classLoader = new ContextClassLoader( OgnlContext.class.getClassLoader(), context );

        ret = new EnhancedClassLoader( classLoader );
        loaders.put( context.getClassResolver(), ret );

        return ret;
    }

    /**
     * Loads a new class definition via javassist for the specified class.
     *
     * @param searchClass The class to load.
     * @return The javassist class equivalent.
     * @throws javassist.NotFoundException When the class definition can't be found.
     */
    protected CtClass getCtClass( Class<?> searchClass )
        throws NotFoundException
    {
        return pool.get( searchClass.getName() );
    }

    /**
     * Gets either a new or existing {@link ClassPool} for use in compiling javassist classes. A new class path object
     * is inserted in to the returned {@link ClassPool} using the passed in <code>loader</code> instance if a new pool
     * needs to be created.
     *
     * @param context The current execution context.
     * @param loader The {@link ClassLoader} instance to use - as returned by
     *            {@link #getClassLoader(org.apache.commons.ognl.OgnlContext)}.
     * @return The existing or new {@link ClassPool} instance.
     */
    protected ClassPool getClassPool( OgnlContext context, EnhancedClassLoader loader )
    {
        if ( pool != null )
        {
            return pool;
        }

        pool = ClassPool.getDefault();
        pool.insertClassPath( new LoaderClassPath( loader.getParent() ) );

        return pool;
    }
}
