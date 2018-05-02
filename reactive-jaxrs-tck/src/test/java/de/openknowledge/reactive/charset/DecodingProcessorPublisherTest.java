package de.openknowledge.reactive.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Publisher;

import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;
import de.openknowledge.MockPublisher;

public class DecodingProcessorPublisherTest extends FlowPublisherVerification<CharBuffer> {

  public DecodingProcessorPublisherTest() {
    super(new TestEnvironment(200));
  }

  public boolean skipStochasticTests() {
	return true;
  }

  @Override
  public Publisher<CharBuffer> createFlowPublisher(long elements) {
    MockPublisher<ByteBuffer> mockPublisher = new MockPublisher<>(elements, false) {
      @Override
      protected ByteBuffer publish(long index) {
        return ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8));
      }
    };
    DecodingProcessor decodingProcessor = new DecodingProcessor(StandardCharsets.UTF_8, 2);
    mockPublisher.subscribe(decodingProcessor);
    return decodingProcessor;
  }

  @Override
  public Publisher<CharBuffer> createFailedFlowPublisher() {
    Processor<ByteBuffer, CharBuffer> publisher = new DecodingProcessor(StandardCharsets.UTF_8, 2);
    publisher.onError(new RuntimeException());
    return publisher;
  }
}
