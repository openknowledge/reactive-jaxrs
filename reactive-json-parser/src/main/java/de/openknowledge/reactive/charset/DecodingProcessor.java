package de.openknowledge.reactive.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class DecodingProcessor implements Processor<ByteBuffer, CharBuffer> {

  private Subscription subscription;
  private Subscriber<? super CharBuffer> subscriber;
  private CharsetDecoder decoder;
  private CharBuffer charBuffer;

  public DecodingProcessor(Charset charset, int bufferSize) {
    decoder = charset.newDecoder();
    charBuffer = CharBuffer.allocate(bufferSize);
    charBuffer.mark();
  }

  @Override
  public void onSubscribe(Subscription s) {
    subscription = s;
  }

  @Override
  public void onNext(ByteBuffer byteBuffer) {
    charBuffer.reset();
    decoder.decode(byteBuffer, charBuffer, false);
    if (charBuffer.position() == 0) {
      subscription.request(1);
    } else {
      charBuffer.flip();
      charBuffer.mark();
      subscriber.onNext(charBuffer);
    }
  }

  @Override
  public void onComplete() {
    // TODO wait for request
    charBuffer.reset();
    decoder.flush(charBuffer);
    if (charBuffer.position() != 0) {
      charBuffer.flip();
      charBuffer.mark();
      subscriber.onNext(charBuffer);
    }
    subscriber.onComplete();
  }

  @Override
  public void onError(Throwable error) {
    subscriber.onError(error);
  }

  @Override
  public void subscribe(Subscriber<? super CharBuffer> s) {
    subscriber = s;
    subscriber.onSubscribe(new Subscription() {
      
      @Override
      public void request(long request) {
        subscription.request(request);
      }
      
      @Override
      public void cancel() {
        subscription.cancel();
      }
    });
  }
}
