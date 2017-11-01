/*
 * Copyright (C) open knowledge GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package de.openknowledge.jaxrs.reactive;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

// TODO Replace with GenericUtils or similar stuff
public class Utils {
  public static Type getGenericInterfaces(Type cls, Class<?> search) {
    while (cls != null) {
      Type[] interfaces = new Type[]{};
      if (cls instanceof Class) {
        interfaces = ((Class)cls).getGenericInterfaces();
      } else if (cls instanceof ParameterizedType) {
        interfaces = ((Class)((ParameterizedType)cls).getRawType()).getGenericInterfaces();
      }

      for (final Type i : interfaces) {
        if (((ParameterizedType)i).getRawType().equals(search)) {
          return i;
        }

        Type genericInterface = getGenericInterfaces(i, search);
        if (genericInterface != null) {
          return i;
        }
      }

      if (cls instanceof Class) {
        cls = ((Class)cls).getSuperclass();
      } else {
        cls = null;
      }
    }

    return null;
  }

  public static Type extractGenericTypeFromInterface(Class<?> type, Class<?> interfaceClass) {
    Type foundInterface = getGenericInterfaces(type, interfaceClass);

    if (foundInterface == null) {
      throw new IllegalArgumentException();
    } else {
      return ((ParameterizedType)foundInterface).getActualTypeArguments()[0];
    }
  }

  public static Class<?> extractClassFromType(Type type) {
    if (type instanceof Class) {
      return (Class)type;
    } else if (type instanceof ParameterizedType) {
      return (Class)((ParameterizedType)type).getRawType();
    } else {
      throw new IllegalArgumentException();
    }
  }
}
