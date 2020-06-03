package org.sonar.python.semantic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.python.api.symbols.Symbol;

public class SymbolDeserializer {

  private final Map<String, Symbol> deserializedSymbolsByFqn = new HashMap<>();

  @CheckForNull
  Symbol deserializeSymbol(@Nullable SerializableSymbol serializableSymbol, Function<String, Symbol> symbolProvider) {
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
        .map(symbolProvider)
        .forEach(superClass -> {
          if (superClass != null) {
            classSymbol.addSuperClass(superClass);
          } else {
            classSymbol.setHasSuperClassWithoutSymbol();
          }
        });
    }
    return deserializedSymbol;
  }

  @CheckForNull
  Set<Symbol> deserializeSymbols(@Nullable Set<SerializableSymbol> serializableSymbols, Function<String, Symbol> symbolProvider) {
    if (serializableSymbols == null) {
      return null;
    }
    return serializableSymbols.stream()
      .map(serializableSymbol -> deserializeSymbol(serializableSymbol, symbolProvider))
      .collect(Collectors.toSet());
  }
}
