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
package org.apache.commons.ognl.test.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test for OGNL-131.
 */
public class SearchTab
{

    /**
     * Flags stating which search criteria are selected
     */
    private List<List<Boolean>> searchCriteriaSelections = new ArrayList<List<Boolean>>();
    {
        searchCriteriaSelections.add( Arrays.asList( Boolean.TRUE, Boolean.FALSE, Boolean.FALSE ) );
        searchCriteriaSelections.add( Arrays.asList( Boolean.FALSE, Boolean.TRUE, Boolean.TRUE ) );
    }

    public List<List<Boolean>> getSearchCriteriaSelections()
    {
        return this.searchCriteriaSelections;
    }

    public void setSearchCriteriaSelections( List<List<Boolean>> selections )
    {
        this.searchCriteriaSelections = selections;
    }

    /**
     * Filters that can be applied to this tabs searches
     */
    private List<SearchCriteria> searchCriteria = new ArrayList<SearchCriteria>();
    {
        searchCriteria.add( new SearchCriteria( "Crittery critters" ) );
        searchCriteria.add( new SearchCriteria( "Woodland creatures" ) );
    }

    public List<SearchCriteria> getSearchCriteria()
    {
        return this.searchCriteria;
    }

    public void setSearchCriteria( List<SearchCriteria> searchCriteria )
    {
        this.searchCriteria = searchCriteria;
    }

    /**
     * 2D list of options available for each criteria
     */
    private List<List<String>> searchCriteriaOptions = new ArrayList<List<String>>();

    public List<List<String>> getSearchCriteriaOptions()
    {
        return this.searchCriteriaOptions;
    }

    public void setSearchCriteriaOptions( List<List<String>> searchCriteriaOptions )
    {

        this.searchCriteriaOptions = searchCriteriaOptions;
    }
}
