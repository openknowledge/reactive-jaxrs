package de.openknowledge.reactive.commons;

import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;

public abstract class AbstractSimpleProcessor<T, R> extends AbstractSubscriber<T> implements Processor<T, R> {

  private final SimpleSubscription<R> subscription = new SimpleSubscription<R>(this::hasSubscription, this::request, this::cancel);

  @Override
  public void subscribe(Subscriber<? super R> s) {
    subscription.register(s);
  }

  @Override
  public void onComplete() {
    subscription.complete();
  }

  @Override
  public void onError(Throwable e) {
    super.onError(e);
    subscription.publish(e);
  }

  protected void publish(R item) {
    subscription.publish(item);
  }

  protected boolean isRequested() {
    return subscription.isRequested();
  }
}
