package de.openknowledge.reactive.commons.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Phaser;

import de.openknowledge.reactive.commons.AbstractSubscriber;

public class AsynchronousFileChannelSubscriber extends AbstractSubscriber<ByteBuffer> {

  private AsynchronousFileChannel channel;
  private long filePosition;
  private Phaser phaser = new Phaser(1);

  public AsynchronousFileChannelSubscriber(AsynchronousFileChannel fileChannel) {
    channel = fileChannel;
  }

  @Override
  public void onSubscribe(Subscription s) {
    super.onSubscribe(s);
    request(1);
  }

  @Override
  public void onNext(ByteBuffer buffer) {
    phaser.register();

    channel.write(buffer, filePosition, filePosition, new CompletionHandler<Integer, Long>() {

      @Override
      public void completed(Integer writeCount, Long position) {
        filePosition += writeCount;
        phaser.arriveAndDeregister();
        request(1);
      }

      @Override
      public void failed(Throwable error, Long position) {
        phaser.arriveAndDeregister();
        cancel();
      }
    });
  }

  @Override
  public void onComplete() {
    shutdown();
  }

  @Override
  public void onError(Throwable error) {
    // TODO log error
    shutdown();
  }

  private void shutdown() {
    try {
      phaser.arriveAndAwaitAdvance();
      phaser.arriveAndDeregister();
      channel.close();
    } catch (IOException e) {
      // ignore
    }
  }
}
