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

public class SimpleSubscription implements Flow.Subscription {

  private Flow.Subscription parentSubscription;
  private boolean canceled = false;

  public SimpleSubscription() {
    this(null);
  }

  public SimpleSubscription(Flow.Subscription parentSubscription) {
    this.parentSubscription = parentSubscription;
  }

  @Override
  public void request(long n) {
    if (this.parentSubscription != null && !canceled) {
      parentSubscription.request(n);
    }
  }

  @Override
  public void cancel() {
    canceled = true;

    if (this.parentSubscription != null) {
      this.parentSubscription.cancel();
    }
  }
}
