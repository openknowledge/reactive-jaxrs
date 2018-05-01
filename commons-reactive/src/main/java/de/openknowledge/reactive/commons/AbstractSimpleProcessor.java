package de.openknowledge.reactive.commons;

import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;

public abstract class AbstractSimpleProcessor<T, R> extends AbstractSubscriber<T> implements Processor<T, R> {

  private Subscriber<? super R> subscriber;
  private AtomicLong requested = new AtomicLong();
  private Throwable error;

  @Override
  public void subscribe(Subscriber<? super R> s) {
    subscriber = s;
    s.onSubscribe(new Subscription() {

      @Override
      public void request(long request) {
        if (!hasSubscription()) {
          return;
        }
        if (request <= 0) {
          s.onError(new IllegalArgumentException("request must be greater than 0, but was " + request));
          return;
        }
        requested.accumulateAndGet(request, add());
        AbstractSimpleProcessor.this.request(request);
      }

      @Override
      public void cancel() {
        if (!hasSubscription()) {
          return;
        }
        AbstractSimpleProcessor.super.cancel();
        subscriber = null;
      }
    });
    if (error != null) {
      s.onError(error);
    }
  }

  @Override
  public void onComplete() {
    Subscriber<? super R> s = subscriber;
    if (s != null) {
      s.onComplete();
    }
  }

  @Override
  public void onError(Throwable e) {
    super.onError(e);
    Subscriber<? super R> s = subscriber;
    if (s != null) {
      s.onError(e);
    } else {
      error = e;
    }
  }

  protected void publish(R item) {
    long newRequested = requested.accumulateAndGet(1, subtract());
    if (newRequested < 0) {
      throw new IllegalStateException("publish called, but no input was requested");
    }
    Subscriber<? super R> s = subscriber;
    if (s != null) {
      s.onNext(item);
    }
  }

  protected boolean isRequested() {
    return requested.get() > 0;
  }

  private LongBinaryOperator add() {
    return (oldRequested, addRequested) -> {
      long newRequested = oldRequested + addRequested;
      if (newRequested < 0) { // overflow
        newRequested = Long.MAX_VALUE;
      }
      return newRequested;
    };
  }

  private LongBinaryOperator subtract() {
    return (oldRequested, subtractRequested) -> {
      if (oldRequested == Long.MAX_VALUE) {
        return oldRequested; // special handling for "request all"
      }
      return oldRequested - subtractRequested;
    };
  }
}
