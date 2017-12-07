package de.openknowledge.jaxrs.reactive.converter;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

/**
 * @author Robert Zilke - open knowledge GmbH
 */
public class JsonConverterTest {

  private JsonConverter jsonConverter;

  @Before
  public void setUp()
    throws Exception {

    jsonConverter = new JsonConverter();
    jsonConverter.onSubscribe(Mockito.mock(Flow.Subscription.class));
  }

  @Test
  public void testSimpleObjectsOneArray()
    throws Exception {

    final boolean[] onCompleteInvoked = {false};

    List<String> items = new ArrayList<>();

    jsonConverter.subscribe(new AbstractSubscriber<>() {

      @Override
      public void onNext(String item) {

        items.add(item);
      }

      @Override
      public void onComplete() {

        onCompleteInvoked[0] = true;
      }
    });

    jsonConverter.onNext(
      "[{\"firstName\":\"Elvis\",\"lastName\":\"Presley\"},{\"firstName\":\"Peter\",\"lastName\":\"Lustig\"}]"
        .getBytes(StandardCharsets.UTF_8));
    jsonConverter.onComplete();

    assertEquals(2, items.size());

    assertEquals("{\"firstName\":\"Elvis\",\"lastName\":\"Presley\"}", items.get(0));
    assertEquals("{\"firstName\":\"Peter\",\"lastName\":\"Lustig\"}", items.get(1));

    assertTrue(onCompleteInvoked[0]);
  }

  @Test
  public void testSimpleObjectsMultipleArrays()
    throws Exception {

    final boolean[] onCompleteInvoked = {false};

    List<String> items = new ArrayList<>();

    jsonConverter.subscribe(new AbstractSubscriber<>() {

      @Override
      public void onNext(String item) {

        items.add(item);
      }

      @Override
      public void onComplete() {

        onCompleteInvoked[0] = true;
      }
    });

    jsonConverter.onNext("[{\"firstName\":\"Elv".getBytes(StandardCharsets.UTF_8));
    jsonConverter.onNext("is\",\"lastName\":\"Pres".getBytes(StandardCharsets.UTF_8));
    jsonConverter.onNext("ley\"},{\"firstName\":\"Pe".getBytes(StandardCharsets.UTF_8));
    jsonConverter.onNext("ter\",\"lastName\":\"Lus".getBytes(StandardCharsets.UTF_8));
    jsonConverter.onNext("tig\"},".getBytes(StandardCharsets.UTF_8));
    jsonConverter.onNext("{\"firstName\":\"Biene\",\"lastName\":\"Maia\"}".getBytes(StandardCharsets.UTF_8));
    jsonConverter.onNext("]".getBytes(StandardCharsets.UTF_8));
    jsonConverter.onComplete();

    assertEquals(3, items.size());

    assertEquals("{\"firstName\":\"Elvis\",\"lastName\":\"Presley\"}", items.get(0));
    assertEquals("{\"firstName\":\"Peter\",\"lastName\":\"Lustig\"}", items.get(1));
    assertEquals("{\"firstName\":\"Biene\",\"lastName\":\"Maia\"}", items.get(2));

    assertTrue(onCompleteInvoked[0]);
  }

  @Test
  public void testSingleBytes()
    throws Exception {

    final boolean[] onCompleteInvoked = {false};

    List<String> items = new ArrayList<>();

    jsonConverter.subscribe(new AbstractSubscriber<>() {

      @Override
      public void onNext(String item) {

        items.add(item);
      }

      @Override
      public void onComplete() {

        onCompleteInvoked[0] = true;
      }
    });

    byte[] bytes = "[{\"firstName\":\"Elvis\",\"lastName\":\"Presley\"}]".getBytes(StandardCharsets.UTF_8);

    for (byte aByte : bytes) {
      jsonConverter.onNext(new byte[] {aByte});
    }
    jsonConverter.onComplete();

    assertEquals(1, items.size());
    assertEquals("{\"firstName\":\"Elvis\",\"lastName\":\"Presley\"}", items.get(0));
    assertTrue(onCompleteInvoked[0]);
  }

  @Test
  public void testNestedObjectsAndArrays()
    throws Exception {

    final boolean[] onCompleteInvoked = {false};

    List<String> items = new ArrayList<>();

    jsonConverter.subscribe(new AbstractSubscriber<>() {

      @Override
      public void onNext(String item) {

        items.add(item);
      }

      @Override
      public void onComplete() {

        onCompleteInvoked[0] = true;
      }
    });

    String complexJson = "[\n" +
      "  {\n" +
      "    \"firstName\": \"Elvis\",\n" +
      "    \"lastName\": \"Presley\",\n" +
      "    \"friends\": [\n" +
      "      {\n" +
      "        \"firstName\": \"Michael\",\n" +
      "        \"lastName\": \"Jackson\"\n" +
      "      },\n" +
      "      {\n" +
      "        \"firstName\": \"Max\",\n" +
      "        \"lastName\": \"Mustermann\"\n" +
      "      }\n" +
      "    ],\n" +
      "    \"bestFriend\": {\n" +
      "      \"firstName\": \"Peter\",\n" +
      "      \"lastName\": \"Lustig\",\n" +
      "      \"friends\": [\n" +
      "        {\n" +
      "          \"firstName\": \"Michael\",\n" +
      "          \"lastName\": \"Jackson\"\n" +
      "        },\n" +
      "        {\n" +
      "          \"firstName\": \"Max\",\n" +
      "          \"lastName\": \"Mustermann\"\n" +
      "        }\n" +
      "      ]\n" +
      "    }\n" +
      "  },\n" +
      "  {\n" +
      "    \"firstName\": \"Peter\",\n" +
      "    \"lastName\": \"Lustig\",\n" +
      "    \"friends\": []\n" +
      "  }\n" +
      "]";

    jsonConverter.onNext(complexJson.getBytes(StandardCharsets.UTF_8));

    jsonConverter.onComplete();

    assertEquals(2, items.size());

    assertEquals("{\n" +
      "    \"firstName\": \"Elvis\",\n" +
      "    \"lastName\": \"Presley\",\n" +
      "    \"friends\": [\n" +
      "      {\n" +
      "        \"firstName\": \"Michael\",\n" +
      "        \"lastName\": \"Jackson\"\n" +
      "      },\n" +
      "      {\n" +
      "        \"firstName\": \"Max\",\n" +
      "        \"lastName\": \"Mustermann\"\n" +
      "      }\n" +
      "    ],\n" +
      "    \"bestFriend\": {\n" +
      "      \"firstName\": \"Peter\",\n" +
      "      \"lastName\": \"Lustig\",\n" +
      "      \"friends\": [\n" +
      "        {\n" +
      "          \"firstName\": \"Michael\",\n" +
      "          \"lastName\": \"Jackson\"\n" +
      "        },\n" +
      "        {\n" +
      "          \"firstName\": \"Max\",\n" +
      "          \"lastName\": \"Mustermann\"\n" +
      "        }\n" +
      "      ]\n" +
      "    }\n" +
      "  }", items.get(0));

    assertEquals("{\n" +
      "    \"firstName\": \"Peter\",\n" +
      "    \"lastName\": \"Lustig\",\n" +
      "    \"friends\": []\n" +
      "  }", items.get(1));

    assertTrue(onCompleteInvoked[0]);
  }

  @Test
  public void testSingleObject()
    throws Exception {

    final boolean[] onCompleteInvoked = {false};

    List<String> items = new ArrayList<>();

    jsonConverter.subscribe(new AbstractSubscriber<>() {

      @Override
      public void onNext(String item) {

        items.add(item);
      }

      @Override
      public void onComplete() {

        onCompleteInvoked[0] = true;
      }
    });

    byte[] bytes = "{\"firstName\":\"Elvis\",\"lastName\":\"Presley\"}".getBytes();

    for (byte aByte : bytes) {
      jsonConverter.onNext(new byte[] {aByte});
    }
    jsonConverter.onComplete();

    assertEquals(1, items.size());
    assertEquals("{\"firstName\":\"Elvis\",\"lastName\":\"Presley\"}", items.get(0));
    assertTrue(onCompleteInvoked[0]);
  }

  @Test
  public void testErredJson() {

    jsonConverter = new JsonConverter();
    Flow.Subscription subscription = Mockito.mock(Flow.Subscription.class);
    jsonConverter.onSubscribe(subscription);
    Flow.Subscriber subscriber = Mockito.mock(Flow.Subscriber.class);
    jsonConverter.subscribe(subscriber);
    jsonConverter.onNext("{]".getBytes());

    Mockito.verify(subscriber).onError(Mockito.any(IllegalArgumentException.class));
    Mockito.verify(subscription).cancel();
  }

  @Test
  public void testOnNextCanNotBeCalledAfterCompletion() {

    final boolean[] called = {false};
    jsonConverter.subscribe(new AbstractSubscriber<>() {
      @Override
      public void onComplete() {

        called[0] = true;
      }
    });
    jsonConverter.onNext("{}".getBytes());
    jsonConverter.onComplete();

    assertTrue(called[0]);
    assertThatThrownBy(() -> jsonConverter.onNext("{}".getBytes()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Processor is completed!");
  }

  @Test
  public void testOnNextThrowErrorIfProducerIsFinishedButMoreDataIsNeeded() {

    jsonConverter.subscribe(new AbstractSubscriber<>());
    jsonConverter.onComplete();
    assertThatThrownBy(() -> jsonConverter.onNext("{".getBytes()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Producer is already finished!");
  }

  @Test
  public void testOnErrorShouldDontFireCompletion() {

    Flow.Subscriber subscriber = Mockito.mock(Flow.Subscriber.class);
    jsonConverter.subscribe(subscriber);
    jsonConverter.onComplete();
    jsonConverter.onError(new Exception());
    jsonConverter.onNext("{}".getBytes());

    Mockito.verify(subscriber).onError(Mockito.any(Exception.class));
    Mockito.verify(subscriber, Mockito.times(0)).onComplete();
  }

  @Test
  public void testFireErrorIfParsingComplete() {

    Flow.Subscriber subscriber = Mockito.mock(Flow.Subscriber.class);
    jsonConverter.subscribe(subscriber);
    jsonConverter.onNext("{}{}".getBytes());

    Mockito.verify(subscriber).onError(Mockito.any(IllegalArgumentException.class));
  }
}
