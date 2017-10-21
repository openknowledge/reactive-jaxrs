package de.openknowledge.jaxrs.reactive.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert Zilke - open knowledge GmbH
 */
public class JsonConverterTest {

  private JsonConverter jsonConverter;

  @Before
  public void setUp()
      throws Exception {

    jsonConverter = new JsonConverter();
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

    assertEquals(2, items.size());
    assertTrue(onCompleteInvoked[0]);
  }

//  @Test
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
    jsonConverter.onNext("tig\"}]".getBytes(StandardCharsets.UTF_8));

    assertEquals(2, items.size());
    assertTrue(onCompleteInvoked[0]);
  }
}
