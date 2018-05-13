package de.openknowledge.reactive.commons;

import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;

public abstract class AbstractSimpleProcessor<T, R> extends AbstractSubscriber<T> implements Processor<T, R> {

  private final SimpleSubscription<R> subscription = new SimpleSubscription<>(this::hasSubscription, this::request, this::cancel);

  @Override
  public void subscribe(Subscriber<? super R> s) {
    subscription.register(s);
  }

  @Override
  public void onComplete() {
    subscription.complete();
  }

  @Override
  public void onError(Throwable error) {
    super.onError(error);
    subscription.publish(error);
  }

  protected void publish(R item) {
    subscription.publish(item);
  }

  protected boolean isRequested() {
    return subscription.isRequested();
  }
}
