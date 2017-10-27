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

import java.util.concurrent.Flow;

public abstract class AbstractSimpleProcessor<T, R> implements Flow.Processor<T, R> {

  private boolean error = false;
  private boolean canceled = false;

  private Flow.Subscription parentSubscription;
  private Flow.Subscription childSubscription;
  private Flow.Subscriber<? super R> childSubscriber;

  @Override
  public void subscribe(Flow.Subscriber<? super R> subscriber) {
    childSubscriber = subscriber;
    childSubscription = new SimpleSubscription(parentSubscription) {
      @Override
      public void cancel() {
        super.cancel();

        canceled = true;
        cleanup();
      }
    };
    subscriber.onSubscribe(childSubscription);
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    parentSubscription = subscription;
  }

  @Override
  public void onNext(T item) {
    if (childSubscriber != null) {
      childSubscriber.onNext(this.process(item));
    }
  }

  @Override
  public void onError(Throwable throwable) {
    if (childSubscriber != null) {
      childSubscriber.onError(throwable);

      error = true;
      cleanup();
    }
  }

  @Override
  public void onComplete() {
    if (childSubscriber != null) {
      childSubscriber.onComplete();
    }

    cleanup();
  }

  private void cleanup() {
    childSubscriber = null;
    childSubscription = null;
  }

  protected abstract R process(T item);

  public boolean isErred() {
    return error;
  }

  public boolean isCanceled() {
    return canceled;
  }
}
