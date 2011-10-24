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

package org.apache.commons.ognl.performance;

import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * User: Maurizio Cucchiara
 * Date: 10/22/11
 * Time: 12:49 AM
 */

@AxisRange( min = 0, max = 1 )
@BenchmarkMethodChart( filePrefix = "benchmark-old-ognl" )
@BenchmarkHistoryChart( labelWith = LabelType.CUSTOM_KEY, maxRuns = 20 )
public class PerformanceOldOgnlTest
    extends PerformanceCommonsOgnlTest
{
    @BeforeClass
    public static void before( )
    {
        runtimeWrapper = OLD_RUNTIME_WRAPPER;
    }

    @AfterClass
    public static void after( )
    {
        runtimeWrapper.clearCache( );
    }
}
