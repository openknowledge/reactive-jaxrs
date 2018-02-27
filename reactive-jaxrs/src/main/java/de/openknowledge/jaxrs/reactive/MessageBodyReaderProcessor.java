package de.openknowledge.jaxrs.reactive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import de.openknowledge.reactive.AbstractSimpleProcessor;

public class MessageBodyReaderProcessor<R> extends AbstractSimpleProcessor<String, R> {

  private MessageBodyReader<R> reader;
  private Class<R> type;
  private Type genericType;
  private Annotation[] annotations;
  private MediaType mediaType;
  private MultivaluedMap<String, String> headers;
  private String encoding;

  public MessageBodyReaderProcessor(
      MessageBodyReader<R> reader,
      Class<R> type,
      Type genericType,
      Annotation[] annotations, 
      MediaType mediaType,
      MultivaluedMap<String, String> headers,
      String encoding) {
    this.reader = reader;
    this.type = type;
    this.genericType = genericType;
    this.annotations = annotations;
    this.mediaType = mediaType;
    this.headers = headers;
    this.encoding = encoding;
  }

  @Override
  public void onNext(String value) {
    try {
      publish(reader.readFrom(type, genericType, annotations, mediaType, headers, new ByteArrayInputStream(value.getBytes(encoding))));
    } catch (WebApplicationException | IOException e) {
      onError(e);
    }
  }
}
