package de.openknowledge.jaxrs.reactive.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class AsynchronousFileChannelPublisher implements Publisher<ByteBuffer> {

  private AsynchronousFileChannel channel;
  private ByteBuffer buffer;
  private Subscriber<? super ByteBuffer> subscriber;
  private AtomicLong requested = new AtomicLong(0);
  private AtomicReference<Long> filePosition = new AtomicReference<>(0L);

  public AsynchronousFileChannelPublisher(AsynchronousFileChannel asynchronousChannel, int bufferSize) {
    if (asynchronousChannel == null) {
      throw new IllegalArgumentException("channel may not be null");
    }
    channel = asynchronousChannel;
    buffer = ByteBuffer.allocate(bufferSize);
  }

  @Override
  public void subscribe(Subscriber<? super ByteBuffer> s) {
    if (subscriber != null) {
      throw new IllegalStateException("This publisher does support only one subscriber");
    }
    subscriber = s;
    subscriber.onSubscribe(new Subscription() {
      
      @Override
      public void request(long count) {
        if (count < 0) {
          throw new IllegalArgumentException("count may not be negative");
        }
        requested.addAndGet(count);
        read();
      }
      
      @Override
      public void cancel() {
        try {
          channel.close();
        } catch (IOException e) {
          subscriber.onError(e);
        }
      }
    });
  }

  private void read() {
    Long oldPosition = filePosition.getAndSet(null);
    if (oldPosition != null) {
      channel.read(buffer, oldPosition, oldPosition, new CompletionHandler<Integer, Long>() {

        @Override
        public void completed(Integer readCount, Long oldPosition) {
          if (readCount == -1) {
            subscriber.onComplete();
          } else {
            subscriber.onNext(buffer);
            filePosition.set(oldPosition + readCount);
          }
          long waitingRequests = requested.decrementAndGet();
          if (waitingRequests > 0) {
            read();
          }
        }

        @Override
        public void failed(Throwable error, Long oldPosition) {
          try {
            channel.close();
          } catch (IOException e) {
            // ignore
          }
          subscriber.onError(error);
        }
      });
    }
  }
}
