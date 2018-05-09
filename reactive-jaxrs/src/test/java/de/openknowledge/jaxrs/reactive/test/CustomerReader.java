package de.openknowledge.jaxrs.reactive.test;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.openknowledge.reactive.commons.AbstractSimpleProcessor;

public class CustomerReader extends AbstractSimpleProcessor<String, Customer> {

  private ObjectMapper mapper = new ObjectMapper();
  @Override
  public void onNext(String customer) {
    
    try {
      publish(mapper.readValue(customer, Customer.class));
    } catch (IOException e) {
      onError(e);
    }
  }
}
