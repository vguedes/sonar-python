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
package org.sonar.python.types;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.sonar.python.semantic.ClassSymbolImpl;
import org.sonar.python.semantic.FunctionSymbolImpl;
import org.sonar.python.semantic.SymbolImpl;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.python.types.InferredTypes.or;

public class RuntimeTypeTest {

  private final ClassSymbolImpl a = new ClassSymbolImpl("a", "a");
  private final ClassSymbolImpl b = new ClassSymbolImpl("b", "b");
  private final ClassSymbolImpl c = new ClassSymbolImpl("c", "c");

  @Test
  public void isIdentityComparableWith() {
    RuntimeType aType = new RuntimeType(a);
    RuntimeType bType = new RuntimeType(b);
    RuntimeType cType = new RuntimeType(c);

    assertThat(aType.isIdentityComparableWith(bType)).isFalse();
    assertThat(aType.isIdentityComparableWith(aType)).isTrue();
    assertThat(aType.isIdentityComparableWith(new RuntimeType(a))).isTrue();

    assertThat(aType.isIdentityComparableWith(AnyType.ANY)).isTrue();

    assertThat(aType.isIdentityComparableWith(or(aType, bType))).isTrue();
    assertThat(aType.isIdentityComparableWith(or(cType, bType))).isFalse();

    assertThat(aType.isIdentityComparableWith(new DeclaredType(a))).isTrue();
    assertThat(aType.isIdentityComparableWith(new DeclaredType(b))).isTrue();
  }

  @Test
  public void member() {
    ClassSymbolImpl x = new ClassSymbolImpl("x", "x");
    SymbolImpl foo = new SymbolImpl("foo", null);
    x.addMembers(singletonList(foo));
    assertThat(new RuntimeType(x).canHaveMember("foo")).isTrue();
    assertThat(new RuntimeType(x).canHaveMember("bar")).isFalse();
    assertThat(new RuntimeType(x).resolveMember("foo")).contains(foo);
    assertThat(new RuntimeType(x).resolveMember("bar")).isEmpty();

    ClassSymbolImpl x1 = new ClassSymbolImpl("x1", "x1");
    x1.addSuperClass(x);
    x1.addMembers(singletonList(new SymbolImpl("bar", null)));
    assertThat(new RuntimeType(x1).canHaveMember("foo")).isTrue();
    assertThat(new RuntimeType(x1).canHaveMember("bar")).isTrue();
    assertThat(new RuntimeType(x1).resolveMember("foo")).contains(foo);

    ClassSymbolImpl y = new ClassSymbolImpl("y", "y");
    assertThat(new RuntimeType(y).canHaveMember("foo")).isFalse();
  }

  @Test
  public void overridden_symbol() {
    ClassSymbolImpl x = new ClassSymbolImpl("x", "x");
    SymbolImpl fooX = new SymbolImpl("foo", null);
    x.addMembers(singletonList(fooX));
    assertThat(new RuntimeType(x).resolveMember("foo")).contains(fooX);

    ClassSymbolImpl x1 = new ClassSymbolImpl("x1", "x1");
    SymbolImpl fooX1 = new SymbolImpl("foo", null);
    x1.addSuperClass(x);
    x1.addMembers(singletonList(fooX1));
    assertThat(new RuntimeType(x1).resolveMember("foo")).contains(fooX1);

    ClassSymbolImpl y = new ClassSymbolImpl("y", "y");
    y.addMembers(singletonList(new SymbolImpl("foo", null)));
    ClassSymbolImpl z = new ClassSymbolImpl("z", "z");
    z.addSuperClass(x);
    z.addSuperClass(y);
    // TODO should be empty when multiple superclasses have the same member name
    assertThat(new RuntimeType(z).resolveMember("foo")).contains(fooX);
  }

  @Test
  public void cycle_between_super_types() {
    ClassSymbolImpl x = new ClassSymbolImpl("x", "x");
    ClassSymbolImpl y = new ClassSymbolImpl("y", "y");
    ClassSymbolImpl z = new ClassSymbolImpl("z", "z");
    x.addSuperClass(y);
    y.addSuperClass(z);
    z.addSuperClass(x);
    assertThat(new RuntimeType(x).canHaveMember("foo")).isFalse();
  }

  @Test
  public void unresolved_type_hierarchy() {
    ClassSymbolImpl x = new ClassSymbolImpl("x", "x");
    x.setHasSuperClassWithoutSymbol();
    assertThat(new RuntimeType(x).canHaveMember("foo")).isTrue();

    ClassSymbolImpl y = new ClassSymbolImpl("y", "y");
    y.addSuperClass(x);
    assertThat(new RuntimeType(y).canHaveMember("foo")).isTrue();
  }

  @Test
  public void test_equals() {
    RuntimeType aType = new RuntimeType(a);
    assertThat(aType.equals(aType)).isTrue();
    assertThat(aType.equals(new RuntimeType(a))).isTrue();
    assertThat(aType.equals(new RuntimeType(b))).isFalse();
    assertThat(aType.equals(a)).isFalse();
    assertThat(aType.equals(null)).isFalse();

    ClassSymbolImpl aWithSuperClass = new ClassSymbolImpl("a", "a");
    aWithSuperClass.addSuperClass(b);
    RuntimeType aTypeWithSuperClass = new RuntimeType(aWithSuperClass);
    ClassSymbolImpl aWithSuperClass2 = new ClassSymbolImpl("a", "a");
    aWithSuperClass2.addSuperClass(b);
    RuntimeType aTypeWithSuperClass2 = new RuntimeType(aWithSuperClass2);
    assertThat(aTypeWithSuperClass).isNotEqualTo(aType);
    assertThat(aTypeWithSuperClass).isEqualTo(aTypeWithSuperClass2);

    ClassSymbolImpl aWithMember = new ClassSymbolImpl("a", "a");
    aWithMember.addMembers(Collections.singleton(new SymbolImpl("fn", "a.fn")));
    RuntimeType aTypeWithMember = new RuntimeType(aWithMember);
    ClassSymbolImpl aWithMember2 = new ClassSymbolImpl("a", "a");
    aWithMember2.addMembers(Collections.singleton(new SymbolImpl("fn", "a.fn")));
    RuntimeType aTypeWithMember2 = new RuntimeType(aWithMember2);
    assertThat(aTypeWithMember).isNotEqualTo(aType);
    assertThat(aTypeWithMember).isEqualTo(aTypeWithMember2);

    RuntimeType x = new RuntimeType(new ClassSymbolImpl("X", null));
    RuntimeType y = new RuntimeType(new ClassSymbolImpl("Y", null));
    assertThat(x).isNotEqualTo(y);
  }

  @Test
  public void test_hashCode() {
    RuntimeType aType = new RuntimeType(a);
    assertThat(aType.hashCode()).isEqualTo(new RuntimeType(a).hashCode());
    assertThat(aType.hashCode()).isNotEqualTo(new RuntimeType(b).hashCode());

    RuntimeType x = new RuntimeType(new ClassSymbolImpl("X", null));
    RuntimeType y = new RuntimeType(new ClassSymbolImpl("Y", null));
    assertThat(x.hashCode()).isNotEqualTo(y.hashCode());
  }

  @Test
  public void test_toString() {
    assertThat(new RuntimeType(a).toString()).isEqualTo("RuntimeType(a)");
  }

  @Test
  public void test_canOnlyBe() {
    assertThat(new RuntimeType(a).canOnlyBe("a")).isTrue();
    assertThat(new RuntimeType(b).canOnlyBe("a")).isFalse();
  }

  @Test
  public void test_canBeOrExtend() {
    ClassSymbolImpl x = new ClassSymbolImpl("x", "x");
    assertThat(new RuntimeType(x).canBeOrExtend("x")).isTrue();
    assertThat(new RuntimeType(x).canBeOrExtend("y")).isFalse();

    ClassSymbolImpl x1 = new ClassSymbolImpl("x1", "x1");
    ClassSymbolImpl x2 = new ClassSymbolImpl("x2", "x2");
    x2.addSuperClass(x1);
    assertThat(new RuntimeType(x2).canBeOrExtend("x1")).isTrue();

    ClassSymbolImpl y = new ClassSymbolImpl("y", "y");
    y.addSuperClass(new SymbolImpl("unknown", null));
    assertThat(new RuntimeType(y).canBeOrExtend("z")).isTrue();
  }

  @Test
  public void test_isCompatibleWith() {
    ClassSymbolImpl x1 = new ClassSymbolImpl("x1", "x1");
    ClassSymbolImpl x2 = new ClassSymbolImpl("x2", "x2");
    x2.addSuperClass(x1);

    assertThat(new RuntimeType(x2).isCompatibleWith(new RuntimeType(x1))).isTrue();
    assertThat(new RuntimeType(x1).isCompatibleWith(new RuntimeType(x1))).isTrue();
    assertThat(new RuntimeType(x1).isCompatibleWith(new RuntimeType(x2))).isFalse();

    assertThat(new RuntimeType(x2).isCompatibleWith(new DeclaredType(x1))).isTrue();
    assertThat(new RuntimeType(x1).isCompatibleWith(new DeclaredType(x1))).isTrue();
    assertThat(new RuntimeType(x1).isCompatibleWith(new DeclaredType(x2))).isFalse();

    ClassSymbolImpl a = new ClassSymbolImpl("a", null);
    ClassSymbolImpl b = new ClassSymbolImpl("b", "b");
    assertThat(new RuntimeType(a).isCompatibleWith(new RuntimeType(b))).isFalse();
    assertThat(new RuntimeType(b).isCompatibleWith(new RuntimeType(a))).isTrue();

    ClassSymbolImpl y = new ClassSymbolImpl("y", "y");
    ClassSymbolImpl z = new ClassSymbolImpl("z", "z");
    y.addSuperClass(new SymbolImpl("unknown", null));
    assertThat(new RuntimeType(y).isCompatibleWith(new RuntimeType(z))).isTrue();

    ClassSymbolImpl duck = new ClassSymbolImpl("duck", "duck");
    ClassSymbolImpl goose = new ClassSymbolImpl("goose", "goose");
    FunctionSymbolImpl duckSwim = new FunctionSymbolImpl("swim", "duck.swim", false, false, false, false, Collections.emptyList(),
      Collections.emptyList());
    FunctionSymbolImpl duckQuack = new FunctionSymbolImpl("quack", "duck.quack", false, false, false, false, Collections.emptyList(),
      Collections.emptyList());
    FunctionSymbolImpl gooseSwim = new FunctionSymbolImpl("swim", "goose.swim", false, false, false, false, Collections.emptyList(),
      Collections.emptyList());
    duck.addMembers(Arrays.asList(duckSwim, duckQuack));
    goose.addMembers(Collections.singleton(gooseSwim));
    assertThat(new RuntimeType(duck).isCompatibleWith(new RuntimeType(goose))).isTrue();
    assertThat(new RuntimeType(goose).isCompatibleWith(new RuntimeType(duck))).isFalse();
  }
}
