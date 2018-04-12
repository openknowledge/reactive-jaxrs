package de.openknowledge.io.reactive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Phaser;

public class AsynchronousFileChannelSubscriber implements Subscriber<ByteBuffer> {

  private AsynchronousFileChannel channel;
  private long filePosition;
  private Subscription subscription;
  private Phaser phaser = new Phaser(1);

  public AsynchronousFileChannelSubscriber(AsynchronousFileChannel fileChannel) {
    channel = fileChannel;
  }

  @Override
  public void onSubscribe(Subscription s) {
    subscription = s;
    subscription.request(1);
  }

  @Override
  public void onNext(ByteBuffer buffer) {
    phaser.register();
    
    channel.write(buffer, filePosition, filePosition, new CompletionHandler<Integer, Long>() {

      @Override
      public void completed(Integer writeCount, Long position) {
        filePosition += writeCount;
        phaser.arriveAndDeregister();
        subscription.request(1);
      }

      @Override
      public void failed(Throwable error, Long position) {
        phaser.arriveAndDeregister();
        subscription.cancel();
      }
    });
  }

  @Override
  public void onComplete() {
    try {
      phaser.arriveAndAwaitAdvance();
      phaser.arriveAndDeregister();
      channel.close();
    } catch (IOException e) {
      // ignore
    }
  }

  @Override
  public void onError(Throwable error) {
    // TODO log error
    onComplete();
  }
}
