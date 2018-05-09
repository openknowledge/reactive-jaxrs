package de.openknowledge.reactive.commons.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.atomic.AtomicReference;

import de.openknowledge.reactive.commons.SimpleSubscription;

public class AsynchronousFileChannelPublisher implements Publisher<ByteBuffer> {

  private final SimpleSubscription<ByteBuffer> subscription = new SimpleSubscription<>(this::request, this::cancel); 
  private AsynchronousFileChannel channel;
  private ByteBuffer buffer;
  private AtomicReference<Long> filePosition = new AtomicReference<>(0L);

  public AsynchronousFileChannelPublisher(AsynchronousFileChannel asynchronousChannel, int bufferSize) {
    if (asynchronousChannel == null) {
      throw new IllegalArgumentException("channel may not be null");
    }
    channel = asynchronousChannel;
    buffer = ByteBuffer.allocate(bufferSize);
    buffer.mark();
  }

  @Override
  public void subscribe(Subscriber<? super ByteBuffer> s) {
    subscription.register(s);
  }

  private void cancel() {
    try {
      channel.close();
    } catch (IOException e) {
      // ignore, since subscription is canceled
    }
  }

  private void request(long elements) {
    read();
  }

  private void read() {
    Long oldPosition = filePosition.getAndSet(null);
    if (oldPosition != null) {
      buffer.reset();
      channel.read(buffer, oldPosition, oldPosition, new CompletionHandler<Integer, Long>() {

        @Override
        public void completed(Integer readCount, Long oldPosition) {
          if (readCount <= 0) {
            subscription.complete();
            return;
          } else {
            buffer.flip();
            buffer.mark();
            subscription.publish(buffer);
            filePosition.set(oldPosition + readCount);
          }
          if (subscription.isRequested()) {
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
          subscription.publish(error);
        }
      });
    }
  }
}
