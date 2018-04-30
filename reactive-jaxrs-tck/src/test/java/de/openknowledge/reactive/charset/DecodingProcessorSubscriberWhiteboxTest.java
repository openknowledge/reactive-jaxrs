package de.openknowledge.reactive.charset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowSubscriberWhiteboxVerification;
import org.testng.annotations.Test;
import de.openknowledge.DelegatingProbeProcessor;

@Test
public class DecodingProcessorSubscriberWhiteboxTest extends FlowSubscriberWhiteboxVerification<ByteBuffer> {

  protected DecodingProcessorSubscriberWhiteboxTest() {
    super(new TestEnvironment());
  }

  @Override
  public ByteBuffer createElement(int element) {
    return ByteBuffer.wrap(Integer.toString(element).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Subscriber<ByteBuffer> createFlowSubscriber(WhiteboxSubscriberProbe<ByteBuffer> probe) {
    java.util.concurrent.Flow.Subscriber<CharBuffer> subscriber
      = mock(java.util.concurrent.Flow.Subscriber.class);
    DelegatingProbeProcessor.VoidAnswer requestAll = (invocation) -> invocation.<Subscription>getArgument(0).request(Long.MAX_VALUE);
    doAnswer(requestAll).when(subscriber).onSubscribe(any(Subscription.class));
    return new DelegatingProbeProcessor<>(new DecodingProcessor(StandardCharsets.UTF_8, 2), subscriber, probe);
  }
}
