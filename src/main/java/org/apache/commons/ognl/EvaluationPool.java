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

import java.util.List;

/**
 * @deprecated evaluation-pooling now relies on the jvm garbage collection
 */
public final class EvaluationPool
{
    public EvaluationPool()
    {
    }

    /**
     * Returns an Evaluation that contains the node, source and whether it is a set operation. If there are no
     * Evaluation objects in the pool one is created and returned.
     */
    public Evaluation create( SimpleNode node, Object source )
    {
        return create( node, source, false );
    }

    /**
     * Returns an Evaluation that contains the node, source and whether it
     * is a set operation.
     */
    public Evaluation create( SimpleNode node, Object source, boolean setOperation )
    {
        // synchronization is removed as we do not rely anymore on the in-house object pooling
        return new Evaluation( node, source, setOperation );
    }

    /**
     * Recycles an Evaluation
     *
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public void recycle( Evaluation value )
    {
        // no need of recycling, we rely on the garbage collection efficiency
    }

    /**
     * Recycles an of Evaluation and all of it's siblings
     * and children.
     *
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public void recycleAll( Evaluation value )
    {
        // no need of recycling, we rely on the garbage collection efficiency
    }

    /**
     * Recycles a List of Evaluation objects
     *
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public void recycleAll( List value )
    {
        // no need of recycling, we rely on the garbage collection efficiency
    }

    /**
     * Returns the number of items in the pool
     *
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public int getSize()
    {
        return 0;
    }

    /**
     * Returns the number of items this pool has created since
     * it's construction.
     *
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public int getCreatedCount()
    {
        return 0;
    }

    /**
     * Returns the number of items this pool has recovered from
     * the pool since its construction.
     *
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public int getRecoveredCount()
    {
        return 0;
    }

    /**
     * Returns the number of items this pool has recycled since
     * it's construction.
     *
     * @deprecated object-pooling now relies on the jvm garbage collection
     */
    public int getRecycledCount()
    {
        return 0;
    }
}

