package org.sonar.python.symbols;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.python.api.LocationInFile;
import org.sonar.plugins.python.api.symbols.FunctionSymbol;

public class SerializableParameterImpl implements SerializableParameter {
  @Nullable
  private final String name;
  private final String declaredType;
  private final boolean hasDefaultValue;
  private final boolean isKeywordOnly;
  private final boolean isPositionalOnly;
  @Nullable
  private final LocationInFile location;


  public SerializableParameterImpl(FunctionSymbol.Parameter parameter) {
    name = parameter.name();
    declaredType = parameter.declaredType().toString();
    hasDefaultValue = parameter.hasDefaultValue();
    isKeywordOnly = parameter.isKeywordOnly();
    isPositionalOnly = parameter.isPositionalOnly();
    location = parameter.location();
  }

  @CheckForNull
  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean hasDefaultValue() {
    return hasDefaultValue;
  }

  @Override
  public boolean isKeywordOnly() {
    return isKeywordOnly;
  }

  @Override
  public boolean isPositionalOnly() {
    return isPositionalOnly;
  }

  @CheckForNull
  @Override
  public LocationInFile location() {
    return location;
  }
}
