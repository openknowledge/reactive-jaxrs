/*
 * Copyright (C) open knowledge GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package de.openknowledge.jaxrs.reactive.test;

import de.openknowledge.jaxrs.reactive.PublisherMessageBodyReader;
import de.openknowledge.jaxrs.reactive.converter.JsonConverter;
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
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static de.openknowledge.jaxrs.reactive.test.StopWatch.time;
import static javax.ws.rs.client.Entity.entity;
import static org.assertj.core.api.Assertions.assertThat;

@RunAsClient
@RunWith(Arquillian.class)
public class CustomerBatchUploadTest {

  @ArquillianResource private URL url;

  @Deployment
  public static WebArchive deployment() {
    PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile("pom.xml");

    return ShrinkWrap.create(WebArchive.class)
      .addPackage(Customer.class.getPackage())
      .addPackage(JsonConverter.class.getPackage())
      .addPackage(PublisherMessageBodyReader.class.getPackage())
      .addAsLibraries(pom.resolve("de.undercouch:actson:1.2.0").withTransitivity().asFile())
      .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class)
        .addDefaultNamespaces()
        .version("3.1")
        .exportAsString()));
  }

  @Before
  public void warmUp() throws URISyntaxException {
    ClientBuilder.newClient().target(url.toURI()).path("customers").request().get();
  }

  @Test
  public void synchronous() throws Exception {

    int amount = 100000;

    List<Customer> customers = createRandomCustomers(amount);

    // Upload some Customers
    Duration duration = time(() -> {
        Response response = ClientBuilder.newClient().target(url.toURI())
          .path("customers")
          .request()
          .put(entity(customers, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(204);
      }

    );

    System.out.println(String.format("**** synchronouse: Customers: %s, Duration: %s ms", amount, duration.toMillis()));
  }

  @Test
  public void asynchronous() throws Exception {

    int amount = 100000;

    List<Customer> customers = createRandomCustomers(amount);

    // Upload some Customers
    Duration duration = time(() -> {
        Response response = ClientBuilder.newClient().target(url.toURI())
          .path("reactive/customers")
          .request()
          .put(entity(customers, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(204);
      }
    );

    System.out.println(String.format("**** asynchronous: Customers: %s, Duration: %s ms", amount, duration.toMillis()));
  }

  private List<Customer> createRandomCustomers(int amount) {
    List<Customer> list = new ArrayList<>(amount);
    for (int i = 0; i < amount; i++) {
      list.add(createRandomCustomer(i));
    }
    return list;
  }

  private Customer createRandomCustomer(int id) {
    Customer customer = new Customer();
    customer.setFirstName(createRandomString(15));
    customer.setLastName(createRandomString(15));
    return customer;
  }

  private String createRandomString(int length) {
    return RandomStringUtils.random(length, /*useLetters*/ true, /*useNumbers*/ false);
  }
}
