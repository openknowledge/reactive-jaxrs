package de.openknowledge.io.reactive;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow.Subscription;

import org.junit.jupiter.api.Test;

public class AsynchronousFileChannelSubscriberTest {

  @Test
  public void write() throws IOException, InterruptedException, ExecutionException {
    Path file = Paths.get("target", "AsynchronousFileChannelSubscriberTest.txt");
    AsynchronousFileChannel channel = AsynchronousFileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    AsynchronousFileChannelSubscriber subscriber = new AsynchronousFileChannelSubscriber(channel);

    Subscription subscription = mock(Subscription.class);
    ByteBuffer byteBuffer = ByteBuffer.wrap("Test".getBytes("UTF-8"), 0, 1);
    CountDownLatch completed = new CountDownLatch(byteBuffer.capacity());
    for (int i = 1; i <= byteBuffer.capacity(); i++) {
      int limit = i;
      doAnswer(execution -> {
        completed.countDown();
        byteBuffer.limit(limit);
        subscriber.onNext(byteBuffer);
        return null;
      }).when(subscription).request(1);
    }
    
    subscriber.onSubscribe(subscription);
    completed.await(10, SECONDS);
    subscriber.onComplete();

    assertThat(file).hasContent("Test");
  }
}
