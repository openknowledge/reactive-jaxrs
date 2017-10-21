package de.openknowledge.jaxrs.reactive.test;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.client.Entity.entity;
import static org.assertj.core.api.Assertions.assertThat;

@RunAsClient
@RunWith(Arquillian.class)
public class CustomerBatchUploadTest {

  @Deployment
  public static WebArchive deployment() {
    return ShrinkWrap.create(WebArchive.class)
      .addPackage(Customer.class.getPackage())
      .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class)
        .addDefaultNamespaces()
        .version("3.1")
        .exportAsString()));
  }

  @Test
  public void put(@ArquillianResource URL url) throws URISyntaxException, IOException {

    int amount = 100000;

    List<Customer> randomCustomer = createRandomCustomers(amount);

    Instant start = Instant.now();

    // Upload some Customers
    ClientBuilder.newClient().target(url.toURI())
      .path("customers")
      .request()
      .put(entity(randomCustomer, MediaType.APPLICATION_JSON_TYPE));

    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);

    // Get the uploaded Customers
    Response response = ClientBuilder.newClient().target(url.toURI())
      .path("customers")
      .request(MediaType.APPLICATION_JSON)
      .get();

    List<Customer> customers = response.readEntity(new GenericType<List<Customer>>() {});

    assertThat(customers.size()).isEqualTo(amount);

    System.out.println(String.format("**** Customers: %s, Duration: %s ms", amount, duration.toMillis()));
  }

  private List<Customer> createRandomCustomers(int amount) {
    List<Customer> list = new ArrayList<>(amount);
    for (int i = 0; i < amount; i++) {
      list.add(createRandomCustomer());
    }
    return list;
  }

  private Customer createRandomCustomer() {
    Customer customer = new Customer();
    customer.setFirstName(createRandomString(15));
    customer.setLastName(createRandomString(15));
    return customer;
  }

  private String createRandomString(int length) {
    return RandomStringUtils.random(length, /*useLetters*/ true, /*useNumbers*/ false);
  }

}
