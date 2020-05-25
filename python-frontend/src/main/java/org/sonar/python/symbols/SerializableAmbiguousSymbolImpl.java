package org.sonar.python.symbols;

import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public class SerializableAmbiguousSymbolImpl implements SerializableAmbiguousSymbol{

  private final String name;
  @Nullable
  private final String fullyQualifiedName;
  private final Set<SerializableSymbol> alternatives;

  public SerializableAmbiguousSymbolImpl(String name, @Nullable String fullyQualifiedName, Set<SerializableSymbol> alternatives) {
    this.name = name;
    this.fullyQualifiedName = fullyQualifiedName;
    this.alternatives = alternatives;
  }

  @Override
  public Set<SerializableSymbol> alternatives() {
    return alternatives;
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
}
