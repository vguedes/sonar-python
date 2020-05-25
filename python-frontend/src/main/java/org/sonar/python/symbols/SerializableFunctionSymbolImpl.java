package org.sonar.python.symbols;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.plugins.python.api.LocationInFile;
import org.sonar.plugins.python.api.symbols.FunctionSymbol;
import org.sonar.plugins.python.api.symbols.Symbol;
import org.sonar.plugins.python.api.types.InferredType;
import org.sonar.python.semantic.FunctionSymbolImpl;
import org.sonar.python.types.InferredTypes;

public class SerializableFunctionSymbolImpl implements SerializableFunctionSymbol {
  private final String name;
  @Nullable
  private final String fullyQualifiedName;
  private final List<SerializableParameter> parameters;
  private final boolean isStub;
  private final boolean isInstanceMethod;
  private final List<String> decorators;
  private final LocationInFile definitionLocation;

  public SerializableFunctionSymbolImpl(String name, @Nullable String fullyQualifiedName, List<SerializableParameter> parameters, boolean isStub,
                                        boolean isInstanceMethod, List<String> decorators, LocationInFile definitionLocation
                                        ) {

    this.name = name;
    this.fullyQualifiedName = fullyQualifiedName;
    this.parameters = parameters;
    this.isStub = isStub;
    this.isInstanceMethod = isInstanceMethod;
    this.decorators = decorators;
    this.definitionLocation = definitionLocation;
  }

  @Override
  public List<SerializableParameter> parameters() {
    return parameters;
  }

  @Override
  public boolean isStub() {
    return isStub;
  }

  @Override
  public boolean isInstanceMethod() {
    return isInstanceMethod;
  }

  @Override
  public List<String> decorators() {
    return decorators;
  }

  @CheckForNull
  @Override
  public LocationInFile definitionLocation() {
    return definitionLocation;
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
    boolean hasDecorators = !decorators.isEmpty();
    parameters.stream().map(parameter -> {
      return new FunctionSymbolImpl.ParameterImpl(parameter.name(), InferredTypes.anyType(), );
    })
    return new FunctionSymbolImpl(name, fullyQualifiedName, false, isInstanceMethod, hasDecorators, parameters);
  }
}
