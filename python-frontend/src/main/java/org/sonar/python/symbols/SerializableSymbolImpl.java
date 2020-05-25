package org.sonar.python.symbols;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.python.api.symbols.Symbol;
import org.sonar.python.semantic.SymbolImpl;

public class SerializableSymbolImpl implements SerializableSymbol {

  private final String name;
  @Nullable
  private final String fullyQualifiedName;

  public SerializableSymbolImpl(String name, @Nullable String fullyQualifiedName) {
    this.name = name;
    this.fullyQualifiedName = fullyQualifiedName;
  }

  @Override
  public String name() {
    return name;
  }

  @CheckForNull
  @Override
  public String fullyQualifiedName() {
    return fullyQualifiedName;
  }

  @Override
  public Symbol toSymbol() {
    return new SymbolImpl(name, fullyQualifiedName);
  }
}
