package de.openknowledge.io.reactive;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

public class AsynchronousFileChannelPublisherTest {

  @Test
  public void read() throws IOException, InterruptedException, ExecutionException, TimeoutException {
    AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("src", "test", "resources", "AsynchronousFileChannelPublisherTest.txt"), StandardOpenOption.READ);
    AsynchronousFileChannelPublisher publisher = new AsynchronousFileChannelPublisher(channel, 1);
    ByteBufferToStringSubscriber subscriber = new ByteBufferToStringSubscriber();

    publisher.subscribe(subscriber);
    
    assertThat(subscriber.get(10, SECONDS)).isEqualTo("Test");
  }
 
  private static class ByteBufferToStringSubscriber extends CompletableFuture<String> implements Subscriber<ByteBuffer> {

    private Subscription subscription;
    private StringBuilder builder = new StringBuilder();

    @Override
    public void onComplete() {
      complete(builder.toString());
    }

    @Override
    public void onError(Throwable error) {
      fail(error);
    }

    @Override
    public void onNext(ByteBuffer buffer) {
      while (buffer.hasRemaining()) {
        builder.append((char)buffer.get());
      }
      subscription.request(1);
    }

    @Override
    public void onSubscribe(Subscription s) {
      subscription = s;
      subscription.request(1);
    }
  }
}
