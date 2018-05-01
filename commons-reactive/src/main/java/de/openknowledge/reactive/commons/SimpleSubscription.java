package de.openknowledge.reactive.commons;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongBinaryOperator;
import java.util.function.Supplier;

public class SimpleSubscription<R> implements Subscription {

  private Supplier<Boolean> activeCheck;
  private Consumer<Long> requestConsumer;
  private Runnable cancelProcessor;

  private Subscriber<? super R> subscriber;
  private AtomicLong requested = new AtomicLong();
  private Throwable error;

  public SimpleSubscription(Supplier<Boolean> activeCheck, Consumer<Long> requestConsumer, Runnable cancelProcessor) {
    this.activeCheck = notNull(activeCheck, "activeCheck");
    this.requestConsumer = notNull(requestConsumer, "requestConsumer");
    this.cancelProcessor = notNull(cancelProcessor, "cancelProcessor");
  }

  public void register(Subscriber<? super R> s) {
    subscriber = s;
    s.onSubscribe(this);
    if (error != null) {
      s.onError(error);
      subscriber = null;
    }
  }

  public boolean isRequested() {
    return requested.get() > 0;
  }

  @Override
  public void request(long request) {
    if (!isActive()) {
      return;
    }
    if (request <= 0) {
      Subscriber<? super R> s = subscriber;
      if (s != null) {
        s.onError(new IllegalArgumentException("request must be greater than 0, but was " + request));
        subscriber = null;
      }
      return;
    }
    requested.accumulateAndGet(request, add());
    requestConsumer.accept(request);
  }

  public void publish(R item) {
    long newRequested = requested.accumulateAndGet(1, subtract());
    if (newRequested < 0) {
      throw new IllegalStateException("publish called, but no input was requested");
    }
    Subscriber<? super R> s = subscriber;
    if (s != null) {
      s.onNext(item);
    }
  }

  public void publish(Throwable e) {
    error = e;
    Subscriber<? super R> s = subscriber;
    if (s != null) {
      s.onError(e);
      subscriber = null;
    }
  }

  public void complete() {
    Subscriber<? super R> s = subscriber;
    if (s != null) {
      s.onComplete();
    }
  }

  @Override
  public void cancel() {
    if (!isActive()) {
      return;
    }
    cancelProcessor.run();
    subscriber = null;
  }

  private boolean isActive() {
    return activeCheck.get();
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

  private static <T> T notNull(T value, String fieldName) {
    if (value == null) {
      throw new NullPointerException(fieldName + " may not be null");
    }
    return value;
  }
}
