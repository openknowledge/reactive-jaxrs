package de.openknowledge.reactive.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class EncodingProcessor implements Processor<CharBuffer, ByteBuffer> {

  private Subscription subscription;
  private Subscriber<? super ByteBuffer> subscriber;
  private CharsetEncoder encoder;
  private ByteBuffer byteBuffer;

  public EncodingProcessor(Charset charset, int bufferSize) {
    encoder = charset.newEncoder();
    byteBuffer = ByteBuffer.allocate(bufferSize);
    byteBuffer.mark();
  }

  @Override
  public void onSubscribe(Subscription s) {
    subscription = s;
  }

  @Override
  public void onNext(CharBuffer charBuffer) {
    byteBuffer.position(0);
    byteBuffer.limit(byteBuffer.capacity());
    encoder.encode(charBuffer, byteBuffer, false);
    if (byteBuffer.position() != 0) {
      byteBuffer.flip();
      byteBuffer.mark();
      subscriber.onNext(byteBuffer);
    }
  }

  @Override
  public void onComplete() {
    byteBuffer.reset();
    encoder.flush(byteBuffer);
    if (byteBuffer.position() != 0) {
      byteBuffer.flip();
      byteBuffer.mark();
      subscriber.onNext(byteBuffer);
    }
    subscriber.onComplete();
  }

  @Override
  public void onError(Throwable error) {
    subscriber.onError(error);
  }

  @Override
  public void subscribe(Subscriber<? super ByteBuffer> s) {
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
