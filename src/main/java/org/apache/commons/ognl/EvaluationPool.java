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

import java.util.*;

public final class EvaluationPool extends Object
{
    private List        evaluations = new ArrayList();
    private int         size = 0;
    private int         created = 0;
    private int         recovered = 0;
    private int         recycled = 0;

    public EvaluationPool()
    {
        this(0);
    }

    public EvaluationPool(int initialSize)
    {
        super();
        for (int i = 0; i < initialSize; i++) {
            evaluations.add(new Evaluation(null, null));
        }
        created = size = initialSize;
    }

    /**
        Returns an Evaluation that contains the node, source and whether it
        is a set operation.  If there are no Evaluation objects in the
        pool one is created and returned.
     */
    public Evaluation create(SimpleNode node, Object source)
    {
        return create(node, source, false);
    }

    /**
        Returns an Evaluation that contains the node, source and whether it
        is a set operation.  If there are no Evaluation objects in the
        pool one is created and returned.
     */
    public synchronized Evaluation create(SimpleNode node, Object source, boolean setOperation)
    {
        Evaluation          result;

        if (size > 0) {
            result = (Evaluation)evaluations.remove(size - 1);
            result.init(node, source, setOperation);
            size--;
            recovered++;
        } else {
            result = new Evaluation(node, source, setOperation);
            created++;
        }
        return result;
    }

    /**
        Recycles an Evaluation
     */
    public synchronized void recycle(Evaluation value)
    {
        if (value != null) {
            value.reset();
            evaluations.add(value);
            size++;
            recycled++;
        }
    }

    /**
        Recycles an of Evaluation and all of it's siblings
        and children.
     */
    public void recycleAll(Evaluation value)
    {
        if (value != null) {
            recycleAll(value.getNext());
            recycleAll(value.getFirstChild());
            recycle(value);
        }
    }

    /**
        Recycles a List of Evaluation objects
     */
    public void recycleAll(List value)
    {
        if (value != null) {
            for (int i = 0, icount = value.size(); i < icount; i++) {
                recycle((Evaluation)value.get(i));
            }
        }
    }

    /**
        Returns the number of items in the pool
     */
    public int getSize()
    {
        return size;
    }

    /**
        Returns the number of items this pool has created since
        it's construction.
     */
    public int getCreatedCount()
    {
        return created;
    }

    /**
        Returns the number of items this pool has recovered from
        the pool since its construction.
     */
    public int getRecoveredCount()
    {
        return recovered;
    }

    /**
        Returns the number of items this pool has recycled since
        it's construction.
     */
    public int getRecycledCount()
    {
        return recycled;
    }
}
