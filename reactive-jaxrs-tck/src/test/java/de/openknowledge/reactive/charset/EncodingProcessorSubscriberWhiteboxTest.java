package de.openknowledge.reactive.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowSubscriberWhiteboxVerification;
import org.testng.annotations.Test;
import de.openknowledge.DelegatingProbeProcessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@Test
public class EncodingProcessorSubscriberWhiteboxTest extends FlowSubscriberWhiteboxVerification<CharBuffer> {

  protected EncodingProcessorSubscriberWhiteboxTest() {
    super(new TestEnvironment());
  }

  @Override
  public CharBuffer createElement(int element) {
    return CharBuffer.wrap(Integer.toString(element));
  }

  @Override
  public Subscriber<CharBuffer> createFlowSubscriber(WhiteboxSubscriberProbe<CharBuffer> probe) {
    Subscriber<ByteBuffer> subscriber = mock(Subscriber.class);
    DelegatingProbeProcessor.VoidAnswer requestAll = (invocation) -> invocation.<Subscription>getArgument(0).request(Long.MAX_VALUE);
    doAnswer(requestAll).when(subscriber).onSubscribe(any(Subscription.class));
    return new DelegatingProbeProcessor<>(new EncodingProcessor(StandardCharsets.UTF_8, 2), subscriber, probe);
  }
}
