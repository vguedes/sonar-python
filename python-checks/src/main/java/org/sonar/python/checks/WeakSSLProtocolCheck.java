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
package org.sonar.python.checks;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.python.PythonSubscriptionCheck;
import org.sonar.python.api.tree.PyNameTree;
import org.sonar.python.api.tree.Tree;
import org.sonar.python.semantic.Symbol;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck extends PythonSubscriptionCheck {
  private static final List<String> WEAK_PROTOCOL_CONSTANTS = Arrays.asList(
    "ssl.PROTOCOL_SSLv2",
    "ssl.PROTOCOL_SSLv3",
    "ssl.PROTOCOL_SSLv23",
    "ssl.PROTOCOL_TLS",
    "ssl.PROTOCOL_TLSv1",
    "ssl.PROTOCOL_TLSv1_1",
    "OpenSSL.SSL.SSLv2_METHOD",
    "OpenSSL.SSL.SSLv3_METHOD",
    "OpenSSL.SSL.SSLv23_METHOD",
    "OpenSSL.SSL.TLSv1_METHOD",
    "OpenSSL.SSL.TLSv1_1_METHOD"
  );

  @Override
  public void initialize(Context context) {
    context.registerSyntaxNodeConsumer(Tree.Kind.NAME, ctx -> {
      PyNameTree pyNameTree = (PyNameTree) ctx.syntaxNode();
      Symbol symbol = ctx.symbolTable().getSymbol(pyNameTree);
      if (isWeakProtocol(pyNameTree, symbol)) {
        ctx.addIssue(pyNameTree, "Change this code to use a stronger protocol.");
      }
    });
  }

  private static boolean isWeakProtocol(PyNameTree pyNameTree, @Nullable Symbol symbol) {
    Predicate<String> matchWeakProtocol;
    if (symbol == null) {
      matchWeakProtocol = s -> s.substring(s.lastIndexOf('.') + 1).equals(pyNameTree.name());
    } else {
      matchWeakProtocol = symbol.qualifiedName()::equals;
    }
    return WEAK_PROTOCOL_CONSTANTS.stream().anyMatch(matchWeakProtocol);
  }
}