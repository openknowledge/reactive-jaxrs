package de.openknowledge.reactive.json;

import java.nio.CharBuffer;
import java.util.concurrent.Flow;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowSubscriberBlackboxVerification;

/**
 * @author Christian Schulz - open knowledge GmbH
 */
public class JsonTokenizerProcessorSubscriberBlackboxTest extends FlowSubscriberBlackboxVerification<CharBuffer> {
  public JsonTokenizerProcessorSubscriberBlackboxTest() {
    super(new TestEnvironment());
  }

  @Override
  public CharBuffer createElement(int element) {
    return CharBuffer.allocate(JsonToken.COMMA.toChar());
  }

  @Override
  public Flow.Subscriber<CharBuffer> createFlowSubscriber() {
    return new JsonTokenizer();
  }
}
