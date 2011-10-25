package org.apache.commons.ognl;

/*
 * $Id: $
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public interface NodeVisitor<R, P>
{
    R visit( ASTSequence node, P data );

    R visit( ASTAssign node, P data );

    R visit( ASTTest node, P data );

    R visit( ASTOr node, P data );

    R visit( ASTAnd node, P data );

    R visit( ASTBitOr node, P data );

    R visit( ASTXor node, P data );

    R visit( ASTBitAnd node, P data );

    R visit( ASTEq node, P data );

    R visit( ASTNotEq node, P data );

    R visit( ASTLess node, P data );

    R visit( ASTGreater node, P data );

    R visit( ASTLessEq node, P data );

    R visit( ASTGreaterEq node, P data );

    R visit( ASTIn node, P data );

    R visit( ASTNotIn node, P data );

    R visit( ASTShiftLeft node, P data );

    R visit( ASTShiftRight node, P data );

    R visit( ASTUnsignedShiftRight node, P data );

    R visit( ASTAdd node, P data );

    R visit( ASTSubtract node, P data );

    R visit( ASTMultiply node, P data );

    R visit( ASTDivide node, P data );

    R visit( ASTRemainder node, P data );

    R visit( ASTNegate node, P data );

    R visit( ASTBitNegate node, P data );

    R visit( ASTNot node, P data );

    R visit( ASTInstanceof node, P data );

    R visit( ASTChain node, P data );

    R visit( ASTEval node, P data );

    R visit( ASTConst node, P data );

    R visit( ASTThisVarRef node, P data );

    R visit( ASTRootVarRef node, P data );

    R visit( ASTVarRef node, P data );

    R visit( ASTList node, P data );

    R visit( ASTMap node, P data );

    R visit( ASTKeyValue node, P data );

    R visit( ASTStaticField node, P data );

    R visit( ASTCtor node, P data );

    R visit( ASTProperty node, P data );

    R visit( ASTStaticMethod node, P data );

    R visit( ASTMethod node, P data );

    R visit( ASTProject node, P data );

    R visit( ASTSelect node, P data );

    R visit( ASTSelectFirst node, P data );

    R visit( ASTSelectLast node, P data );
}
