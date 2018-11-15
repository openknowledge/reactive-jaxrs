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
  private NonClosableOutputStream nonClosableOutputStream;

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
      ServletOutputStream outputStream = asyncContext.getResponse().getOutputStream();
      outputStream.setWriteListener(new WriteListener() {

        @Override
        public void onWritePossible() throws IOException {
          request(1L);
        }

        @Override
        public void onError(Throwable error) {
          AsyncContextMessageBodyWriterSubscriber.this.onError(error);
        }
      });
      nonClosableOutputStream = new NonClosableOutputStream(outputStream);
      if (outputStream.isReady()) {
        request(1L);
      }
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
      entityWriter.writeTo(item, targetClass, targetType, annotations, mediaType, httpHeaders, nonClosableOutputStream);
      outputStream.flush();
      
      if (outputStream.isReady()) {
        request(1L);
      }

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
      nonClosableOutputStream.closeAsync();
    } catch (IOException e) {
      // we are finished, ignoring errors
    } finally {
      closeStream();
    }
  }

  @Override
  public void onError(Throwable error) {
    super.onError(error);

    closeStream();
  }

  private void closeStream() {
    if (nonClosableOutputStream != null) {
      try {
        nonClosableOutputStream.closeAsync();
      } catch (IOException e) {
        // ignore errors during close
      }
    }
  }
}
