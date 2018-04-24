package de.openknowledge.jaxrs.reactive;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.util.TypeLiteral;

import org.junit.Test;

/**
 * @author Arne Limburg - open knowledge GmbH
 */
public class GenericsUtilTest {

  @Test
  public void genericSubclass() {
    assertThat(GenericsUtil.fromGenericType(new TypeLiteral<GenericNumberSubclass<Integer>>() {}.getType(), GenericSuperclass.class, 0)).isEqualTo(Integer.class);
  }

  @Test
  public void genericInterface() {
    assertThat(GenericsUtil.fromGenericType(new TypeLiteral<ParameterizedInterface<Integer>>() {}.getType(), ParameterizedInterface.class, 0)).isEqualTo(Integer.class);
  }

  @Test
  public void stringSubclass() {
    assertThat(GenericsUtil.fromGenericType(StringSubclass.class, GenericSuperclass.class, 0)).isEqualTo(String.class);
  }

  @Test
  public void integerSubclass() {
    assertThat(GenericsUtil.fromGenericType(IntegerSubclass.class, GenericSuperclass.class, 0)).isEqualTo(Integer.class);
  }

  @Test(expected = IllegalStateException.class)
  public void rawSuperclassFails() {
    GenericsUtil.fromGenericType(RawSubclass.class, GenericSuperclass.class, 0);
  }

  @Test(expected = IllegalStateException.class)
  public void unresolvableSuperclassFails() {
    GenericsUtil.fromGenericType(OuterClass.UnspecificSubclass.class, GenericSuperclass.class, 0);
  }

  @Test(expected = IllegalStateException.class)
  public void superclassIsNotAssignableFromSubclass() {
    try {
      GenericsUtil.fromGenericType(Long.class, Integer.class, 0);
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("is no subclass of");
      throw e;
    }
  }

  @Test
  public void multipleGenericParameters() {
    assertThat(GenericsUtil.fromGenericType(MultiparameterSubclass.class, MultiparameterSuperclass.class, 0))
      .isEqualTo(String.class);
    assertThat(GenericsUtil.fromGenericType(MultiparameterSubclass.class, MultiparameterSuperclass.class, 1))
      .isEqualTo(Integer.class);
    assertThat(GenericsUtil.fromGenericType(MultiparameterSwitchingSubclass.class, MultiparameterSuperclass.class, 0))
      .isEqualTo(Integer.class);
    assertThat(GenericsUtil.fromGenericType(MultiparameterSwitchingSubclass.class, MultiparameterSuperclass.class, 1))
      .isEqualTo(String.class);
  }

  @Test
  public void interfaceWithParameters() {
    assertThat(GenericsUtil.fromGenericType(MultiparameterSwitchingSubclass.class, ParameterizedInterface.class, 0))
      .isEqualTo(Integer.class);
  }

  public abstract static class GenericSuperclass<A> {
  }
  
  public static class GenericNumberSubclass<N extends Number> extends GenericSuperclass<N> {
  }
  
  public static class IntegerSubclass extends GenericNumberSubclass<Integer> {
  }
  
  public static class StringSubclass extends GenericSuperclass<String> {
  }
  
  public static class RawSubclass extends GenericSuperclass {
  }
  
  public static class OuterClass<X> {
    public class UnspecificSubclass extends GenericSuperclass<X> {
    }
  }
  
  public static class MultiparameterSuperclass<A, B> {
  }
  
  public static class MultiparameterInbetweenClass<A, B> extends MultiparameterSuperclass<B, A> implements ParameterizedInterface<B> {
  }
  
  public static class MultiparameterSubclass extends MultiparameterSuperclass<String, Integer> {
  }
  
  public static class MultiparameterSwitchingSubclass extends MultiparameterInbetweenClass<String, Integer> {
  }
  
  public interface ParameterizedInterface<A> {
  }
}