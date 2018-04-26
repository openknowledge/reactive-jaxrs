package de.openknowledge.reactive.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import de.openknowledge.reactive.commons.AbstractSimpleProcessor;

public class EncodingProcessor extends AbstractSimpleProcessor<CharBuffer, ByteBuffer> {

  private CharsetEncoder encoder;
  private CharBuffer charBuffer;
  private ByteBuffer byteBuffer;

  public EncodingProcessor(Charset charset, int bufferSize) {
    encoder = charset.newEncoder();
    byteBuffer = ByteBuffer.allocate(bufferSize);
    byteBuffer.mark();
  }

  @Override
  public void onNext(CharBuffer buffer) {
    charBuffer = buffer;
    byteBuffer.position(0);
    byteBuffer.limit(byteBuffer.capacity());
    encoder.encode(charBuffer, byteBuffer, false);
    if (byteBuffer.position() == 0) {
      // TODO check if position 0 is equivalent to null
      super.request(1);
    } else {
      byteBuffer.flip();
      byteBuffer.mark();
      publish(byteBuffer);
    }
  }

  @Override
  public void onComplete() {
    byteBuffer.reset();
    // TODO error handling
    if (charBuffer != null) {
      encoder.encode(charBuffer, byteBuffer, true);
      encoder.flush(byteBuffer);
    }
    if (byteBuffer.position() != 0) {
      byteBuffer.flip();
      byteBuffer.mark();
      publish(byteBuffer);
    }
    super.onComplete();
  }
}
