package de.openknowledge.reactive.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import de.openknowledge.reactive.commons.AbstractSimpleProcessor;

public class DecodingProcessor extends AbstractSimpleProcessor<ByteBuffer, CharBuffer> {

  private CharsetDecoder decoder;
  private ByteBuffer byteBuffer;
  private CharBuffer charBuffer;
  private boolean completed;

  public DecodingProcessor(Charset charset, int bufferSize) {
    decoder = charset.newDecoder();
    charBuffer = CharBuffer.allocate(bufferSize);
    charBuffer.mark();
  }

  @Override
  public void onNext(ByteBuffer buffer) {
    if (buffer == null) {
      throw new NullPointerException("buffer may not be null");
    }
    byteBuffer = buffer;
    charBuffer.reset();
    decoder.decode(buffer, charBuffer, false);
    if (charBuffer.position() == 0) {
      // TODO check if position 0 is equivalent to null
      super.request(1);
    } else {
      charBuffer.flip();
      charBuffer.mark();
      publish(charBuffer);
    }
  }

  @Override
  public void onComplete() {
    if (byteBuffer != null) {
      decoder.decode(byteBuffer, charBuffer, true);
      decoder.flush(charBuffer);
    }
    if (!charBuffer.hasRemaining()) {
      super.onComplete();
    } else if (isRequested()) {
      publish(charBuffer);
      super.onComplete();
    } else {
      completed = true;
    }
  }

  @Override
  protected void request(long n) {
    if (completed && n > 0) {
      publish(charBuffer);
      super.onComplete();
    } else {
      super.request(n);
    }
  }
}
