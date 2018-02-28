package de.openknowledge.reactive.charset;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.openknowledge.reactive.EmptySubscription;
import de.openknowledge.reactive.TestSubscriber;

public class EncodingProcessorTest {

  private static final String AEGAEUM = "\u00c4g\u00e4is";

  @ParameterizedTest
  @ValueSource(strings = {"UTF-8", "UTF-16", "UTF-16be", "UTF-16le", "ISO-8859-1", "IBM437"})
  public void encode(@ConvertCharset Charset charset) {
    EncodingProcessor processor = new EncodingProcessor(charset, 4);
    processor.onSubscribe(new EmptySubscription() {});
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    TestSubscriber<ByteBuffer> subscriber = item -> { while (item.hasRemaining()) result.write(item.get()); };
    processor.subscribe(subscriber);
    
    CharBuffer charBuffer = CharBuffer.wrap(AEGAEUM.toCharArray());
    for (int i = 0; i <= charBuffer.capacity(); i++) {
      charBuffer.limit(i);
      processor.onNext(charBuffer);
    }
    assertThat(result.toByteArray()).isEqualTo(AEGAEUM.getBytes(charset));
  }
}
