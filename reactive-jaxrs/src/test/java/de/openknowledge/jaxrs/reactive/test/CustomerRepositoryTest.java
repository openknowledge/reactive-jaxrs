package de.openknowledge.jaxrs.reactive.test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

public class CustomerRepositoryTest {

  @Test
  public void save() throws IOException, InterruptedException {
    CustomerRepository repository = new CustomerRepository();
    repository.initialize();
    
    repository.save(asList(new Customer("John", "Doe"), new Customer("Jane", "Doe")));
    
    Thread.sleep(10000);
    
    assertThat(repository.findAll()).containsExactly(new Customer("John", "Doe"), new Customer("Jane", "Doe"));
  }
}
