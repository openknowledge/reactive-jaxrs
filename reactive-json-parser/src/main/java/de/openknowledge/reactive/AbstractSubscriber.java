package de.openknowledge.reactive;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public abstract class AbstractSubscriber<T> implements Subscriber<T> {

  private Subscription subscription;

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
  public void onError(Throwable e) {
    if (e == null) {
      throw new NullPointerException("error may not be null");
    }
  }

  protected boolean hasSubscription() {
    return subscription != null;
  }

  protected void request(long n) {
    Subscription s = subscription;
    if (s != null) { // just ignore otherwise
      s.request(n);
    }
  }

  protected void cancel() {
    Subscription s = subscription;
    if (s != null) {
      s.cancel();
    } 
    subscription = null;
  }
}
