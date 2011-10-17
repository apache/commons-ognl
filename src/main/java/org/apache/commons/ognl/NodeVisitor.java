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
package org.apache.commons.ognl;

public interface NodeVisitor<R, P>
{
  public R visit(ASTSequence node, P data) throws OgnlException;
  public R visit(ASTAssign node, P data) throws OgnlException;
  public R visit(ASTTest node, P data) throws OgnlException;
  public R visit(ASTOr node, P data) throws OgnlException;
  public R visit(ASTAnd node, P data) throws OgnlException;
  public R visit(ASTBitOr node, P data) throws OgnlException;
  public R visit(ASTXor node, P data) throws OgnlException;
  public R visit(ASTBitAnd node, P data) throws OgnlException;
  public R visit(ASTEq node, P data) throws OgnlException;
  public R visit(ASTNotEq node, P data) throws OgnlException;
  public R visit(ASTLess node, P data) throws OgnlException;
  public R visit(ASTGreater node, P data) throws OgnlException;
  public R visit(ASTLessEq node, P data) throws OgnlException;
  public R visit(ASTGreaterEq node, P data) throws OgnlException;
  public R visit(ASTIn node, P data) throws OgnlException;
  public R visit(ASTNotIn node, P data) throws OgnlException;
  public R visit(ASTShiftLeft node, P data) throws OgnlException;
  public R visit(ASTShiftRight node, P data) throws OgnlException;
  public R visit(ASTUnsignedShiftRight node, P data) throws OgnlException;
  public R visit(ASTAdd node, P data) throws OgnlException;
  public R visit(ASTSubtract node, P data) throws OgnlException;
  public R visit(ASTMultiply node, P data) throws OgnlException;
  public R visit(ASTDivide node, P data) throws OgnlException;
  public R visit(ASTRemainder node, P data) throws OgnlException;
  public R visit(ASTNegate node, P data) throws OgnlException;
  public R visit(ASTBitNegate node, P data) throws OgnlException;
  public R visit(ASTNot node, P data) throws OgnlException;
  public R visit(ASTInstanceof node, P data) throws OgnlException;
  public R visit(ASTChain node, P data) throws OgnlException;
  public R visit(ASTEval node, P data) throws OgnlException;
  public R visit(ASTConst node, P data) throws OgnlException;
  public R visit(ASTThisVarRef node, P data) throws OgnlException;
  public R visit(ASTRootVarRef node, P data) throws OgnlException;
  public R visit(ASTVarRef node, P data) throws OgnlException;
  public R visit(ASTList node, P data) throws OgnlException;
  public R visit(ASTMap node, P data) throws OgnlException;
  public R visit(ASTKeyValue node, P data) throws OgnlException;
  public R visit(ASTStaticField node, P data) throws OgnlException;
  public R visit(ASTCtor node, P data) throws OgnlException;
  public R visit(ASTProperty node, P data) throws OgnlException;
  public R visit(ASTStaticMethod node, P data) throws OgnlException;
  public R visit(ASTMethod node, P data) throws OgnlException;
  public R visit(ASTProject node, P data) throws OgnlException;
  public R visit(ASTSelect node, P data) throws OgnlException;
  public R visit(ASTSelectFirst node, P data) throws OgnlException;
  public R visit(ASTSelectLast node, P data) throws OgnlException;
}
