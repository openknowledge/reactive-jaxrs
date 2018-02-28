package de.openknowledge.reactive.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class JsonTokenizerTest {

  public static final String SAMPLE_JSON_OBJECT
    = "{\"key\": [true, false],\n"
    + " \"key\\t2\": {\"key\" : \"value\"},"
    + " \"key\\n3\": true,"
    + " \"key\\u0000\": false,"
    + " \"key5\": null"
    + "}";
  public static final String SAMPLE_TRIMMED_JSON_OBJECT
  = "{\"key\":[true,false],"
  + "\"key\t2\":{\"key\":\"value\"},"
  + "\"key\n3\":true,"
  + "\"key\u0000\":false,"
  + "\"key5\":null"
  + "}";
  public static final String SAMPLE_JSON_ARRAY
    = "[\n"
    + SAMPLE_JSON_OBJECT + ", \n"
    + "{}, \n"
    + "[1,2,3],"
    + "[] ,\n"
    + "\"Json String\","
    + "0 , \n"
    + "42,"
    + "-11,\n"
    + "3.141, 3e141,"
    + "true, false, null"
    + "]";
  public static final JsonToken[] SAMPLE_TOKENS
    = {
        JsonToken.START_ARRAY,
        JsonToken.START_OBJECT,
        new JsonToken('"', "key"),
        JsonToken.COLON,
        JsonToken.START_ARRAY,
        JsonToken.VALUE_TRUE,
        JsonToken.COMMA,
        JsonToken.VALUE_FALSE,
        JsonToken.END_ARRAY,
        JsonToken.COMMA,
        new JsonToken('"', "key\t2"),
        JsonToken.COLON,
        JsonToken.START_OBJECT,
        new JsonToken('"', "key"),
        JsonToken.COLON,
        new JsonToken('"', "value"),
        JsonToken.END_OBJECT,
        JsonToken.COMMA,
        new JsonToken('"', "key\n3"),
        JsonToken.COLON,
        JsonToken.VALUE_TRUE,
        JsonToken.COMMA,
        new JsonToken('"', "key\u0000"),
        JsonToken.COLON,
        JsonToken.VALUE_FALSE,
        JsonToken.COMMA,
        new JsonToken('"', "key5"),
        JsonToken.COLON,
        JsonToken.VALUE_NULL,
        JsonToken.END_OBJECT,
        JsonToken.COMMA,
        JsonToken.START_OBJECT,
        JsonToken.END_OBJECT,
        JsonToken.COMMA,
        JsonToken.START_ARRAY,
        new JsonToken('0', "1", new BigDecimal("1")),
        JsonToken.COMMA,
        new JsonToken('0', "2", new BigDecimal("2")),
        JsonToken.COMMA,
        new JsonToken('0', "3", new BigDecimal("3")),
        JsonToken.END_ARRAY,
        JsonToken.COMMA,
        JsonToken.START_ARRAY,
        JsonToken.END_ARRAY,
        JsonToken.COMMA,
        new JsonToken('"', "Json String"),
        JsonToken.COMMA,
        JsonToken.VALUE_ZERO,
        JsonToken.COMMA,
        new JsonToken('0', "42", new BigDecimal("42")),
        JsonToken.COMMA,
        new JsonToken('0', "-11", new BigDecimal("-11")),
        JsonToken.COMMA,
        new JsonToken('0', "3.141", new BigDecimal("3.141")),
        JsonToken.COMMA,
        new JsonToken('0', "3e141", new BigDecimal("3e141")),
        JsonToken.COMMA,
        JsonToken.VALUE_TRUE,
        JsonToken.COMMA,
        JsonToken.VALUE_FALSE,
        JsonToken.COMMA,
        JsonToken.VALUE_NULL,
        JsonToken.END_ARRAY
  };
  private JsonTokenizer jsonTokenizer = new JsonTokenizer();
  
  @Test
  public void correctSequence() {
    CharBuffer buffer = CharBuffer.wrap(SAMPLE_JSON_ARRAY.toCharArray());
    Subscription subscription = mock(Subscription.class);
    VoidAnswer onNext = invocation -> jsonTokenizer.onNext(buffer);
    VoidAnswer onComplete = invocation -> jsonTokenizer.onComplete();
    doAnswer(onNext).doAnswer(onComplete).when(subscription).request(1L);
    jsonTokenizer.onSubscribe(subscription);
    
    Subscriber<JsonToken> subscriber = mock(Subscriber.class);
    VoidAnswer requestAll = invocation -> invocation.<Subscription>getArgument(0).request(Long.MAX_VALUE);
    doAnswer(requestAll).when(subscriber).onSubscribe(any());
    jsonTokenizer.subscribe(subscriber);

    ArgumentCaptor<JsonToken> tokens = ArgumentCaptor.forClass(JsonToken.class);
    verify(subscriber, times(SAMPLE_TOKENS.length)).onNext(tokens.capture());
    assertThat(tokens.getAllValues()).containsExactly(SAMPLE_TOKENS);
  }

  @Test
  public void correctRequests() {
    CharBuffer buffer = CharBuffer.wrap(SAMPLE_JSON_ARRAY.toCharArray());
    Subscription bufferSubscription = mock(Subscription.class);
    doAnswer(new BufferAnswer(buffer)).when(bufferSubscription).request(1L);
    jsonTokenizer.onSubscribe(bufferSubscription);
    
    Subscriber<JsonToken> subscriber = mock(Subscriber.class);
    RequestOneAnswer answer = new RequestOneAnswer();
    doAnswer(answer).when(subscriber).onSubscribe(any());
    doAnswer(answer).when(subscriber).onNext(any());
    jsonTokenizer.subscribe(subscriber);

    verify(bufferSubscription, times(buffer.capacity() + 1)).request(anyLong());
    ArgumentCaptor<JsonToken> tokens = ArgumentCaptor.forClass(JsonToken.class);
    verify(subscriber, times(SAMPLE_TOKENS.length)).onNext(tokens.capture());
    assertThat(tokens.getAllValues()).containsExactly(SAMPLE_TOKENS);
  }

  public static interface VoidAnswer extends Answer<Void> {
    default Void answer(InvocationOnMock invocation) throws Throwable {
      execute(invocation);
      return null;
    }

    void execute(InvocationOnMock invocation);
  }

  public class BufferAnswer implements Answer<Void> {
    private int index;
    private CharBuffer buffer;
    
    public BufferAnswer(CharBuffer charBuffer) {
      buffer = charBuffer;
    }

    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      if (index == buffer.capacity()) {
        jsonTokenizer.onComplete();
        return null;
      }
      buffer.position(index);
      buffer.limit(++index);
      jsonTokenizer.onNext(buffer);
      return null;
    }
  }

  public static class RequestOneAnswer implements Answer<Void> {

    private Subscription subscription;

    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      if (invocation.getArgument(0) instanceof Subscription) {
        subscription = invocation.getArgument(0);
      }
      subscription.request(1);
      return null;
    }
  }
}
