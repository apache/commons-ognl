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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Numeric primitive literal string expressions.
 */
class NumericLiterals {

    private final Map<Class<? extends Number>, String> map = new HashMap<Class<? extends Number>, String>();

    NumericLiterals() {
        map.put( Integer.class, "" );
        map.put( Integer.TYPE, "" );
        map.put( Long.class, "l" );
        map.put( Long.TYPE, "l" );
        map.put( BigInteger.class, "d" );
        map.put( Float.class, "f" );
        map.put( Float.TYPE, "f" );
        map.put( Double.class, "d" );
        map.put( Double.TYPE, "d" );
        map.put( BigInteger.class, "d" );
        map.put( BigDecimal.class, "d" );
    }

    String get( Class<? extends Number> clazz ) {
        return map.get( clazz );
    }
}
