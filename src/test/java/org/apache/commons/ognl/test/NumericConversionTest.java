/*
 * $Id$
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.ognl.test;

import static junit.framework.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;
import org.apache.commons.ognl.OgnlException;
import org.apache.commons.ognl.OgnlOps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith( value = Parameterized.class )
public class NumericConversionTest
    extends OgnlTestCase
{

    private static final Object[][] TESTS = {
        /* To Integer.class */
        { "55", Integer.class, new Integer( 55 ) }, { new Integer( 55 ), Integer.class, new Integer( 55 ) },
        { new Double( 55 ), Integer.class, new Integer( 55 ) }, { Boolean.TRUE, Integer.class, new Integer( 1 ) },
        { new Byte( (byte) 55 ), Integer.class, new Integer( 55 ) },
        { new Character( (char) 55 ), Integer.class, new Integer( 55 ) },
        { new Short( (short) 55 ), Integer.class, new Integer( 55 ) },
        { new Long( 55 ), Integer.class, new Integer( 55 ) }, { new Float( 55 ), Integer.class, new Integer( 55 ) },
        { new BigInteger( "55" ), Integer.class, new Integer( 55 ) },
        { new BigDecimal( "55" ), Integer.class, new Integer( 55 ) },

        /* To Double.class */
        { "55.1234", Double.class, new Double( 55.1234 ) }, { new Integer( 55 ), Double.class, new Double( 55 ) },
        { new Double( 55.1234 ), Double.class, new Double( 55.1234 ) },
        { Boolean.TRUE, Double.class, new Double( 1 ) }, { new Byte( (byte) 55 ), Double.class, new Double( 55 ) },
        { new Character( (char) 55 ), Double.class, new Double( 55 ) },
        { new Short( (short) 55 ), Double.class, new Double( 55 ) },
        { new Long( 55 ), Double.class, new Double( 55 ) },
        { new Float( 55.1234 ), Double.class, new Double( 55.1234 ), new Integer( 4 ) },
        { new BigInteger( "55" ), Double.class, new Double( 55 ) },
        { new BigDecimal( "55.1234" ), Double.class, new Double( 55.1234 ) },

        /* To Boolean.class */
        { "true", Boolean.class, Boolean.TRUE }, { new Integer( 55 ), Boolean.class, Boolean.TRUE },
        { new Double( 55 ), Boolean.class, Boolean.TRUE }, { Boolean.TRUE, Boolean.class, Boolean.TRUE },
        { new Byte( (byte) 55 ), Boolean.class, Boolean.TRUE },
        { new Character( (char) 55 ), Boolean.class, Boolean.TRUE },
        { new Short( (short) 55 ), Boolean.class, Boolean.TRUE }, { new Long( 55 ), Boolean.class, Boolean.TRUE },
        { new Float( 55 ), Boolean.class, Boolean.TRUE }, { new BigInteger( "55" ), Boolean.class, Boolean.TRUE },
        { new BigDecimal( "55" ), Boolean.class, Boolean.TRUE },

        /* To Byte.class */
        { "55", Byte.class, new Byte( (byte) 55 ) }, { new Integer( 55 ), Byte.class, new Byte( (byte) 55 ) },
        { new Double( 55 ), Byte.class, new Byte( (byte) 55 ) }, { Boolean.TRUE, Byte.class, new Byte( (byte) 1 ) },
        { new Byte( (byte) 55 ), Byte.class, new Byte( (byte) 55 ) },
        { new Character( (char) 55 ), Byte.class, new Byte( (byte) 55 ) },
        { new Short( (short) 55 ), Byte.class, new Byte( (byte) 55 ) },
        { new Long( 55 ), Byte.class, new Byte( (byte) 55 ) }, { new Float( 55 ), Byte.class, new Byte( (byte) 55 ) },
        { new BigInteger( "55" ), Byte.class, new Byte( (byte) 55 ) },
        { new BigDecimal( "55" ), Byte.class, new Byte( (byte) 55 ) },

        /* To Character.class */
        { "55", Character.class, new Character( (char) 55 ) },
        { new Integer( 55 ), Character.class, new Character( (char) 55 ) },
        { new Double( 55 ), Character.class, new Character( (char) 55 ) },
        { Boolean.TRUE, Character.class, new Character( (char) 1 ) },
        { new Byte( (byte) 55 ), Character.class, new Character( (char) 55 ) },
        { new Character( (char) 55 ), Character.class, new Character( (char) 55 ) },
        { new Short( (short) 55 ), Character.class, new Character( (char) 55 ) },
        { new Long( 55 ), Character.class, new Character( (char) 55 ) },
        { new Float( 55 ), Character.class, new Character( (char) 55 ) },
        { new BigInteger( "55" ), Character.class, new Character( (char) 55 ) },
        { new BigDecimal( "55" ), Character.class, new Character( (char) 55 ) },

        /* To Short.class */
        { "55", Short.class, new Short( (short) 55 ) }, { new Integer( 55 ), Short.class, new Short( (short) 55 ) },
        { new Double( 55 ), Short.class, new Short( (short) 55 ) },
        { Boolean.TRUE, Short.class, new Short( (short) 1 ) },
        { new Byte( (byte) 55 ), Short.class, new Short( (short) 55 ) },
        { new Character( (char) 55 ), Short.class, new Short( (short) 55 ) },
        { new Short( (short) 55 ), Short.class, new Short( (short) 55 ) },
        { new Long( 55 ), Short.class, new Short( (short) 55 ) },
        { new Float( 55 ), Short.class, new Short( (short) 55 ) },
        { new BigInteger( "55" ), Short.class, new Short( (short) 55 ) },
        { new BigDecimal( "55" ), Short.class, new Short( (short) 55 ) },

        /* To Long.class */
        { "55", Long.class, new Long( 55 ) }, { new Integer( 55 ), Long.class, new Long( 55 ) },
        { new Double( 55 ), Long.class, new Long( 55 ) }, { Boolean.TRUE, Long.class, new Long( 1 ) },
        { new Byte( (byte) 55 ), Long.class, new Long( 55 ) },
        { new Character( (char) 55 ), Long.class, new Long( 55 ) },
        { new Short( (short) 55 ), Long.class, new Long( 55 ) }, { new Long( 55 ), Long.class, new Long( 55 ) },
        { new Float( 55 ), Long.class, new Long( 55 ) }, { new BigInteger( "55" ), Long.class, new Long( 55 ) },
        { new BigDecimal( "55" ), Long.class, new Long( 55 ) },

        /* To Float.class */
        { "55.1234", Float.class, new Float( 55.1234 ) }, { new Integer( 55 ), Float.class, new Float( 55 ) },
        { new Double( 55.1234 ), Float.class, new Float( 55.1234 ) }, { Boolean.TRUE, Float.class, new Float( 1 ) },
        { new Byte( (byte) 55 ), Float.class, new Float( 55 ) },
        { new Character( (char) 55 ), Float.class, new Float( 55 ) },
        { new Short( (short) 55 ), Float.class, new Float( 55 ) }, { new Long( 55 ), Float.class, new Float( 55 ) },
        { new Float( 55.1234 ), Float.class, new Float( 55.1234 ) },
        { new BigInteger( "55" ), Float.class, new Float( 55 ) },
        { new BigDecimal( "55.1234" ), Float.class, new Float( 55.1234 ) },

        /* To BigInteger.class */
        { "55", BigInteger.class, new BigInteger( "55" ) },
        { new Integer( 55 ), BigInteger.class, new BigInteger( "55" ) },
        { new Double( 55 ), BigInteger.class, new BigInteger( "55" ) },
        { Boolean.TRUE, BigInteger.class, new BigInteger( "1" ) },
        { new Byte( (byte) 55 ), BigInteger.class, new BigInteger( "55" ) },
        { new Character( (char) 55 ), BigInteger.class, new BigInteger( "55" ) },
        { new Short( (short) 55 ), BigInteger.class, new BigInteger( "55" ) },
        { new Long( 55 ), BigInteger.class, new BigInteger( "55" ) },
        { new Float( 55 ), BigInteger.class, new BigInteger( "55" ) },
        { new BigInteger( "55" ), BigInteger.class, new BigInteger( "55" ) },
        { new BigDecimal( "55" ), BigInteger.class, new BigInteger( "55" ) },

        /* To BigDecimal.class */
        { "55.1234", BigDecimal.class, new BigDecimal( "55.1234" ) },
        { new Integer( 55 ), BigDecimal.class, new BigDecimal( "55" ) },
        { new Double( 55.1234 ), BigDecimal.class, new BigDecimal( "55.1234" ), new Integer( 4 ) },
        { Boolean.TRUE, BigDecimal.class, new BigDecimal( "1" ) },
        { new Byte( (byte) 55 ), BigDecimal.class, new BigDecimal( "55" ) },
        { new Character( (char) 55 ), BigDecimal.class, new BigDecimal( "55" ) },
        { new Short( (short) 55 ), BigDecimal.class, new BigDecimal( "55" ) },
        { new Long( 55 ), BigDecimal.class, new BigDecimal( "55" ) },
        { new Float( 55.1234 ), BigDecimal.class, new BigDecimal( "55.1234" ), new Integer( 4 ) },
        { new BigInteger( "55" ), BigDecimal.class, new BigDecimal( "55" ) },
        { new BigDecimal( "55.1234" ), BigDecimal.class, new BigDecimal( "55.1234" ) }, };

    private final Object value;

    private final Class<? extends Number> toClass;

    private final Object expectedValue;

    private final int scale;

    /*
     * =================================================================== Public static methods
     * ===================================================================
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        Collection<Object[]> data = new ArrayList<Object[]>(TESTS.length);
        for ( int i = 0; i < TESTS.length; i++ )
        {
            Object[] tmp = new Object[4];
            tmp[0] = TESTS[i][0];
            tmp[1] = TESTS[i][1];
            tmp[2] = TESTS[i][2];
            tmp[3] = ( TESTS[i].length > 3 ) ? ( (Integer) TESTS[i][3] ).intValue() : -1 ;

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public NumericConversionTest( Object value, Class<? extends Number> toClass, Object expectedValue, int scale )
    {
        super( value + " [" + value.getClass().getName() + "] -> " + toClass.getName() + " == " + expectedValue + " ["
            + expectedValue.getClass().getName() + "]"
            + ( ( scale >= 0 ) ? ( " (to within " + scale + " decimal places)" ) : "" ), null, null, null, null, null );
        this.value = value;
        this.toClass = toClass;
        this.expectedValue = expectedValue;
        this.scale = scale;
    }

    /*
     * =================================================================== Overridden methods
     * ===================================================================
     */
    @Test
    @Override
    public void runTest()
        throws OgnlException
    {
        Object result;

        result = OgnlOps.convertValue( value, toClass );
        if ( !isEqual( result, expectedValue ) )
        {
            if ( scale >= 0 )
            {
                double scalingFactor = Math.pow( 10, scale ), v1 = ( (Number) value ).doubleValue() * scalingFactor, v2 =
                    ( (Number) expectedValue ).doubleValue() * scalingFactor;

                Assert.assertEquals((int) v1, (int) v2);
            }
            else
            {
                fail();
            }
        }
    }
}
