package de.openknowledge.reactive;

import java.util.concurrent.Flow.Subscription;

public interface EmptySubscription extends Subscription {
  default void request(long request) {
  }
  
  default void cancel() {
  }
}
