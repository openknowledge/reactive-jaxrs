package de.openknowledge.reactive.json;

import java.math.BigDecimal;
import java.nio.CharBuffer;

import javax.json.stream.JsonParsingException;

import de.openknowledge.reactive.AbstractSimpleProcessor;

public class JsonTokenizer extends AbstractSimpleProcessor<CharBuffer, JsonToken> {

  private static final CharBuffer EMPTY = CharBuffer.wrap("");
  private CharBuffer buffer;
  private StringBuilder value = new StringBuilder();
  private State state = State.END;
  private ProcessState processState = ProcessState.STOPPED;

  public JsonTokenizer() {
    buffer = EMPTY;
  }

  @Override
  public void onNext(CharBuffer item) {
    if (!item.hasRemaining()) {
      super.request(1);
      return;
    }
    buffer = item;
    char character = buffer.get();
    switch (state) {
    case IN_STRING:
      readString(character);
      break;
    case IN_ESCAPE_SEQUENCE:
      readEscapeSequence(character);
      break;
    case IN_NUMBER:
      readNumber(character);
      break;
    case IN_TRUE:
      readKeyword("true", Character.toLowerCase(character));
      break;
    case IN_FALSE:
      readKeyword("false", Character.toLowerCase(character));
      break;
    case IN_NULL:
      readKeyword("null", Character.toLowerCase(character));
      break;
    default:
      readToken(character);
    }
  }

  @Override
  protected void request(long n) {
    readNext();
  }

  @Override
  protected void publish(JsonToken token) {
    state = State.END;
    super.publish(token);
    if (isRequested()) {
      readNext();
    }
  }

  private void readNext() {
    ProcessState oldState = processState;
    processState = ProcessState.RUNNING;
    if (oldState == ProcessState.STOPPED) {
      next();
    }
  }

  private void next() {
    while (processState == ProcessState.RUNNING) {
      processState = ProcessState.STOPPING;
      onNext(buffer);
    }
    processState = ProcessState.STOPPED;
  }

  private void readString(char character) {
    if (character == '"') {
      publish(new JsonToken(character, value.toString()));
    } else {
      if (character == '\\') {
        state = State.IN_ESCAPE_SEQUENCE;
      }
      value.append(character);
      readNext();
    }
  }

  private void readEscapeSequence(char character) {
    if (value.charAt(value.length() - 1) == '\\') {
      switch (character) {
      case '"':
        value.setCharAt(value.length() - 1, '"');
        state = State.IN_STRING;
        break;
      case '\\':
        // character already there
        state = State.IN_STRING;
        break;
      case '/':
        value.setCharAt(value.length() - 1, '/');
        state = State.IN_STRING;
        break;
      case 'b':
        value.setCharAt(value.length() - 1, '\b');
        state = State.IN_STRING;
        break;
      case 'f':
        value.setCharAt(value.length() - 1, '\f');
        state = State.IN_STRING;
        break;
      case 'n':
        value.setCharAt(value.length() - 1, '\n');
        state = State.IN_STRING;
        break;
      case 'r':
        value.setCharAt(value.length() - 1, '\r');
        state = State.IN_STRING;
        break;
      case 't':
        value.setCharAt(value.length() - 1, '\t');
        state = State.IN_STRING;
        break;
      case 'u':
        value.append(character);
        break;
      default:
        // TODO proper exception handling
        throw new JsonParsingException("", null);
      }
    } else {
      if (character < '0' || character > '9') {
        // TODO proper exception handling
        throw new JsonParsingException("", null);
      }
      value.append(character);
      if (value.charAt(value.length() - 6) == '\\' && value.charAt(value.length() - 5) == 'u') {
        String hexString = value.substring(value.length() - 4);
        value.delete(value.length() - 6, value.length());
        value.append((char)Integer.parseInt(hexString, 16));
        state = State.IN_STRING;
      }
    }
    readNext();
  }

  private void readNumber(char character) {
    switch (character) {
    case ']':
      publishNumber();
      publish(JsonToken.END_ARRAY);
      break;
    case '}':
      publishNumber();
      publish(JsonToken.END_OBJECT);
      break;
    case ',':
      publishNumber();
      publish(JsonToken.COMMA);
      break;
    default:
      if (Character.isWhitespace(character)) {
        publishNumber();
        if (isRequested()) {
          readNext();
        }
      } else {
        value.append(character);
        readNext();
      }
    }
  }

  private void publishNumber() {
    String number = value.toString();
    BigDecimal decimal;
    try {
      decimal = new BigDecimal(number);
    } catch (NumberFormatException e) {
      // TODO correct error handling
      throw new JsonParsingException("", e, null);
    }
    state = State.END;
    super.publish(new JsonToken('0', number, decimal));
  }

  private void readKeyword(String keyword, char character) {
    if (character != keyword.charAt(value.length())) {
      // TODO correct exception handling
      throw new JsonParsingException("", null);
    }
    value.append(character);
    if (keyword.length() == value.length()) {
      publish(JsonToken.KEYWORDS.get(keyword.charAt(0)));
    } else {
      readNext();
    }
  }

  private void readToken(char character) {
    switch (character) {
    case '[':
      publish(JsonToken.START_ARRAY);
      break;
    case ']':
      publish(JsonToken.END_ARRAY);
      break;
    case '{':
      publish(JsonToken.START_OBJECT);
      break;
    case '}':
      publish(JsonToken.END_OBJECT);
      break;
    case ',':
      publish(JsonToken.COMMA);
      break;
    case ':':
      publish(JsonToken.COLON);
      break;
    case '"':
      startValue(State.IN_STRING, character);
      break;
    case '0':
      publish(JsonToken.VALUE_ZERO);
      break;
    case '-':
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
    case '6':
    case '7':
    case '8':
    case '9':
      startValue(State.IN_NUMBER, character);
      break;
    case 't':
    case 'T':
      startValue(State.IN_TRUE, Character.toLowerCase(character));
      break;
    case 'f':
    case 'F':
      startValue(State.IN_FALSE, Character.toLowerCase(character));
      break;
    case 'n':
    case 'N':
      startValue(State.IN_NULL, Character.toLowerCase(character));
      break;
    default:
      // eat whitespaces
      if (Character.isWhitespace(character)) {
        readNext();
      } else {
        // TODO proper exception handling
        onError(new JsonParsingException("unhandled character: " + (int)character, null));
      }
    }
  }

  private void startValue(State newState, char character) {
    state = newState;
    value.setLength(0);
    if (newState != State.IN_STRING) {
      value.append(character);
    }
    readNext();
  }

  private enum State {
    IN_STRING, IN_ESCAPE_SEQUENCE, IN_NUMBER, IN_TRUE, IN_FALSE, IN_NULL, END;
  }

  private enum ProcessState {
    RUNNING, STOPPING, STOPPED;
  }
}
