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
    R visit( ASTSequence node, P data )
        throws OgnlException;

    R visit( ASTAssign node, P data )
        throws OgnlException;

    R visit( ASTTest node, P data )
        throws OgnlException;

    R visit( ASTOr node, P data )
        throws OgnlException;

    R visit( ASTAnd node, P data )
        throws OgnlException;

    R visit( ASTBitOr node, P data )
        throws OgnlException;

    R visit( ASTXor node, P data )
        throws OgnlException;

    R visit( ASTBitAnd node, P data )
        throws OgnlException;

    R visit( ASTEq node, P data )
        throws OgnlException;

    R visit( ASTNotEq node, P data )
        throws OgnlException;

    R visit( ASTLess node, P data )
        throws OgnlException;

    R visit( ASTGreater node, P data )
        throws OgnlException;

    R visit( ASTLessEq node, P data )
        throws OgnlException;

    R visit( ASTGreaterEq node, P data )
        throws OgnlException;

    R visit( ASTIn node, P data )
        throws OgnlException;

    R visit( ASTNotIn node, P data )
        throws OgnlException;

    R visit( ASTShiftLeft node, P data )
        throws OgnlException;

    R visit( ASTShiftRight node, P data )
        throws OgnlException;

    R visit( ASTUnsignedShiftRight node, P data )
        throws OgnlException;

    R visit( ASTAdd node, P data )
        throws OgnlException;

    R visit( ASTSubtract node, P data )
        throws OgnlException;

    R visit( ASTMultiply node, P data )
        throws OgnlException;

    R visit( ASTDivide node, P data )
        throws OgnlException;

    R visit( ASTRemainder node, P data )
        throws OgnlException;

    R visit( ASTNegate node, P data )
        throws OgnlException;

    R visit( ASTBitNegate node, P data )
        throws OgnlException;

    R visit( ASTNot node, P data )
        throws OgnlException;

    R visit( ASTInstanceof node, P data )
        throws OgnlException;

    R visit( ASTChain node, P data )
        throws OgnlException;

    R visit( ASTEval node, P data )
        throws OgnlException;

    R visit( ASTConst node, P data )
        throws OgnlException;

    R visit( ASTThisVarRef node, P data )
        throws OgnlException;

    R visit( ASTRootVarRef node, P data )
        throws OgnlException;

    R visit( ASTVarRef node, P data )
        throws OgnlException;

    R visit( ASTList node, P data )
        throws OgnlException;

    R visit( ASTMap node, P data )
        throws OgnlException;

    R visit( ASTKeyValue node, P data )
        throws OgnlException;

    R visit( ASTStaticField node, P data )
        throws OgnlException;

    R visit( ASTCtor node, P data )
        throws OgnlException;

    R visit( ASTProperty node, P data )
        throws OgnlException;

    R visit( ASTStaticMethod node, P data )
        throws OgnlException;

    R visit( ASTMethod node, P data )
        throws OgnlException;

    R visit( ASTProject node, P data )
        throws OgnlException;

    R visit( ASTSelect node, P data )
        throws OgnlException;

    R visit( ASTSelectFirst node, P data )
        throws OgnlException;

    R visit(ASTSelectLast node, P data)  throws OgnlException;
}
