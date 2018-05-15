package de.openknowledge.reactive.commons;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  public void onError(Throwable error) {
    if (error == null) {
      throw new NullPointerException("error may not be null");
    }
    getLogger().log(Level.SEVERE, "Encountered exception", error);
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

  protected Logger getLogger() {
    return Logger.getLogger(getClass().getCanonicalName());
  }
}
