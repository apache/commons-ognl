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

import static java.lang.String.format;

/**
 * Exception thrown if a method or constructor call fails.
 */
public class MethodFailedException
    extends OgnlException
{
    private static final long serialVersionUID = -8537354635249153386L;

    public MethodFailedException( Object source, String name )
    {
        super( format( "Method \"%s\" failed for object %s", name, source ) );
    }

    public MethodFailedException( Object source, String name, Throwable reason )
    {
        super( format( "Method \"%s\" failed for object %s", name, source ), reason );
    }
}
