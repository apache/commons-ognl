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
  public R visit(ASTSequence node, P data);
  public R visit(ASTAssign node, P data);
  public R visit(ASTTest node, P data);
  public R visit(ASTOr node, P data);
  public R visit(ASTAnd node, P data);
  public R visit(ASTBitOr node, P data);
  public R visit(ASTXor node, P data);
  public R visit(ASTBitAnd node, P data);
  public R visit(ASTEq node, P data);
  public R visit(ASTNotEq node, P data);
  public R visit(ASTLess node, P data);
  public R visit(ASTGreater node, P data);
  public R visit(ASTLessEq node, P data);
  public R visit(ASTGreaterEq node, P data);
  public R visit(ASTIn node, P data);
  public R visit(ASTNotIn node, P data);
  public R visit(ASTShiftLeft node, P data);
  public R visit(ASTShiftRight node, P data);
  public R visit(ASTUnsignedShiftRight node, P data);
  public R visit(ASTAdd node, P data);
  public R visit(ASTSubtract node, P data);
  public R visit(ASTMultiply node, P data);
  public R visit(ASTDivide node, P data);
  public R visit(ASTRemainder node, P data);
  public R visit(ASTNegate node, P data);
  public R visit(ASTBitNegate node, P data);
  public R visit(ASTNot node, P data);
  public R visit(ASTInstanceof node, P data);
  public R visit(ASTChain node, P data);
  public R visit(ASTEval node, P data);
  public R visit(ASTConst node, P data);
  public R visit(ASTThisVarRef node, P data);
  public R visit(ASTRootVarRef node, P data);
  public R visit(ASTVarRef node, P data);
  public R visit(ASTList node, P data);
  public R visit(ASTMap node, P data);
  public R visit(ASTKeyValue node, P data);
  public R visit(ASTStaticField node, P data);
  public R visit(ASTCtor node, P data);
  public R visit(ASTProperty node, P data);
  public R visit(ASTStaticMethod node, P data);
  public R visit(ASTMethod node, P data);
  public R visit(ASTProject node, P data);
  public R visit(ASTSelect node, P data);
  public R visit(ASTSelectFirst node, P data);
  public R visit(ASTSelectLast node, P data);
}
