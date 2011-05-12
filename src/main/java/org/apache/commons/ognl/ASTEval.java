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
package org.apache.commons.ognl;

import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
class ASTEval extends SimpleNode
{

    public ASTEval(int id)
    {
        super(id);
    }

    public ASTEval(OgnlParser p, int id)
    {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source)
        throws OgnlException
    {
        Object result, expr = _children[0].getValue(context, source), previousRoot = context.getRoot();
        Node node;

        source = _children[1].getValue(context, source);
        node = (expr instanceof Node) ? (Node) expr : (Node) Ognl.parseExpression(expr.toString());
        try {
            context.setRoot(source);
            result = node.getValue(context, source);
        } finally {
            context.setRoot(previousRoot);
        }
        return result;
    }

    protected void setValueBody(OgnlContext context, Object target, Object value)
        throws OgnlException
    {
        Object expr = _children[0].getValue(context, target), previousRoot = context.getRoot();
        Node node;

        target = _children[1].getValue(context, target);
        node = (expr instanceof Node) ? (Node) expr : (Node) Ognl.parseExpression(expr.toString());
        try {
            context.setRoot(target);
            node.setValue(context, target, value);
        } finally {
            context.setRoot(previousRoot);
        }
    }

    public String toString()
    {
        return "(" + _children[0] + ")(" + _children[1] + ")";
    }
    
    public String toGetSourceString(OgnlContext context, Object target)
    {
        throw new UnsupportedCompilationException("Eval expressions not supported as native java yet.");
    }
    
    public String toSetSourceString(OgnlContext context, Object target)
    {
        throw new UnsupportedCompilationException("Map expressions not supported as native java yet.");
    }
}
