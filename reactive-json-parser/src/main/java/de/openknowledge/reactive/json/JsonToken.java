package de.openknowledge.reactive.json;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JsonToken {

  public static final JsonToken START_ARRAY = new JsonToken('[');
  public static final JsonToken END_ARRAY = new JsonToken(']');
  public static final JsonToken START_OBJECT = new JsonToken('{');
  public static final JsonToken END_OBJECT = new JsonToken('}');
  public static final JsonToken COMMA = new JsonToken(',');
  public static final JsonToken COLON = new JsonToken(':');
  public static final JsonToken VALUE_TRUE = new JsonToken('t', "true");
  public static final JsonToken VALUE_FALSE = new JsonToken('f', "false");
  public static final JsonToken VALUE_NULL = new JsonToken('n', "null");
  public static final JsonToken VALUE_ZERO = new JsonToken('0', "0");
  public static final Map<Character, JsonToken> KEYWORDS;
  static {
    Map<Character, JsonToken> keywords = new HashMap<>();
    keywords.put('t', VALUE_TRUE);
    keywords.put('f', VALUE_FALSE);
    keywords.put('n', VALUE_NULL);
    KEYWORDS = Collections.unmodifiableMap(keywords);
  }

  private char token;
  private String value;
  private BigDecimal number;

  public JsonToken(char token) {
    this(token, Character.toString(token));
  }

  public JsonToken(char token, String value) {
    this.token = token;
    this.value = value;
  }

  public JsonToken(char token, String value, BigDecimal number) {
    this(token, value);
    this.number = number;
  }

  public String getValue() {
    return value;
  }

  public char toChar() {
    return token;
  }

  public String toString() {
    return "JsonToken('" + token + "', \"" + value + "\")";
  }

  @Override
  public int hashCode() {
    return Objects.hash(token, value, number);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (null == object) {
      return false;
    }
    if (getClass() != object.getClass()) {
      return false;
    }
    JsonToken that = (JsonToken)object;
    return token == that.token && Objects.equals(value, that.value) && Objects.equals(number, that.number);
  }
}
