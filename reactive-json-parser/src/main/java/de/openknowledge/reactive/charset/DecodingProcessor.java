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
    // TODO error handling
    if (byteBuffer != null) {
      decoder.decode(byteBuffer, charBuffer, true);
      decoder.flush(charBuffer);
    }
    if (!charBuffer.hasRemaining()) {
      super.onComplete();
    } else if (isRequested()) {
      publish(charBuffer);
    } else {
      throw new IllegalStateException("Should not happen");
      // TODO what shall we do? We have remaining items but no one requested them, I think it should not happen
    }
  }
}
