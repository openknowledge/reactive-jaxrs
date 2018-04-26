package de.openknowledge.reactive.json;

import java.nio.CharBuffer;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Flow.Subscriber;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowSubscriberWhiteboxVerification;
import de.openknowledge.DelegatingProbeProcessor;
import de.openknowledge.reactive.charset.DecodingProcessorWhiteboxTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author Christian Schulz - open knowledge GmbH
 */
public class JsonTokenizerProcessorSubscriberWhiteboxTest extends FlowSubscriberWhiteboxVerification<CharBuffer> {

  protected JsonTokenizerProcessorSubscriberWhiteboxTest() {
    super(new TestEnvironment());
  }

  @Override
  public CharBuffer createElement(int element) {
    return CharBuffer.allocate(JsonToken.COMMA.toChar());
  }

  @Override
  protected Subscriber<CharBuffer> createFlowSubscriber(WhiteboxSubscriberProbe<CharBuffer> probe) {
    Subscriber<JsonToken> subscriber = mock(Subscriber.class);
    DecodingProcessorWhiteboxTest.VoidAnswer requestAll = (invocation) -> invocation.<Subscription>getArgument(0).request(Long.MAX_VALUE);
    doAnswer(requestAll).when(subscriber).onSubscribe(any(Subscription.class));
    return new DelegatingProbeProcessor<>(new JsonTokenizer(), subscriber, probe);
  }
}
