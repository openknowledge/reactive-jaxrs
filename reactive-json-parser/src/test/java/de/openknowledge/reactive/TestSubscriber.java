package de.openknowledge.reactive;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public interface TestSubscriber<T> extends Subscriber<T> {

  @Override
  default void onComplete() {
  }

  @Override
  default void onError(Throwable error) {
    throw new AssertionError(error);
  }

  @Override
  default void onSubscribe(Subscription subscription) {
    subscription.request(Long.MAX_VALUE);
  }
}
