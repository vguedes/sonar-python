package org.sonar.python.symbols;

import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public class SerializableClassSymbolImpl implements SerializableClassSymbol {

  private final String name;
  @Nullable
  private final String fullyQualifiedName;
  private final List<String> superClasses;
  private final Set<String> declaredMembers;

  public SerializableClassSymbolImpl(String name, @Nullable String fullyQualifiedName,
                                     List<String> superClasses, Set<String> declaredMembers) {
    this.name = name;
    this.fullyQualifiedName = fullyQualifiedName;
    this.superClasses = superClasses;
    this.declaredMembers = declaredMembers;
  }

  @Override
  public List<String> superClasses() {
    return superClasses;
  }

  @Override
  public Set<String> declaredMembers() {
    return declaredMembers;
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
