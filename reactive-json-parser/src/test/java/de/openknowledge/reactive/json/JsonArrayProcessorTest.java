package de.openknowledge.reactive.json;

import static de.openknowledge.reactive.json.JsonTokenizerTest.SAMPLE_JSON_ARRAY;
import static de.openknowledge.reactive.json.JsonTokenizerTest.SAMPLE_TOKENS;
import static de.openknowledge.reactive.json.JsonTokenizerTest.SAMPLE_TRIMMED_JSON_OBJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.CharBuffer;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.openknowledge.reactive.json.JsonTokenizerTest.RequestOneAnswer;
import de.openknowledge.reactive.json.JsonTokenizerTest.VoidAnswer;


public class JsonArrayProcessorTest {

  private JsonArrayProcessor processor = new JsonArrayProcessor();

  @Test
  public void correctItems() {
    Subscription subscription = mock(Subscription.class);
    Answer<Void> onNext = new Answer<Void>() {
      public Void answer(InvocationOnMock invocation) throws Throwable {
        for (JsonToken token: SAMPLE_TOKENS) {
          processor.onNext(token);
        }
        return null;
      }
    };
    doAnswer(onNext).when(subscription).request(Long.MAX_VALUE);
    processor.onSubscribe(subscription);
    
    Subscriber<String> subscriber = mock(Subscriber.class);
    VoidAnswer requestAll = invocation -> invocation.<Subscription>getArgument(0).request(Long.MAX_VALUE);
    doAnswer(requestAll).when(subscriber).onSubscribe(any());
    processor.subscribe(subscriber);

    ArgumentCaptor<String> values = ArgumentCaptor.forClass(String.class);
    verify(subscriber).onSubscribe(any());
    verify(subscriber, times(13)).onNext(values.capture());
    assertThat(values.getAllValues()).containsExactly(
        SAMPLE_TRIMMED_JSON_OBJECT,
        "{}",
        "[1,2,3]",
        "[]",
        "\"Json String\"",
        "0",
        "42",
        "-11",
        "3.141",
        "3e141",
        "true",
        "false",
        "null");
  }

  @Test
  public void correctRequests() {
    CharBuffer buffer = CharBuffer.wrap(SAMPLE_JSON_ARRAY.toCharArray());
    Subscription subscription = mock(Subscription.class);
    doAnswer(new TokenAnswer(SAMPLE_TOKENS)).when(subscription).request(1L);
    processor.onSubscribe(subscription);
    
    Subscriber<String> subscriber = mock(Subscriber.class);
    RequestOneAnswer answer = new RequestOneAnswer(); 
    doAnswer(answer).when(subscriber).onSubscribe(any());
    doAnswer(answer).when(subscriber).onNext(any());
    processor.subscribe(subscriber);

    verify(subscription, times(SAMPLE_TOKENS.length)).request(anyLong());
    ArgumentCaptor<String> values = ArgumentCaptor.forClass(String.class);
    verify(subscriber, times(13)).onNext(values.capture());
    assertThat(values.getAllValues()).containsExactly(
        SAMPLE_TRIMMED_JSON_OBJECT,
        "{}",
        "[1,2,3]",
        "[]",
        "\"Json String\"",
        "0",
        "42",
        "-11",
        "3.141",
        "3e141",
        "true",
        "false",
        "null");
  }

  public class TokenAnswer implements Answer<Void> {
    private int index;
    private JsonToken[] tokens;
    
    public TokenAnswer(JsonToken[] jsonTokens) {
      tokens = jsonTokens;
    }

    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      if (index == tokens.length) {
        processor.onComplete();
        return null;
      }
      processor.onNext(tokens[index++]);
      return null;
    }
  }
}
