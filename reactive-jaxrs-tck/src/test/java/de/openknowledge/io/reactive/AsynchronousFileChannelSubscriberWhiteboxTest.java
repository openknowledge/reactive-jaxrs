package de.openknowledge.io.reactive;

import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.apache.commons.io.FileUtils;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowSubscriberWhiteboxVerification;
import org.testng.annotations.Test;

@Test
public class AsynchronousFileChannelSubscriberWhiteboxTest extends FlowSubscriberWhiteboxVerification<ByteBuffer> {

  private static final File FILE = new File("target/output.txt");

  protected AsynchronousFileChannelSubscriberWhiteboxTest() {
    super(new TestEnvironment());
  }

  @Override
  public ByteBuffer createElement(int element) {
    return ByteBuffer.wrap(Integer.toString(element).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Subscriber<ByteBuffer> createFlowSubscriber(WhiteboxSubscriberProbe<ByteBuffer> probe) {
    AsynchronousFileChannel channel = null;
    try {
      FileUtils.touch(FILE);
      channel = AsynchronousFileChannel.open(FILE.toPath(), StandardOpenOption.WRITE);
    } catch (IOException e) {
      fail(e.getMessage(), e);
    }
    return new AsynchronousFileChannelSubscriber(channel) {
      @Override
      public void onSubscribe(final Subscription s) {
        super.onSubscribe(s);

        // register a successful Subscription, and create a Puppet,
        // for the WhiteboxVerification to be able to drive its tests:
        probe.registerOnSubscribe(new SubscriberPuppet() {

          @Override
          public void triggerRequest(long elements) {
            s.request(elements);
          }

          @Override
          public void signalCancel() {
            s.cancel();
          }
        });
      }

      @Override
      public void onNext(ByteBuffer element) {
        // in addition to normal Subscriber work that you're testing, register onNext with the probe
        super.onNext(element);
        probe.registerOnNext(element);
      }

      @Override
      public void onError(Throwable cause) {
        // in addition to normal Subscriber work that you're testing, register onError with the probe
        super.onError(cause);
        probe.registerOnError(cause);
      }

      @Override
      public void onComplete() {
        // in addition to normal Subscriber work that you're testing, register onComplete with the probe
        super.onComplete();
        probe.registerOnComplete();
      }
    };
  }
}
