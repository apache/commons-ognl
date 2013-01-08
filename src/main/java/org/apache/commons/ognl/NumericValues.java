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
 * Constant strings for getting the primitive value of different native types on the generic {@link Number} object
 * interface. (or the less generic BigDecimal/BigInteger types)
 */
class NumericValues {

    private final Map<Class<?>, String> map = new HashMap<Class<?>, String>();

    NumericValues() {
        map.put( Double.class, "doubleValue()" );
        map.put( Float.class, "floatValue()" );
        map.put( Integer.class, "intValue()" );
        map.put( Long.class, "longValue()" );
        map.put( Short.class, "shortValue()" );
        map.put( Byte.class, "byteValue()" );
        map.put( BigDecimal.class, "doubleValue()" );
        map.put( BigInteger.class, "doubleValue()" );
        map.put( Boolean.class, "booleanValue()" );
    }

    String get( Class<?> cls ) {
        return map.get( cls );
    }
}
