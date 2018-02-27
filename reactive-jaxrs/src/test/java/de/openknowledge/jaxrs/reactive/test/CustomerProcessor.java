package de.openknowledge.jaxrs.reactive.test;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.stream.Stream;

import de.openknowledge.reactive.AbstractSimpleProcessor;

public class CustomerProcessor extends AbstractSimpleProcessor<String, Customer> {

  @Override
  public void onNext(String customer) {
    Map<String, String> attributes = Stream.of(removeCurlyBrackets(customer).split(","))
        .map(s -> s.split(":")).collect(toMap(s -> s[0], s -> s[1]));
    publish(new Customer(attributes.get("firstName"), attributes.get("lastName")));
  }

  private String removeCurlyBrackets(String customer) {
    return customer.trim().substring(1, customer.length() - 1).trim(); 
  }
}
