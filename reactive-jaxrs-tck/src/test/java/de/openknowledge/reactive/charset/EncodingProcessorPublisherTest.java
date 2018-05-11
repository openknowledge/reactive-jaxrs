package de.openknowledge.reactive.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Publisher;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;
import de.openknowledge.MockPublisher;

public class EncodingProcessorPublisherTest extends FlowPublisherVerification<ByteBuffer> {

  public EncodingProcessorPublisherTest() {
    super(new TestEnvironment());
  }

  public boolean skipStochasticTests() {
    return true;
  }

  @Override
  public Publisher<ByteBuffer> createFlowPublisher(long elements) {
    MockPublisher<CharBuffer> mockPublisher = new MockPublisher<>(elements, false) {
      @Override
      protected CharBuffer publish(long index) {
        return CharBuffer.wrap("test");
      }
    };
    EncodingProcessor encodingProcessor = new EncodingProcessor(StandardCharsets.UTF_8, 2);
    mockPublisher.subscribe(encodingProcessor);
    return encodingProcessor;
  }

  @Override
  public Publisher<ByteBuffer> createFailedFlowPublisher() {
    Processor<CharBuffer, ByteBuffer> publisher = new EncodingProcessor(StandardCharsets.UTF_8, 2);
    publisher.onError(new RuntimeException());
    return publisher;
  }
}
