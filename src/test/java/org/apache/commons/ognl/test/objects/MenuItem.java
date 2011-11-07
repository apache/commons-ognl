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
import java.util.List;

/**
 *
 */
public class MenuItem
{

    private String page;

    private String label;

    private List<MenuItem> children = new ArrayList<MenuItem>();

    public MenuItem( String page, String label )
    {
        this( page, label, new ArrayList<MenuItem>() );
    }

    public MenuItem( String page, String label, List<MenuItem> children )
    {
        this.page = page;
        this.label = label;
        this.children = children;
    }

    public List<MenuItem> getChildren()
    {
        return children;
    }

    public String getLabel()
    {
        return label;
    }

    public String getPage()
    {
        return page;
    }

    public String toString()
    {
        return new StringBuilder( "MenuItem[" )
            .append( "page=" )
            .append( getPage() )
            .append( ",label=" )
            .append( getLabel() )
            .append( ",children=" )
            .append( getChildren().size() )
            .append( "]" ).toString();
    }
}
