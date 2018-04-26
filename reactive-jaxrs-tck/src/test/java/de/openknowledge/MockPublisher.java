package de.openknowledge;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Christian Schulz - open knowledge GmbH
 */
public abstract class MockPublisher<T> implements Flow.Publisher<T> {
  private AtomicInteger published = new AtomicInteger();
  private boolean finished;
  private ExecutorService pool = Executors.newFixedThreadPool(1);
  private final long elements;
  private final boolean sync;

  public MockPublisher(long elements) {
    this(elements, true);
  }

  public MockPublisher(long elements, boolean sync) {
    this.elements = elements;
    this.sync = sync;
  }

  @Override
  public void subscribe(Flow.Subscriber<? super T> s) {
    s.onSubscribe(new Flow.Subscription() {
      @Override
      public synchronized void request(long n) {
        if (n <= 0) {
          s.onError(new IllegalArgumentException());
        }
        if (finished) {
          return;
        }
        int p;
        long j = 0;
        do {
          if (sync) {
            s.onNext(publish(j));
          } else {
            long finalJ = j;
            pool.execute(() -> s.onNext(publish(finalJ)));
          }
          p = published.incrementAndGet();
          j++;
        } while (p < elements && j < n);
        if (p == elements) {
          finished = true;
          if (sync) {
            s.onComplete();
          } else {
            pool.execute(s::onComplete);
          }
        }
      }

      @Override
      public void cancel() {
        finished = true;
      }
    });
  }

  protected abstract T publish(long index);
}
