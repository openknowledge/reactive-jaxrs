package de.openknowledge.jaxrs.reactive;

import java.util.concurrent.Flow;

/**
 * @author Christian Schulz - open knowledge GmbH
 */
public abstract class AbstractSimpleProcessor<T, R> implements Flow.Processor<T, R> {

  private Flow.Subscription parentSubscription;
  private Flow.Subscription childSubscription;
  private Flow.Subscriber<? super R> childSubscriber;

  @Override
  public void subscribe(Flow.Subscriber<? super R> subscriber) {
    childSubscriber = subscriber;
    childSubscription = new SimpleSubscription(parentSubscription) {
      @Override
      public void cancel() {
        super.cancel();

        childSubscriber = null;
      }
    };
    subscriber.onSubscribe(childSubscription);
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    parentSubscription = subscription;
  }

  @Override
  public void onNext(T item) {
    if (childSubscriber != null) {
      childSubscriber.onNext(this.process(item));
    }
  }

  @Override
  public void onError(Throwable throwable) {
    if (childSubscriber != null) {
      childSubscriber.onError(throwable);
    }
  }

  @Override
  public void onComplete() {
    if (childSubscriber != null) {
      childSubscriber.onComplete();
    }
  }

  protected abstract R process(T item);
}
