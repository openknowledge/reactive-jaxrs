/*
 *
 * JsonConverter
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

import de.undercouch.actson.DefaultJsonFeeder;
import de.undercouch.actson.JsonEvent;
import de.undercouch.actson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Flow;

/**
 * @author Robert Zilke - open knowledge GmbH
 */
public class JsonConverter implements Flow.Processor<byte[], String> {

  private Flow.Subscriber<? super String> subscriber;

  @Override
  public void subscribe(Flow.Subscriber<? super String> subscriber) {

    this.subscriber = subscriber;
  }

  // Subscriber
  @Override
  public void onSubscribe(Flow.Subscription subscription) {

    // later for back pressing
  }

  @Override
  public void onNext(byte[] item) {

    handleNextBytes(item);
  }

  private void handleNextBytes(byte[] jsonBytes) {

    JsonParser parser = new JsonParser(new DefaultJsonFeeder(StandardCharsets.UTF_8));

    int pos = 0; // position in the input JSON text
    int event; // event returned by the parser

    int startOfObjectIndex = -1;
    int endOfObjectIndex;

    do {
      // feed the parser until it returns a new event
      while ((event = parser.nextEvent()) == JsonEvent.NEED_MORE_INPUT) {
        // provide the parser with more input

        pos += parser.getFeeder().feed(jsonBytes, pos, jsonBytes.length - pos);

        // indicate end of input to the parser
        if (pos == jsonBytes.length) {
          parser.getFeeder().done();
        }
      }

      if (event == JsonEvent.START_OBJECT) {
        startOfObjectIndex = parser.getParsedCharacterCount();
      }

      if (event == JsonEvent.END_OBJECT) {
        endOfObjectIndex = parser.getParsedCharacterCount();

        byte[] parsedObjectBytes = Arrays.copyOfRange(jsonBytes, startOfObjectIndex - 1, endOfObjectIndex);
        subscriber.onNext(new String(parsedObjectBytes));
        // System.out.println("Object: " + new String(parsedObjectBytes));
      }

      // handle event
      // System.out.println("JSON event: " + event);
      if (event == JsonEvent.ERROR) {
        subscriber.onError(new IllegalStateException("Syntax error in JSON text"));
      }

      if (event == JsonEvent.END_ARRAY) {
        subscriber.onComplete();
      }
    } while (event != JsonEvent.EOF);
  }

  @Override
  public void onError(Throwable throwable) {

  }

  @Override
  public void onComplete() {

    // todo: be sure to consume last bytes
    subscriber.onComplete();
  }
}
