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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.python.api.PythonFile;
import org.sonar.plugins.python.api.symbols.Symbol;
import org.sonar.plugins.python.api.symbols.Usage;
import org.sonar.plugins.python.api.tree.FileInput;

public class ProjectLevelSymbolTable {

  private final Map<String, Set<SerializableSymbol>> globalSymbolsByModuleName;
  private Map<String, SerializableSymbol> globalSymbolsByFQN;

  public static ProjectLevelSymbolTable empty() {
    return new ProjectLevelSymbolTable(Collections.emptyMap());
  }

  public static ProjectLevelSymbolTable from(Map<String, Set<Symbol>> globalSymbolsByModuleName) {
    return new ProjectLevelSymbolTable(globalSymbolsByModuleName);
  }

  public ProjectLevelSymbolTable() {
    this.globalSymbolsByModuleName = new HashMap<>();
  }

  private ProjectLevelSymbolTable(Map<String, Set<Symbol>> globalSymbolsByModuleName) {
    this.globalSymbolsByModuleName = new HashMap<>();
    globalSymbolsByModuleName.forEach((moduleName, exportedSymbols) -> {
      Set<SerializableSymbol> serializableSymbols = exportedSymbols.stream()
        .map(symbol -> ((SymbolImpl) symbol).toSerializableSymbol())
        .collect(Collectors.toSet());
      this.globalSymbolsByModuleName.put(moduleName, serializableSymbols);
    });
  }

  public void addModule(FileInput fileInput, String packageName, PythonFile pythonFile) {
    SymbolTableBuilder symbolTableBuilder = new SymbolTableBuilder(packageName, pythonFile);
    String fullyQualifiedModuleName = SymbolUtils.fullyQualifiedModuleName(packageName, pythonFile.fileName());
    fileInput.accept(symbolTableBuilder);
    Set<SerializableSymbol> exportedSymbols = new HashSet<>();
    for (Symbol exportedSymbol : fileInput.globalVariables()) {
      String fullyQualifiedVariableName = exportedSymbol.fullyQualifiedName();
      if (((fullyQualifiedVariableName != null) && !fullyQualifiedVariableName.startsWith(fullyQualifiedModuleName)) ||
        exportedSymbol.usages().stream().anyMatch(u -> u.kind().equals(Usage.Kind.IMPORT))) {
        // TODO: We don't put builtin or imported names in global symbol table to avoid duplicate FQNs in project level symbol table (to fix with SONARPY-647)
        continue;
      }
      exportedSymbols.add(((SymbolImpl) exportedSymbol).toSerializableSymbol());
    }
    globalSymbolsByModuleName.put(fullyQualifiedModuleName, exportedSymbols);
  }

  private Map<String, SerializableSymbol> globalSymbolsByFQN() {
    if (globalSymbolsByFQN == null) {
      globalSymbolsByFQN = globalSymbolsByModuleName.values()
        .stream()
        .flatMap(Collection::stream)
        .filter(symbol -> symbol.fullyQualifiedName() != null)
        .collect(Collectors.toMap(SerializableSymbol::fullyQualifiedName, Function.identity(), (a, b) -> {
//          System.out.println("a -> name = " + a.name() + ", fqn = " + a.fullyQualifiedName());
//          System.out.println("b -> name = " + b.name() + ", fqn = " + b.fullyQualifiedName());
          return null;
        }));
    }
    return globalSymbolsByFQN;
  }

  @CheckForNull
  public SerializableSymbol getSymbol(@Nullable String fullyQualifiedName) {
    return globalSymbolsByFQN().get(fullyQualifiedName);
  }

  @CheckForNull
  public Set<SerializableSymbol> getSymbolsFromModule(@Nullable String moduleName) {
    return globalSymbolsByModuleName.get(moduleName);
  }

}
