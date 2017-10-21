package de.openknowledge.jaxrs.reactive;

import java.util.concurrent.Flow;

/**
 * @author Christian Schulz - open knowledge GmbH
 */
public class SimpleSubscription implements Flow.Subscription {

  private Flow.Subscription parentSubscription;

  public SimpleSubscription() {
    this(null);
  }

  public SimpleSubscription(Flow.Subscription parentSubscription) {
    this.parentSubscription = parentSubscription;
  }

  @Override
  public void request(long n) {
    // TODO
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public void cancel() {
    if (this.parentSubscription != null) {
      this.parentSubscription.cancel();
    }
  }
}
