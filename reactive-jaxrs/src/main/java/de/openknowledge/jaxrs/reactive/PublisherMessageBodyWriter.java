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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Flow;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class PublisherMessageBodyWriter implements MessageBodyWriter<Flow.Publisher<?>> {

  private ObjectMapper mapper;

  @Context
  private HttpServletResponse response;

  public PublisherMessageBodyWriter() {
    mapper = new ObjectMapper();
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    // TODO condition
    return mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
  }

  @Override
  public long getSize(Flow.Publisher<?> publisher, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Flow.Publisher<?> publisher, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    if (response.getOutputStream() instanceof StreamServletFilter.WrappedServletOutputStream) {
      ((StreamServletFilter.WrappedServletOutputStream)response.getOutputStream()).delayClose();
    } else {
      throw new IllegalStateException("OutputStream is not wrapped! Servlet Filter not registered?");
    }

    Type targetType = ((ParameterizedType)genericType).getActualTypeArguments()[0];

    Class targetClass;

    if (targetType instanceof Class) {
      targetClass = (Class)targetType;
    } else if (targetType instanceof ParameterizedType) {
      targetClass = (Class)((ParameterizedType)targetType).getRawType();
    } else {
      throw new IllegalArgumentException();
    }

    publisher.subscribe(new Flow.Subscriber<Object>() {
      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        // does nothing
      }

      @Override
      public void onNext(Object item) {
        try {
          mapper.writer().forType(targetClass).writeValue(entityStream, item);
        } catch (IOException e) {
          // TODO
          e.printStackTrace();
        }
      }

      @Override
      public void onError(Throwable throwable) {
        // TODO
      }

      @Override
      public void onComplete() {
        // does nothing
      }
    });
  }
}
