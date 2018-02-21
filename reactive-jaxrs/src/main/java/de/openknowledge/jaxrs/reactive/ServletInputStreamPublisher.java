package de.openknowledge.jaxrs.reactive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class ServletInputStreamPublisher implements Publisher<ByteBuffer> {

  private ServletInputStream stream;
  private ByteBuffer buffer;
  private Subscriber<? super ByteBuffer> subscriber;
  private AtomicLong requests = new AtomicLong(0);

  public ServletInputStreamPublisher(ServletInputStream servletInputStream, int bufferSize) {
    buffer = ByteBuffer.wrap(new byte[bufferSize]);
    stream = servletInputStream;
    stream.setReadListener(new ReadListener() {

      public void onDataAvailable() throws IOException {
        Subscriber<? super ByteBuffer> s = subscriber; // put into local variable to be thread-safe
        if (s != null) {
          read(s);
        }
      }

      public void onAllDataRead() throws IOException {
        Subscriber<? super ByteBuffer> s = subscriber; // put into local variable to be thread-safe
        if (s != null) {
          s.onComplete();
        }
      }

      public void onError(Throwable error) {
        Subscriber<? super ByteBuffer> s = subscriber; // put into local variable to be thread-safe
        if (s != null) {
          s.onError(error);
        }
      }      
    });
  }

  @Override
  public void subscribe(Subscriber<? super ByteBuffer> s) {
    subscriber = s;
    s.onSubscribe(new Subscription() {
      
      public void request(long requested) {
        if (requested < 0) {
          throw new IllegalArgumentException("parameter must not be less than zero, but was " + requested);
        }
        Subscriber<? super ByteBuffer> s = subscriber; // put into local variable to be thread-safe
        if (s == null) {
          throw new IllegalStateException("subscription already canceled");
        }
        requests.addAndGet(requested);
        read(s);
      }
      
      public void cancel() {
        subscriber = null;
      }
    });
  }

  private synchronized void read(Subscriber<? super ByteBuffer> s) {
    try {
      for (long requested = requests.get(); stream.isReady() && requested > 0; requested = requests.decrementAndGet()) {
        int limit = stream.read(buffer.array());
        if (limit == -1) {
          s.onComplete();
        } else {
          buffer.position(0);
          buffer.limit(limit);
          s.onNext(buffer);
        }
      }
    } catch (IOException error) {
      s.onError(error);
    }
  }
}
