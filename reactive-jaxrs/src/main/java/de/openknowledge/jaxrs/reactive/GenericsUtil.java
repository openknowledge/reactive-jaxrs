package de.openknowledge.jaxrs.reactive;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * @author Arne Limburg - open knowledge GmbH
 * @author Christian Schulz - open knowledge GmbH
 */
public class GenericsUtil {
  public static <V> Class<V> fromGenericType(Class<?> subclass, Class<?> superclass) {
    return fromGenericType(subclass, null, superclass, 0);
  }

  public static <V> Class<V> fromGenericType(Type subtype, Class<?> superclass, int parameterIndex) {
    if (subtype instanceof Class) {
      return fromGenericType((Class<?>)subtype, null, superclass, parameterIndex);
    } else if (subtype instanceof ParameterizedType) {
      ParameterizedType parameterizedSubtype = (ParameterizedType)subtype;
      Class<?> rawSubtype = getRawType(parameterizedSubtype);
      if (rawSubtype == superclass) {
        return getRawType(parameterizedSubtype.getActualTypeArguments()[parameterIndex]);
      }
      return fromGenericType(rawSubtype, parameterizedSubtype, superclass, parameterIndex);
    } else {
      throw new IllegalArgumentException("Unsupported type " + subtype);
    }
  }

  public static <V> Class<V> fromGenericType(Class<?> subclass, ParameterizedType subtype, Class<?> superclass, int parameterIndex) {
    Class<?> directSubclass = getDirectSubclass(subclass, superclass);
    if (subtype != null && directSubclass == subtype.getRawType()) {
      return getRawType(subtype.getActualTypeArguments()[parameterIndex]);
    }
    Type genericSuperclass = getGenericSuperclass(directSubclass, superclass);
    if (!(genericSuperclass instanceof ParameterizedType)) {
      throw new IllegalStateException("Generic type argument missing for superclass " + superclass.getSimpleName());
    }
    ParameterizedType parameterizedSuperclass = (ParameterizedType)genericSuperclass;
    Type valueType = parameterizedSuperclass.getActualTypeArguments()[parameterIndex];
    if (valueType instanceof TypeVariable) {
      TypeVariable<?> variable = (TypeVariable<?>)valueType;
      TypeVariable<?>[] typeParameters = directSubclass.getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        if (typeParameters[i].getName().equals(variable.getName())) {
          return fromGenericType(subclass, subtype, directSubclass, i);
        }
      }
      throw new IllegalStateException(variable + " cannot be resolved");
    }
    return (Class<V>)valueType;
  }
  private static Class<?> getDirectSubclass(Class<?> subclass, Class<?> superclass) {
    if (!superclass.isAssignableFrom(subclass)) {
      throw new IllegalStateException(subclass.getSimpleName() + " is no subclass of " + superclass.getSimpleName());
    }
    if (superclass.isInterface()) {
      for (Class<?> iface: subclass.getInterfaces()) {
        if (iface.equals(superclass)) {
          return subclass;
        }
      }
      return getDirectSubclass(subclass.getSuperclass(), superclass);
    }
    while (subclass.getSuperclass() != superclass) {
      subclass = subclass.getSuperclass();
    }
    return subclass;
  }
  private static Type getGenericSuperclass(Class<?> subclass, Class<?> superclass) {
    if (!superclass.isInterface()) {
      return subclass.getGenericSuperclass();
    }
    int index = Arrays.asList(subclass.getInterfaces()).indexOf(superclass);
    return subclass.getGenericInterfaces()[index];
  }
  public static ParameterizedType getParameterizedType(Type type)
  {
    if (type instanceof ParameterizedType)
    {
      return (ParameterizedType)type;
    }
    else if (type instanceof Class)
    {
      Class<?> classType = (Class<?>)type;
      return new OwbParametrizedTypeImpl(classType.getDeclaringClass(), classType, classType.getTypeParameters());
    }
    else
    {
      throw new IllegalArgumentException(type.getClass().getSimpleName() + " is not supported");
    }
  }
  public static <T> Class<T> getRawType(Type type)
  {
    return getRawType(type, null);
  }
  static <T> Class<T> getRawType(Type type, Type actualType)
  {
    if (type instanceof Class)
    {
      return (Class<T>)type;
    }
    else if (type instanceof ParameterizedType)
    {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      return getRawType(parameterizedType.getRawType(), actualType);
    }
    else if (type instanceof TypeVariable)
    {
      TypeVariable<?> typeVariable = (TypeVariable<?>)type;
      Type mostSpecificType = getMostSpecificType(getRawTypes(typeVariable.getBounds(), actualType), typeVariable.getBounds());
      return getRawType(mostSpecificType, actualType);
    }
    else if (type instanceof WildcardType)
    {
      WildcardType wildcardType = (WildcardType)type;
      Type mostSpecificType = getMostSpecificType(getRawTypes(wildcardType.getUpperBounds(), actualType), wildcardType.getUpperBounds());
      return getRawType(mostSpecificType, actualType);
    }
    else if (type instanceof GenericArrayType)
    {
      GenericArrayType arrayType = (GenericArrayType)type;
      return getRawType(createArrayType(getRawType(arrayType.getGenericComponentType(), actualType)), actualType);
    }
    else
    {
      throw new IllegalArgumentException("Unsupported type " + type.getClass().getName());
    }
  }
  private static <T> Class<T>[] getRawTypes(Type[] types, Type actualType)
  {
    Class<T>[] rawTypes = new Class[types.length];
    for (int i = 0; i < types.length; i++)
    {
      rawTypes[i] = getRawType(types[i], actualType);
    }
    return rawTypes;
  }
  private static Type getMostSpecificType(Class<?>[] types, Type[] genericTypes)
  {
    Class<?> mostSpecificType = types[0];
    int mostSpecificIndex = 0;
    for (int i = 0; i < types.length; i++)
    {
      if (mostSpecificType.isAssignableFrom(types[i]))
      {
        mostSpecificType = types[i];
        mostSpecificIndex = i;
      }
    }
    return genericTypes[mostSpecificIndex];
  }
  private static Type createArrayType(Type componentType)
  {
    if (componentType instanceof Class)
    {
      return Array.newInstance((Class<?>)componentType, 0).getClass();
    }
    else
    {
      return new OwbGenericArrayTypeImpl(componentType);
    }
  }
  public static class OwbGenericArrayTypeImpl implements GenericArrayType
  {
    private Type componentType;
    public OwbGenericArrayTypeImpl(Type componentType)
    {
      this.componentType = componentType;
    }
    @Override
    public Type getGenericComponentType()
    {
      return componentType;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
      return componentType.hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
      if (this == obj)
      {
        return true;
      }
      else if (obj instanceof GenericArrayType)
      {
        return ((GenericArrayType)obj).getGenericComponentType().equals(componentType);
      }
      else
      {
        return false;
      }
    }
    public String toString()
    {
      return componentType + "[]";
    }
  }
  public static class OwbParametrizedTypeImpl implements ParameterizedType
  {
    /**Owner type*/
    private final Type owner;
    /**Raw type*/
    private final Type rawType;
    /**Actual type arguments*/
    private final Type[] types;
    /**
     * New instance.
     * @param owner owner
     * @param raw raw
     */
    public OwbParametrizedTypeImpl(Type owner, Type raw, Type... types)
    {
      this.owner = owner;
      rawType = raw;
      this.types = types;
    }
    @Override
    public Type[] getActualTypeArguments()
    {
      return types.clone();
    }
    @Override
    public Type getOwnerType()
    {
      return owner;
    }
    @Override
    public Type getRawType()
    {
      return rawType;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
      return Arrays.hashCode(types) ^ (owner == null ? 0 : owner.hashCode()) ^ (rawType == null ? 0 : rawType.hashCode());
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
      if (this == obj)
      {
        return true;
      }
      else if (obj instanceof ParameterizedType)
      {
        ParameterizedType that = (ParameterizedType) obj;
        Type thatOwnerType = that.getOwnerType();
        Type thatRawType = that.getRawType();
        return (owner == null ? thatOwnerType == null : owner.equals(thatOwnerType))
          && (rawType == null ? thatRawType == null : rawType.equals(thatRawType))
          && Arrays.equals(types, that.getActualTypeArguments());
      }
      else
      {
        return false;
      }
    }
    public String toString()
    {
      StringBuilder buffer = new StringBuilder();
      buffer.append(((Class<?>) rawType).getSimpleName());
      Type[] actualTypes = getActualTypeArguments();
      if(actualTypes.length > 0)
      {
        buffer.append("<");
        int length = actualTypes.length;
        for(int i=0;i<length;i++)
        {
          if (actualTypes[i] instanceof Class)
          {
            buffer.append(((Class<?>)actualTypes[i]).getSimpleName());
          }
          else
          {
            buffer.append(actualTypes[i].toString());
          }
          if(i != actualTypes.length-1)
          {
            buffer.append(",");
          }
        }
        buffer.append(">");
      }
      return buffer.toString();
    }
  }
}