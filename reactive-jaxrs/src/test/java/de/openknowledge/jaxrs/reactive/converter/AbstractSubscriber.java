/*
 *
 * de.openknowledge.jaxrs.reactive.converter.AbstractSubscriber
 *
 *
 * This document contains trade secret data which is the property of OpenKnowledge GmbH. Information contained herein
 * may not be used, copied or disclosed in whole or part except as permitted by written agreement from open knowledge
 * GmbH.
 *
 * Copyright (C) 2017 open knowledge GmbH / Oldenburg / Germany
 *
 */
package de.openknowledge.jaxrs.reactive.converter;

import java.util.concurrent.Flow;

/**
 * @author Robert Zilke - open knowledge GmbH
 *
 * @param <T> the subscribed item type
 */
public class AbstractSubscriber<T> implements Flow.Subscriber<T> {

  @Override
  public void onSubscribe(Flow.Subscription subscription) {

    // nothing
  }

  @Override
  public void onNext(T item) {

    // nothing
  }

  @Override
  public void onError(Throwable throwable) {

    throw new UnsupportedOperationException("onError should not be invoked!");
  }

  @Override
  public void onComplete() {

    // nothing
  }
}
