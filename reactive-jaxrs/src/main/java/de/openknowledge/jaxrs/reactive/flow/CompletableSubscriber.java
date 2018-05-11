/*
 * Copyright (C) open knowledge GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package de.openknowledge.jaxrs.reactive.flow;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface CompletableSubscriber<T> extends Subscriber<T> {

  public static <T> CompletableSubscriber<T> pushEach(Consumer<T> consumer) {
    return new AbstractCompletableSubscriber<T>() {

      @Override
      public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(T item) {
        consumer.accept(item);
      }
    };
  }

  public static <T> CompletableSubscriber<T> pullEach(Consumer<T> consumer) {
    return new AbstractCompletableSubscriber<T>() {

      @Override
      public void onNext(T item) {
        consumer.accept(item);
      }
    };
  }

  public static <T> CompletableSubscriber<T> pullEach(BiConsumer<T, Subscription> consumer) {
    return new AbstractCompletableSubscriber<T>() {

      @Override
      public void onSubscribe(Subscription subscription) {
        super.onSubscribe(subscription);
        subscription.request(1);
      }

      @Override
      public void onNext(T item) {
        consumer.accept(item, subscription);
      }
    };
  }

  default CompletableSubscriber<T> andThen(Runnable runnable) {
    CompletableSubscriber<T> thisSubscriber = this;
    return new AbstractCompletableSubscriber<T>() {

      @Override
      public void onSubscribe(Subscription subscription) {
        thisSubscriber.onSubscribe(subscription);
      }

      @Override
      public void onNext(T item) {
        thisSubscriber.onNext(item);
      }

      @Override
      public void onComplete() {
        runnable.run();
      }

      @Override
      public void onError(Throwable error) {
        thisSubscriber.onError(error);
      }
    };
  }

  default CompletableSubscriber<T> exceptionally(Consumer<Throwable> consumer) {
    CompletableSubscriber<T> thisSubscriber = this;
    return new AbstractCompletableSubscriber<T>() {

      @Override
      public void onSubscribe(Subscription subscription) {
        thisSubscriber.onSubscribe(subscription);
      }

      @Override
      public void onNext(T item) {
        thisSubscriber.onNext(item);
      }

      @Override
      public void onComplete() {
        thisSubscriber.onComplete();
      }

      @Override
      public void onError(Throwable error) {
        consumer.accept(error);
      }
    };
  }

  abstract static class AbstractCompletableSubscriber<T> implements CompletableSubscriber<T> {

    Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
      this.subscription = subscription;
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable error) {
    }
  }
}
