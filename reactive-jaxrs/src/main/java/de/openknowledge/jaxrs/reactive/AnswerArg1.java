package de.openknowledge.jaxrs.reactive;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class AnswerArg1<TArg> implements Answer<Object> {

  private TArg arg;

  @Override public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
    arg = invocationOnMock.getArgument(0);
    return null;
  }

  /**
   * Gets first argument.
   * @return
   */
  public TArg getArg() {
    return arg;
  }
}
