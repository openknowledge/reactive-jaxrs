package de.openknowledge.reactive;

import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;

public abstract class AbstractSimpleProcessor<T, R> implements Processor<T, R> {

  private Subscription subscription;
  private Subscriber<? super R> subscriber;
  private AtomicLong requested = new AtomicLong();
  private Throwable error;

  @Override
  public void onSubscribe(Subscription s) {
    if (s == null) {
      throw new NullPointerException("subscription may not be null");
    } else if (subscription != null) {
      s.cancel();
    } else {
      subscription = s;
    }
  }

  @Override
  public void subscribe(Subscriber<? super R> s) {
    subscriber = s;
    s.onSubscribe(new Subscription() {

      @Override
      public void request(long request) {
        if (subscription == null) {
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
        if (subscription == null) {
          return;
        }
        subscription.cancel();
        subscription = null;
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
    if (e == null) {
      throw new NullPointerException("error may not be null");
    }
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

  protected void request(long n) {
    Subscription s = subscription;
    if (s != null) { // just ignore otherwise
      subscription.request(n);
    }
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
