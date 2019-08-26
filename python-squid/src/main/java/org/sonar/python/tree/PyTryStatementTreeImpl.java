/*
 * SonarQube Python Plugin
 * Copyright (C) 2011-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.python.tree;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.python.api.tree.PyElseStatementTree;
import org.sonar.python.api.tree.PyExceptClauseTree;
import org.sonar.python.api.tree.PyFinallyClauseTree;
import org.sonar.python.api.tree.PyStatementTree;
import org.sonar.python.api.tree.PyTreeVisitor;
import org.sonar.python.api.tree.PyTryStatementTree;

public class PyTryStatementTreeImpl extends PyTree implements PyTryStatementTree {
  private final Token tryKeyword;
  private final List<PyStatementTree> tryBody;
  private final List<PyExceptClauseTree> exceptClauses;
  private final PyFinallyClauseTree finallyClause;
  private final PyElseStatementTree elseStatement;

  public PyTryStatementTreeImpl(AstNode astNode, Token tryKeyword, List<PyStatementTree> tryBody, List<PyExceptClauseTree> exceptClauses, PyFinallyClauseTree finallyClause, PyElseStatementTree elseStatement) {
    super(astNode);
    this.tryKeyword = tryKeyword;
    this.tryBody = tryBody;
    this.exceptClauses = exceptClauses;
    this.finallyClause = finallyClause;
    this.elseStatement = elseStatement;
  }

  @Override
  public Token tryKeyword() {
    return tryKeyword;
  }

  @Override
  public List<PyExceptClauseTree> exceptClauses() {
    return exceptClauses;
  }

  @CheckForNull
  @Override
  public PyFinallyClauseTree finallyClause() {
    return finallyClause;
  }

  @CheckForNull
  @Override
  public PyElseStatementTree elseClause() {
    return elseStatement;
  }

  @Override
  public List<PyStatementTree> body() {
    return tryBody;
  }

  @Override
  public Kind getKind() {
    return Kind.TRY_STMT;
  }

  @Override
  public void accept(PyTreeVisitor visitor) {
    visitor.visitTryStatement(this);
  }
}