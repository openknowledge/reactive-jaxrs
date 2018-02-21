package de.openknowledge.jaxrs.reactive.test;

import java.util.concurrent.Flow.Subscription;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class PushSubscription implements Answer<Void> {

  public static PushSubscription withSubscription() {
    return new PushSubscription();
  }

  @Override
  public Void answer(InvocationOnMock invocation) throws Throwable {
    Subscription subscription = invocation.getArgument(0);
    subscription.request(Long.MAX_VALUE);
    return null;
  }
}
