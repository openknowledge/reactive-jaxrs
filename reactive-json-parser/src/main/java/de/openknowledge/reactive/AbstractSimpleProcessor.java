package de.openknowledge.reactive;

import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public abstract class AbstractSimpleProcessor<T, R> implements Processor<T, R> {

  private Subscription subscription;
  private Subscriber<? super R> subscriber;

  @Override
  public void onSubscribe(Subscription s) {
    subscription = s;
  }

  @Override
  public void subscribe(Subscriber<? super R> s) {
    if (subscription == null) {
      throw new IllegalStateException("You may only subscribe to this processor after this processor is subscribed to another publisher");
    }
    subscriber = s;
    subscriber.onSubscribe(new Subscription() {
      @Override
      public void request(long n) {
        AbstractSimpleProcessor.this.request(n);
      }

      @Override
      public void cancel() {
        subscription.cancel();
      }
    });
  }

  @Override
  public void onComplete() {
    subscriber.onComplete();
  }

  @Override
  public void onError(Throwable error) {
    subscriber.onError(error);
  }

  protected void publish(R item) {
    subscriber.onNext(item);
  }

  protected void request(long n) {
    subscription.request(n);
  }
}
