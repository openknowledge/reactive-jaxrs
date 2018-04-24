package de.openknowledge.reactive.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicInteger;

import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;

public class DecodingProcessorPublisherTest extends FlowPublisherVerification<CharBuffer> {

  public DecodingProcessorPublisherTest() {
    super(new TestEnvironment());
  }

  public boolean skipStochasticTests() {
	return true;
  }

  @Override
  public Publisher<CharBuffer> createFlowPublisher(long elements) {
    DecodingProcessor decodingProcessor = new DecodingProcessor(StandardCharsets.UTF_8, 2);
    Publisher<ByteBuffer> mockPublisher = new Publisher<ByteBuffer>() {

      private AtomicInteger published = new AtomicInteger();
      private boolean finished;
      private ExecutorService pool = Executors.newFixedThreadPool(1);
      
      @Override
      public void subscribe(Subscriber<? super ByteBuffer> s) {
        s.onSubscribe(new Subscription() {
          
          @Override
          public synchronized void request(long n) {
            if (n <= 0) {
              s.onError(new IllegalArgumentException());
            }
            if (finished) {
              return;
            }
            int p;
            do {
              pool.execute(() -> s.onNext(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8))));
              p = published.incrementAndGet();
              n--;
            } while (p < elements && n > 0);
            if (p == elements) {
              finished = true;
              pool.execute(() -> s.onComplete());
            }
          }
          
          @Override
          public void cancel() {
            finished = true;
          }
        });
      }
    };
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
