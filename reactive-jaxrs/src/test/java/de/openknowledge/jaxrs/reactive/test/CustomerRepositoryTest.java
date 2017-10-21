package de.openknowledge.jaxrs.reactive.test;

import org.junit.Test;

import java.io.IOException;

import static java.util.Arrays.asList;

public class CustomerRepositoryTest {

  @Test
  public void save() throws IOException, InterruptedException {
    CustomerRepository repository = new CustomerRepository();
    repository.initialize();

    repository.save(asList(new Customer(1, "John", "Doe"), new Customer(2, "Jane", "Doe")));

    Thread.sleep(10000);

    //assertThat(repository.findAll()).containsExactly(new Customer("John", "Doe"), new Customer("Jane", "Doe"));
  }
}
