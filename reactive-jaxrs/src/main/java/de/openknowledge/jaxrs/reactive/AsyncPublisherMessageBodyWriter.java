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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Flow;
import org.jboss.resteasy.core.AsynchronousResponseInjector;
import org.jboss.resteasy.spi.HttpRequest;
import de.openknowledge.jaxrs.reactive.flow.AsyncPublisher;

@Provider
public class AsyncPublisherMessageBodyWriter implements MessageBodyWriter<AsyncPublisher<?>> {

  @Context
  private HttpRequest request;

  @Context
  private Providers providers;

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    // TODO
    return mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
  }

  @Override
  public long getSize(AsyncPublisher<?> publisher, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(AsyncPublisher<?> publisher, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    // TODO extract to be able to support other containers
    if (!request.getAsyncContext().isSuspended()) {
      ((AsyncResponse)new AsynchronousResponseInjector().inject(request, null)).resume(publisher);
    } else {
      MessageBodyWriter<Flow.Publisher> writer = providers.getMessageBodyWriter(Flow.Publisher.class, type, annotations, mediaType);
      if (writer == null) {
        throw new IllegalArgumentException();
      }
      writer.writeTo(publisher, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }
  }
}
