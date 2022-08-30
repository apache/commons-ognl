/*
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

import org.apache.commons.ognl.test.objects.Root;
import org.apache.commons.ognl.test.objects.SimpleNumeric;
import org.apache.commons.ognl.test.objects.TestModel;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
@RunWith(value = Parameterized.class)
public class PropertyArithmeticAndLogicalOperatorsTest
    extends OgnlTestCase
{

    private static final Root ROOT = new Root();

    private static final TestModel MODEL = new TestModel();

    private static final SimpleNumeric NUMERIC = new SimpleNumeric();

    private static final Object[][] TESTS = {
        { ROOT, "objectIndex > 0", Boolean.TRUE },
        { ROOT, "false", Boolean.FALSE },
        { ROOT, "!false || true", Boolean.TRUE },
        { ROOT, "property.bean3.value >= 24", Boolean.TRUE },
        { ROOT, "genericIndex-1", new Integer( 1 ) },
        { ROOT, "((renderNavigation ? 0 : 1) + map.size) * theInt",
            new Integer( ( ( ROOT.getRenderNavigation() ? 0 : 1 ) + ROOT.getMap().size() ) * ROOT.getTheInt() ) },
        { ROOT, "{theInt + 1}", Arrays.asList( new Integer( ROOT.getTheInt() + 1 ) ) },
        { MODEL, "(unassignedCopyModel.optionCount > 0 && canApproveCopy) || entry.copy.size() > 0", Boolean.TRUE },
        { ROOT, " !(printDelivery || @Boolean@FALSE)", Boolean.FALSE },
        { ROOT, "(getIndexedProperty('nested').size - 1) > genericIndex", Boolean.FALSE },
        { ROOT, "(getIndexedProperty('nested').size + 1) >= genericIndex", Boolean.TRUE },
        { ROOT, "(getIndexedProperty('nested').size + 1) == genericIndex", Boolean.TRUE },
        { ROOT, "(getIndexedProperty('nested').size + 1) < genericIndex", Boolean.FALSE },
        { ROOT, "map.size * genericIndex",
            new Integer( ROOT.getMap().size() * ( (Integer) ROOT.getGenericIndex() ).intValue() ) },
        { ROOT, "property == property", Boolean.TRUE }, { ROOT, "property.bean3.value % 2 == 0", Boolean.TRUE },
        { ROOT, "genericIndex % 3 == 0", Boolean.FALSE },
        { ROOT, "genericIndex % theInt == property.bean3.value", Boolean.FALSE },
        { ROOT, "theInt / 100.0", ROOT.getTheInt() / 100.0 },
        { ROOT, "@java.lang.Long@valueOf('100') == @java.lang.Long@valueOf('100')", Boolean.TRUE },
        { NUMERIC, "budget - timeBilled", new Double( NUMERIC.getBudget() - NUMERIC.getTimeBilled() ) },
        { NUMERIC, "(budget % tableSize) == 0", Boolean.TRUE } };


    @Parameters
    public static Collection<Object[]> data()
    {
        Collection<Object[]> data = new ArrayList<Object[]>(TESTS.length);
        for ( int i = 0; i < TESTS.length; i++ )
        {
            Object[] tmp = new Object[6];
            tmp[0] = TESTS[i][1];
            tmp[1] = TESTS[i][0];
            tmp[2] = TESTS[i][1];
            tmp[3] = TESTS[i][2];

            if ( TESTS[i].length == 5 )
            {
                tmp[4] = TESTS[i][3];
                tmp[5] = TESTS[i][4];
            }

            data.add( tmp );
        }
        return data;
    }

    /*
     * =================================================================== Constructors
     * ===================================================================
     */
    public PropertyArithmeticAndLogicalOperatorsTest( String name, Object root, String expressionString,
                                                      Object expectedResult, Object setValue,
                                                      Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
