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

import java.util.HashMap;
import java.util.Map;

/**
 * Default class resolution. Uses ClassLoader.loadClass() to look up classes by name. It also looks in the "java.lang"
 * package
 * if the class named does not give a package specifier, allowing easier usage of these classes.
 */
public class DefaultClassResolver
    implements ClassResolver
{
    private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>( 101 );

    /**
     * Resolves a class for a given className
     *
     * @param className The name of the Class
     * @return The resulting Class object
     * @throws ClassNotFoundException If the class could not be found
     */
    public Class<?> classForName( String className )
        throws ClassNotFoundException
    {
        return classForName( className, null );
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> classForName( String className, Map<String, Object> unused )
        throws ClassNotFoundException
    {
        Class<?> result = classes.get( className );

        if ( result == null )
        {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            try
            {
                result = classLoader.loadClass( className );
            }
            catch ( ClassNotFoundException ex )
            {
                if ( className.indexOf( '.' ) == -1 )
                {
                    result = classLoader.loadClass( "java.lang." + className );
                    classes.put( "java.lang." + className, result );
                }
            }
            classes.put( className, result );
        }
        return result;
    }
}
