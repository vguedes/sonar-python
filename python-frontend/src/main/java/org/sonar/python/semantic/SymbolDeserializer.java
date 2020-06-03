/*
 * SonarQube Python Plugin
 * Copyright (C) 2011-2020 SonarSource SA
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
package org.sonar.python.semantic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.python.api.symbols.Symbol;
import org.sonar.python.types.TypeShed;

public class SymbolDeserializer {

  private final Map<String, Symbol> deserializedSymbolsByFqn = new HashMap<>();
  private final ProjectLevelSymbolTable projectLevelSymbolTable;

  public SymbolDeserializer(ProjectLevelSymbolTable projectLevelSymbolTable) {
    this.projectLevelSymbolTable = projectLevelSymbolTable;
  }

  @CheckForNull
  Symbol deserializeSymbol(@Nullable SerializableSymbol serializableSymbol) {
    if (serializableSymbol == null) {
      return null;
    }
    Symbol deserializedSymbol = deserializedSymbolsByFqn.get(serializableSymbol.fullyQualifiedName());
    if (deserializedSymbol != null) {
      return deserializedSymbol;
    }
    deserializedSymbol = serializableSymbol.toSymbol();
    deserializedSymbolsByFqn.put(deserializedSymbol.fullyQualifiedName(), deserializedSymbol);
    if (deserializedSymbol.is(Symbol.Kind.CLASS)) {
      ClassSymbolImpl classSymbol = (ClassSymbolImpl) deserializedSymbol;
      ((SerializableClassSymbol) serializableSymbol).superClasses().stream()
        .map(this::resolveSymbolFromFqn)
        .forEach(superClass -> {
          if (superClass != null) {
            classSymbol.addSuperClass(superClass);
          } else {
            classSymbol.setHasSuperClassWithoutSymbol();
          }
        });
      Set<Symbol> members = ((SerializableClassSymbol) serializableSymbol).declaredMembers().stream()
        .map(fqn -> deserializeSymbol(projectLevelSymbolTable.getSymbol(fqn)))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
      classSymbol.addMembers(members);
    }
    return deserializedSymbol;
  }

  @CheckForNull
  Set<Symbol> deserializeSymbols(@Nullable Set<SerializableSymbol> serializableSymbols) {
    if (serializableSymbols == null) {
      return null;
    }
    return serializableSymbols.stream()
      .map(this::deserializeSymbol)
      .collect(Collectors.toSet());
  }

  private Symbol resolveSymbolFromFqn(String fullyQualifiedName) {
    Symbol symbol = deserializeSymbol(projectLevelSymbolTable.getSymbol(fullyQualifiedName));
    if (symbol == null) {
      symbol = TypeShed.symbolWithFQN(fullyQualifiedName);
      if (symbol != null) {
        symbol = ((SymbolImpl) symbol).copyWithoutUsages();
      }
    }
    if (symbol == null) {
      // whenever we have a fully qualified name referencing an external library we don't know anything about
      symbol = new SymbolImpl(fullyQualifiedName, fullyQualifiedName);
    }
    return symbol;
  }
}
