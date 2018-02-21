package de.openknowledge.reactive;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Semaphore;

public class QueueingProcessor<T> implements Processor<T, T> {

  private Semaphore semaphore = new Semaphore(1);
  private Subscriber<? super T> subscriber;
  private Queue<T> queue = new ArrayDeque<>();
  private int requested;
  private Throwable error;
  private boolean completed;

  @Override
  public void onNext(T item) {
    semaphore.acquireUninterruptibly();
    if (subscriber != null && requested > 0) {
      requested--;
      Subscriber<? super T> s = subscriber;
      semaphore.release();
      s.onNext(item);
    } else {
      queue.add(item);
      semaphore.release();
    }
  }

  @Override
  public void onSubscribe(Subscription subscription) {
    subscription.request(Long.MAX_VALUE);
  }

  @Override
  public void subscribe(Subscriber<? super T> s) {
    semaphore.acquireUninterruptibly();
    if (subscriber != null) {
      throw new IllegalArgumentException("Only one subscriber at the same time supported");
    }
    subscriber = s;
    semaphore.release();
    s.onSubscribe(new QueueSubscription());
  }

  @Override
  public void onComplete() {
    semaphore.acquireUninterruptibly();
    if (subscriber != null && queue.isEmpty()) {
      Subscriber<? super T> s = subscriber;
      semaphore.release();
      s.onComplete();
      subscriber = null;
    } else {
      completed = true;
      semaphore.release();
    }
  }

  @Override
  public void onError(Throwable e) {
    semaphore.acquireUninterruptibly();
    if (subscriber != null) {
      Subscriber<? super T> s = subscriber;
      semaphore.release();
      s.onError(e);
      subscriber = null;
    } else {
      error = e;
      semaphore.release();
    }
  }

  private class QueueSubscription implements Subscription {

    @Override
    public void request(long request) {
      semaphore.acquireUninterruptibly();
      while (!queue.isEmpty() && request > 0) {
        T item = queue.poll();
        request--;
        Subscriber<? super T> s = subscriber;
        semaphore.release();
        s.onNext(item);
        semaphore.acquireUninterruptibly();
      }
      requested += request;
      if (queue.isEmpty()) {
        if (completed) {
          Subscriber<? super T> s = subscriber;
          semaphore.release();
          s.onComplete();
          subscriber = null;
        } else if (error != null) {
          Subscriber<? super T> s = subscriber;
          semaphore.release();
          s.onError(error);
          subscriber = null;
        } else {
          semaphore.release();
        }
      } else {
        semaphore.release();
      }
    }

    @Override
    public void cancel() {
      subscriber = null;
    }    
  }
}
