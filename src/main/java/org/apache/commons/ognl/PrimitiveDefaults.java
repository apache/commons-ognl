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
 */
class PrimitiveDefaults {

    private final Map<Class<?>, Object> map = new HashMap<Class<?>, Object>( 20 );

    PrimitiveDefaults() {
        map.put( Boolean.TYPE, Boolean.FALSE );
        map.put( Boolean.class, Boolean.FALSE );
        map.put( Byte.TYPE, (byte) 0 );
        map.put( Byte.class, (byte) 0 );
        map.put( Short.TYPE, (short) 0 );
        map.put( Short.class, (short) 0 );
        map.put( Character.TYPE, (char) 0 );
        map.put( Integer.TYPE, 0 );
        map.put( Long.TYPE, 0L );
        map.put( Float.TYPE, 0.0f );
        map.put( Double.TYPE, 0.0 );

        map.put( BigInteger.class, BigInteger.ZERO );
        map.put( BigDecimal.class, BigDecimal.ZERO );
    }

    Object get( Class<?> cls ) {
        return map.get( cls );
    }
}
