package de.openknowledge.jaxrs.reactive;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Flow;
import de.openknowledge.reactive.commons.AbstractSubscriber;

/**
 * @author Christian Schulz - open knowledge GmbH
 */
public class AsyncContextMessageBodyWriterSubscriber extends AbstractSubscriber<Object> {

  private boolean first = true;
  private AsyncContext asyncContext;
  private final MessageBodyWriter entityWriter;
  private final Class<?> targetClass;
  private final Type targetType;
  private final Annotation[] annotations;
  private final MediaType mediaType;
  private final MultivaluedMap<String, Object> httpHeaders;

  public AsyncContextMessageBodyWriterSubscriber(AsyncContext asyncContext, MessageBodyWriter entityWriter, Class<?> targetClass,
                                                 Type targetType, Annotation[] annotations, MediaType mediaType,
                                                 MultivaluedMap<String, Object> httpHeaders) {
    this.asyncContext = asyncContext;
    this.entityWriter = entityWriter;
    this.targetClass = targetClass;
    this.targetType = targetType;
    this.annotations = annotations;
    this.mediaType = mediaType;
    this.httpHeaders = httpHeaders;
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    super.onSubscribe(subscription);
    try {
      asyncContext.getResponse().getOutputStream().setWriteListener(new WriteListener() {

        @Override
        public void onWritePossible() throws IOException {
          request(Long.MAX_VALUE);
        }

        @Override
        public void onError(Throwable error) {
          AsyncContextMessageBodyWriterSubscriber.this.onError(error);
        }
      });
    } catch (IOException e) {
      onError(e);
    }
  }

  @Override
  public void onNext(Object item) {
    try {
      ServletOutputStream outputStream = asyncContext.getResponse().getOutputStream();
      if (first) {
        outputStream.print('[');
        first = false;
      } else {
        outputStream.print(',');
      }
      entityWriter.writeTo(item, targetClass, targetType, annotations, mediaType, httpHeaders, new NonClosableOutputStream(outputStream));
      outputStream.flush();

    } catch (IOException e) {
      onError(e);
    }
  }

  @Override
  public void onComplete() {
    try {
      ServletOutputStream outputStream = asyncContext.getResponse().getOutputStream();
      if (first) {
        outputStream.print('[');
      }
      outputStream.println(']');
      outputStream.flush();
      asyncContext.complete();
    } catch (IOException e) {
      // we are finished, ignoring errors
    }
  }
}