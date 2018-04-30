package de.openknowledge.reactive.json;

import java.nio.CharBuffer;
import java.util.concurrent.Flow;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;
import de.openknowledge.MockPublisher;

/**
 * @author Christian Schulz - open knowledge GmbH
 */
public class JsonTokenizerProcessorPublisherTest extends FlowPublisherVerification<JsonToken> {

  public JsonTokenizerProcessorPublisherTest() {
    super(new TestEnvironment());
  }

  @Override
  public Flow.Publisher<JsonToken> createFlowPublisher(long elements) {
    String text = "[{},{},{}]";
    MockPublisher<CharBuffer> source = new MockPublisher<>(elements) {
      @Override
      protected CharBuffer publish(long index) {
        return CharBuffer.wrap(new char[]{text.charAt((int)(index % text.length()))});
      }
    };
    JsonTokenizer tokenizer = new JsonTokenizer();
    source.subscribe(tokenizer);
    return tokenizer;
  }

  @Override
  public Flow.Publisher<JsonToken> createFailedFlowPublisher() {
    JsonTokenizer tokenizer = new JsonTokenizer();
    tokenizer.onError(new RuntimeException());
    return tokenizer;
  }
}
