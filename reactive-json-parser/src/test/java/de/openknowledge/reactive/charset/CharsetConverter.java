package de.openknowledge.reactive.charset;

import java.nio.charset.Charset;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

public class CharsetConverter implements ArgumentConverter {

  @Override
  public Object convert(Object charsetName, ParameterContext context) throws ArgumentConversionException {
    return Charset.forName(charsetName.toString());
  }
}
