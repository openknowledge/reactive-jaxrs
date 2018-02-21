package de.openknowledge.jaxrs.reactive.test;

import java.util.concurrent.CountDownLatch;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class Completion implements Answer<Void> {

  private CountDownLatch latch = new CountDownLatch(1);
  @Override
  public Void answer(InvocationOnMock invocation) throws Throwable {
    latch.countDown();
    return null;
  }

  public void await() {
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}
