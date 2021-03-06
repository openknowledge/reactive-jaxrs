package de.openknowledge.reactive.json;

import javax.json.stream.JsonParsingException;
import java.util.ArrayDeque;
import java.util.Deque;

import de.openknowledge.reactive.commons.AbstractSimpleProcessor;

public class JsonArrayProcessor extends AbstractSimpleProcessor<JsonToken, String> {

  private State state = State.EXPECT_ARRAY;
  private Deque<JsonToken> tokenStack = new ArrayDeque<>();
  private StringBuilder buffer = new StringBuilder();

  @Override
  public void onNext(JsonToken token) {
    switch (state) {
      case EXPECT_ARRAY:
        if (JsonToken.START_ARRAY.equals(token)) {
          tokenStack.push(token);
          state = State.EXPECT_VALUE;
          request(1);
        } else {
          emitError(JsonToken.START_ARRAY, token);
        }
        break;
      case EXPECT_VALUE:
        if (JsonToken.START_ARRAY.equals(token) || JsonToken.START_OBJECT.equals(token)) {
          tokenStack.push(token);
        } else if (JsonToken.END_ARRAY.equals(token) || JsonToken.END_OBJECT.equals(token)) {
          JsonToken opening = tokenStack.pop();
          if (!match(opening, token)) {
            emitError(JsonToken.END_ARRAY.equals(token) ? JsonToken.END_ARRAY : JsonToken.END_OBJECT, token);
          }
        }
        if (token.toChar() == '"') {
          buffer.append(token.toChar());
        }
        buffer.append(token.getValue());
        if (token.toChar() == '"') {
          buffer.append(token.toChar());
        }
        if (tokenStack.size() == 1) {
          String value = buffer.toString();
          buffer.setLength(0);
          state = State.EXPECT_COMMA;
          publish(value);
        } else {
          request(1);
        }
        break;
      case EXPECT_COMMA:
        if (JsonToken.COMMA.equals(token)) {
          state = State.EXPECT_VALUE;
          request(1);
        } else if (JsonToken.END_ARRAY.equals(token)) {
          tokenStack.pop();
          onComplete();
        } else {
          emitError(JsonToken.COMMA, token);
        }
        break;
      default:
        onError(new JsonParsingException("Unexpected token " + token.getValue(), null));
    }
  }

  private boolean match(JsonToken opening, JsonToken closing) {
    return (JsonToken.START_ARRAY.equals(opening) && JsonToken.END_ARRAY.equals(closing))
      || (JsonToken.START_OBJECT.equals(opening) && JsonToken.END_OBJECT.equals(closing));
  }

  private void emitError(JsonToken expected, JsonToken actual) {
    onError(new JsonParsingException("Invalid token, expecting " + expected.getValue() + " got " + actual.getValue(), null));
  }

  private enum State {
    EXPECT_ARRAY, EXPECT_VALUE, EXPECT_COMMA
  }
}
