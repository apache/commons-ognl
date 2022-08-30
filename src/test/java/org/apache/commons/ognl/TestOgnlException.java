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
package org.apache.commons.ognl;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Tests {@link OgnlException}.
 */
public class TestOgnlException
{

    @Test
    public void test_Throwable_Reason()
    {
        try
        {
            throwException();
        }
        catch ( OgnlException e )
        {
            assertTrue( NumberFormatException.class.isInstance( e.getReason() ) );
        }
    }

    void throwException()
        throws OgnlException
    {
        try
        {
            Integer.parseInt( "45ac" );
        }
        catch ( NumberFormatException et )
        {
            throw new OgnlException( "Unable to parse input string.", et );
        }
    }
}
