/*
 * $Id: $
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

package org.apache.commons.ognl.performance;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import org.apache.commons.ognl.performance.invocation.CompilerInvocation;
import org.apache.commons.ognl.performance.invocation.FieldInvocation;
import org.apache.commons.ognl.performance.invocation.FindParameterTypesInvocation;
import org.apache.commons.ognl.performance.invocation.GetConstructorsInvocation;
import org.apache.commons.ognl.performance.invocation.GetDeclaredMethodsInvocation;
import org.apache.commons.ognl.performance.invocation.GetMethodsInvocation;
import org.apache.commons.ognl.performance.invocation.MethodParameterTypesInvocation;
import org.apache.commons.ognl.performance.invocation.PermissionInvocation;
import org.apache.commons.ognl.performance.invocation.PrimitiveDefaultInvocation;
import org.apache.commons.ognl.performance.runtime.CommonsRuntimeWrapper;
import org.apache.commons.ognl.performance.runtime.OldOgnlRuntimeWrapper;
import org.apache.commons.ognl.performance.runtime.RuntimeWrapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

/**
 * User: Maurizio Cucchiara
 * Date: 10/22/11
 * Time: 10:25 AM
 */
@AxisRange( min = 0, max = 1 )
@BenchmarkMethodChart( filePrefix = "benchmark-ognl" )
public abstract class BasePerformanceTest
{
    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    public static final RuntimeWrapper COMMONS_RUNTIME_WRAPPER = new CommonsRuntimeWrapper();

    public static final RuntimeWrapper OLD_RUNTIME_WRAPPER = new OldOgnlRuntimeWrapper();

    protected static RuntimeWrapper runtimeWrapper;

    @BenchmarkOptions( benchmarkRounds = 50, warmupRounds = 0, concurrency = 1000 )
    @Test
    public void constructorCache()
        throws Exception
    {
        new GetConstructorsInvocation( runtimeWrapper, 50000 );
    }

    @BenchmarkOptions( benchmarkRounds = 50, warmupRounds = 0, concurrency = 1000 )
    @Test
    public void declaredMethodCache()
        throws Exception
    {
        new GetDeclaredMethodsInvocation( runtimeWrapper );
    }

    @BenchmarkOptions( benchmarkRounds = 50, warmupRounds = 0, concurrency = 1000 )
    @Test
    public void fieldCache()
        throws Exception
    {
        new FieldInvocation( runtimeWrapper, 50000 );
    }

    @BenchmarkOptions( benchmarkRounds = 50, warmupRounds = 0, concurrency = 1000 )
    @Test
    public void findParameterTypeCache()
        throws Exception
    {
        new FindParameterTypesInvocation( runtimeWrapper, 100 );
    }

    @BenchmarkOptions( benchmarkRounds = 50, warmupRounds = 0, concurrency = 1000 )
    @Test
    public void methodCache()
        throws Exception
    {
        new GetMethodsInvocation( runtimeWrapper, 20000 );
    }

    @BenchmarkOptions( benchmarkRounds = 50, warmupRounds = 0, concurrency = 1000 )
    @Test
    public void methodParameterTypeCache()
        throws Exception
    {
        new MethodParameterTypesInvocation( runtimeWrapper );
    }

    @BenchmarkOptions( benchmarkRounds = 50, warmupRounds = 0, concurrency = 1000 )
    @Test
    public void permissionCache()
        throws Exception
    {
        new PermissionInvocation( runtimeWrapper );
    }

    @BenchmarkOptions( benchmarkRounds = 50, warmupRounds = 0, concurrency = 1000 )
    @Test
    public void primitiveCache()
        throws Exception
    {
        new PrimitiveDefaultInvocation( runtimeWrapper, 100000 );
    }

    @BenchmarkOptions( benchmarkRounds = 50, warmupRounds = 0, concurrency = 1000 )
    @Test
    public void compiler()
        throws Exception
    {
        new CompilerInvocation( runtimeWrapper, 100 );
    }
}
