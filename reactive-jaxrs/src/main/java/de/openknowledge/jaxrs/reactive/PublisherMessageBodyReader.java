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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.Flow;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.commons.io.IOUtils;

import de.openknowledge.reactive.charset.DecodingProcessor;
import de.openknowledge.reactive.json.JsonArrayProcessor;
import de.openknowledge.reactive.json.JsonTokenizer;

@Provider
public class PublisherMessageBodyReader implements MessageBodyReader<Flow.Publisher<?>> {

  @Context
  private HttpServletRequest request;

  @Context
  private Providers providers;

  @Override
  public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
    // TODO add condition
    return mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
  }

  @Override
  public Flow.Publisher<?> readFrom(Class<Flow.Publisher<?>> aClass,
                                    Type type,
                                    Annotation[] annotations,
                                    MediaType mediaType,
                                    MultivaluedMap<String, String> headers,
                                    InputStream inputStream) throws IOException {
    Type targetType = ((ParameterizedType) type).getActualTypeArguments()[0];

    Class targetClass;

    // TODO throw exception should be onError
    if (targetType instanceof Class) {
      targetClass = (Class) targetType;
    } else if (targetType instanceof ParameterizedType) {
      targetClass = (Class) ((ParameterizedType) targetType).getRawType();
    } else {
      throw new IllegalArgumentException();
    }

    final MessageBodyReader<?> entityReader = providers.getMessageBodyReader(targetClass, targetType, annotations, mediaType);
    if (entityReader == null) {
      throw new IllegalArgumentException();
    }

    // TODO is it really necessary? next readFrom is otherwise blocking, wtf?!
    entityReader.readFrom(targetClass, targetType, annotations, mediaType, headers, IOUtils.toInputStream(""));

    ServletInputStream servletInputStream = null;
    try {
      servletInputStream = request.getInputStream();
    } catch (IOException e) {
      // TODO
      e.printStackTrace();
    }
    if (servletInputStream == null) {
      throw new IllegalArgumentException();
    }

    request.startAsync();
    String characterEncoding = Optional.ofNullable(request.getCharacterEncoding()).orElse(Charset.defaultCharset().name());
    ServletInputStreamPublisher inputStreamPublisher = new ServletInputStreamPublisher(servletInputStream, 32768);
    DecodingProcessor decodingProcessor = new DecodingProcessor(Charset.forName(characterEncoding), 32768);
    JsonTokenizer tokenizer = new JsonTokenizer();
    JsonArrayProcessor arrayProcessor = new JsonArrayProcessor();
    MessageBodyReaderProcessor readerProcessor = new MessageBodyReaderProcessor(entityReader, targetClass, targetType, annotations, mediaType, headers, characterEncoding);
    inputStreamPublisher.subscribe(decodingProcessor);
    decodingProcessor.subscribe(tokenizer);
    tokenizer.subscribe(arrayProcessor);
    arrayProcessor.subscribe(readerProcessor);
    return readerProcessor;
  }
}
