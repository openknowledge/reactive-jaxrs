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

  private final JsonParser jsonParser;

  /**
   * Is equal to the number of removed bytes from byteBuffer
   */
  private int parsedCharactersOffset = 0;

  private Flow.Subscriber<? super String> subscriber;

  private byte[] byteBuffer;

  private int byteBufferPosition = 0;

  private Flow.Subscription subscription;

  /**
   * have to be a field for the case: first bytes contain first bytes of a json object following bytes contain the rest
   */
  private int startOfObjectIndex = 0;

  @SuppressWarnings("WeakerAccess")
  public JsonConverter() {

    jsonParser = new JsonParser(new DefaultJsonFeeder(StandardCharsets.UTF_8));
    byteBuffer = new byte[2048];
  }

  // Publisher
  @Override
  public void subscribe(Flow.Subscriber<? super String> subscriber) {

    this.subscriber = subscriber;
  }

  // Subscriber
  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    this.subscription = subscription;
    // later for back pressing
  }

  @Override
  public void onNext(byte[] item) {

    handleNextBytes(item);

    this.subscription.request(1);
  }

  @Override
  public void onError(Throwable throwable) {

  }

  @Override
  public void onComplete() {

    // todo: be sure to consume last bytes
    subscriber.onComplete();
  }

  private void handleNextBytes(byte[] jsonBytes) {

    addToByteBuffer(jsonBytes);

    int jsonBytesPosition = 0; // position in the input JSON text
    int event; // event returned by the parser

    do {

      if (jsonBytesPosition != jsonBytes.length) {
        // provide the parser with more input
        jsonBytesPosition += jsonParser.getFeeder().feed(
            jsonBytes,
            jsonBytesPosition,
            jsonBytes.length - jsonBytesPosition);
      }

      event = jsonParser.nextEvent();

      if (event == JsonEvent.START_OBJECT) {
        startOfObjectIndex = jsonParser.getParsedCharacterCount();
      }

      if (event == JsonEvent.END_OBJECT) {
        int endOfObjectIndex = jsonParser.getParsedCharacterCount();

        byte[] bufferedBytes = getBytesInBuffer();

        int start = startOfObjectIndex - parsedCharactersOffset - 1;
        int end = endOfObjectIndex - parsedCharactersOffset;

        byte[] parsedObjectBytes = Arrays.copyOfRange(bufferedBytes, start, end);
        subscriber.onNext(new String(parsedObjectBytes));
        // System.out.println("Object: " + new String(parsedObjectBytes));

        removeBytesFromBuffer(parsedObjectBytes.length);
      }

      // handle event
      // System.out.println("JSON event: " + event);
      if (event == JsonEvent.ERROR) {
        subscriber.onError(new IllegalStateException("Syntax error in JSON text"));
      }

      if (event == JsonEvent.END_ARRAY) {
        subscriber.onComplete();
      }
      // do until all jsonBytes consumed and more input needed
    } while (!(jsonBytesPosition == jsonBytes.length && event == JsonEvent.NEED_MORE_INPUT));
  }

  private byte[] getBytesInBuffer() {

    return Arrays.copyOf(byteBuffer, byteBufferPosition);
  }

  private void addToByteBuffer(byte[] bytesToAdd) {

    for (byte byteToAdd : bytesToAdd) {
      byteBuffer[byteBufferPosition++] = byteToAdd;
    }
  }

  /**
   *
   * @param n number of bytes to remove
   */
  private void removeBytesFromBuffer(int n) {

    byte[] validBytes = Arrays.copyOfRange(byteBuffer, n + 1, byteBufferPosition);
    byteBufferPosition = 0;
    addToByteBuffer(validBytes);

    parsedCharactersOffset += n + 1;
  }
}
