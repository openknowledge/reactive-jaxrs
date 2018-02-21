package de.openknowledge.reactive.charset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.openknowledge.reactive.EmptySubscription;
import de.openknowledge.reactive.TestSubscriber;
import de.openknowledge.reactive.charset.DecodingProcessor;

public class DecodingProcessorTest {

  private static final String AEGAEUM = "\u00c4g\u00e4is";

  @ParameterizedTest
  @ValueSource(strings = {"UTF-8", "UTF-16", "UTF-16be", "UTF-16le", "ISO-8859-1", "IBM437"})
  public void decode(@ConvertCharset Charset charset) {
    DecodingProcessor processor = new DecodingProcessor(charset, 2);
    processor.onSubscribe(new EmptySubscription() {});
    StringBuilder result = new StringBuilder();
    TestSubscriber<CharBuffer> subscriber = item -> { while (item.hasRemaining()) result.append(item.get()); };
    processor.subscribe(subscriber);
    
    ByteBuffer byteBuffer = ByteBuffer.wrap(AEGAEUM.getBytes(charset), 0, 1);
    for (int i = 0; i <= byteBuffer.capacity(); i++) {
      byteBuffer.limit(i);
      processor.onNext(byteBuffer);
    }
    assertThat(result.toString(), is(AEGAEUM));
  }
}
