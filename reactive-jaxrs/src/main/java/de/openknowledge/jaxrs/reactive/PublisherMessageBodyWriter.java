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

import static de.openknowledge.jaxrs.reactive.GenericsUtil.getRawType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Publisher;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

@Provider
public class PublisherMessageBodyWriter implements MessageBodyWriter<Flow.Publisher<?>> {

  private static final Logger LOGGER = Logger.getLogger(PublisherMessageBodyWriter.class.getCanonicalName());

  @Context
  private HttpServletRequest request;

  @Context
  private Providers providers;

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
  public void writeTo(Flow.Publisher<?> publisher, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

    AsyncContext asyncContext;
    if (!request.isAsyncStarted()) {
      asyncContext = request.startAsync();
    } else {
      asyncContext = request.getAsyncContext();
    }

    Type targetType = GenericsUtil.fromGenericType(genericType, Publisher.class, 0);
    Class<?> targetClass = getRawType(targetType);

    MessageBodyWriter entityWriter = providers.getMessageBodyWriter(targetClass, targetType, annotations, mediaType);
    if (entityWriter == null) {
      LOGGER.log(Level.SEVERE, "No MessageBodyWriter was found for {0}, {1}, {2}, {3}", new Object[]{targetClass, targetType, annotations, mediaType});
      throw new IllegalArgumentException();
    }
    entityWriter.writeTo(null, targetClass, targetType, annotations, mediaType, httpHeaders, new ByteArrayOutputStream());

    publisher.subscribe(new AsyncContextMessageBodyWriterSubscriber(asyncContext, entityWriter, targetClass, targetType, annotations, mediaType, httpHeaders));
  }
}
