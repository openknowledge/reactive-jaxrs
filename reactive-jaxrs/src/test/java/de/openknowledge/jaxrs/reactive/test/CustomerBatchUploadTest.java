package de.openknowledge.jaxrs.reactive.test;

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

    // Upload some Customers
    ClientBuilder.newClient().target(url.toURI())
      .path("customers")
      .request()
      .put(entity(getClass().getResourceAsStream("/customers.json"), MediaType.APPLICATION_JSON_TYPE));

    // Get the uploaded Customers
    Response response = ClientBuilder.newClient().target(url.toURI())
      .path("customers")
      .request(MediaType.APPLICATION_JSON)
      .get();

    List<Customer> customers = response.readEntity(new GenericType<List<Customer>>() {});

    assertThat(customers.size()).isEqualTo(1000);
  }
}
