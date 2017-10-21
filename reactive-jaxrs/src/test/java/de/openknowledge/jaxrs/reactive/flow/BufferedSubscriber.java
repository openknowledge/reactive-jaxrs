package de.openknowledge.jaxrs.reactive.flow;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

/**
 * Adapter delegating between Flow.Subscriber and Consumer.
 * @param <T> Delegated type.
 */
public class BufferedSubscriber<T> implements Flow.Subscriber<T> {

  private LinkedList<T> list;

  /**
   * Constructor.
   */
  public BufferedSubscriber() {
    this.list = new LinkedList<>();
  }

  @Override public void onSubscribe(Flow.Subscription subscription) {

  }

  @Override public void onError(Throwable throwable) {

  }

  @Override public void onComplete() {

  }

  @Override public void onNext(T item) {
    this.list.push(item);
  }

  /**
   * Copies current received items to list,
   * @return Copied list.
   */
  public List<T> toList() {
    return this.list.stream().collect(Collectors.toList());
  }
}
