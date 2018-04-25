package de.openknowledge.jaxrs.reactive.test;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.openknowledge.reactive.AbstractSimpleProcessor;

public class CustomerWriter extends AbstractSimpleProcessor<Customer, String> {

  private ObjectMapper mapper = new ObjectMapper();
  @Override
  public void onNext(Customer customer) {
    
    try {
      publish(mapper.writeValueAsString(customer));
    } catch (IOException e) {
      onError(e);
    }
  }
}
