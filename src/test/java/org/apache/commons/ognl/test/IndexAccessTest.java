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

import org.apache.commons.ognl.MethodFailedException;
import org.apache.commons.ognl.NoSuchPropertyException;
import org.apache.commons.ognl.test.objects.IndexedSetObject;
import org.apache.commons.ognl.test.objects.Root;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class IndexAccessTest
    extends OgnlTestCase
{

    private static final Root ROOT = new Root();

    private static final IndexedSetObject INDEXED_SET = new IndexedSetObject();

    private static final Object[][] TESTS =
        {
            { ROOT, "list[index]", ROOT.getList().get( ROOT.getIndex() ) },
            { ROOT, "list[objectIndex]", ROOT.getList().get( ROOT.getObjectIndex().intValue() ) },
            { ROOT, "array[objectIndex]", ROOT.getArray()[ROOT.getObjectIndex().intValue()] },
            { ROOT, "array[getObjectIndex()]", ROOT.getArray()[ROOT.getObjectIndex().intValue()] },
            { ROOT, "array[genericIndex]", ROOT.getArray()[( (Integer) ROOT.getGenericIndex() ).intValue()] },
            { ROOT, "booleanArray[self.objectIndex]", Boolean.FALSE },
            { ROOT, "booleanArray[getObjectIndex()]", Boolean.FALSE },
            { ROOT, "booleanArray[nullIndex]", NoSuchPropertyException.class },
            { ROOT, "list[size() - 1]", MethodFailedException.class },
            { ROOT, "(index == (array.length - 3)) ? 'toggle toggleSelected' : 'toggle'", "toggle toggleSelected" },
            { ROOT, "\"return toggleDisplay('excdisplay\"+index+\"', this)\"",
                "return toggleDisplay('excdisplay1', this)" }, { ROOT, "map[mapKey].split('=')[0]", "StringStuff" },
            { ROOT, "booleanValues[index1][index2]", Boolean.FALSE },
            { ROOT, "tab.searchCriteria[index1].displayName", "Woodland creatures" },
            { ROOT, "tab.searchCriteriaSelections[index1][index2]", Boolean.TRUE },
            { ROOT, "tab.searchCriteriaSelections[index1][index2]", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE },
            { ROOT, "map['bar'].value", 100, 50, 50 }, { INDEXED_SET, "thing[\"x\"].val", 1, 2, 2 } };

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
    public IndexAccessTest( String name, Object root, String expressionString, Object expectedResult, Object setValue,
                            Object expectedAfterSetResult )
    {
        super( name, root, expressionString, expectedResult, setValue, expectedAfterSetResult );
    }
}
