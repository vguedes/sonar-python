package org.sonar.python.symbols;

import javax.annotation.CheckForNull;
import org.sonar.plugins.python.api.LocationInFile;

public interface SerializableParameter {
  @CheckForNull
  String name();

  boolean hasDefaultValue();

  boolean isKeywordOnly();

  boolean isPositionalOnly();

  @CheckForNull
  LocationInFile location();
}
