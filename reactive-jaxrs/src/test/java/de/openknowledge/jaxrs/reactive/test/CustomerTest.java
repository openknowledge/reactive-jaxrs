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

import static javax.ws.rs.client.Entity.entity;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import org.junit.Test;
import org.junit.runner.RunWith;

import de.openknowledge.jaxrs.reactive.flow.SingleItemPublisher;

@RunAsClient
@RunWith(Arquillian.class)
public class CustomerTest {

  @Deployment
  public static WebArchive deployment() {
    PomEquippedResolveStage pom = Maven.resolver().loadPomFromFile("pom.xml");
    return ShrinkWrap.create(WebArchive.class)
      .addPackage(SingleItemPublisher.class.getPackage())
      .addPackage(Customer.class.getPackage())
      .addAsLibraries(pom.resolve("commons-io:commons-io").withTransitivity().asFile())
      .setWebXML(new StringAsset(Descriptors.create(WebAppDescriptor.class)
        .addDefaultNamespaces()
        .version("3.1")
        .exportAsString()));
  }

  @Test
  public void putOneCustomer(@ArquillianResource URL url) throws URISyntaxException {

    ClientBuilder.newClient().target(url.toURI())
      .path("customers")
      .request()
      .put(entity("[{\"firstName\": \"John\", \"lastName\": \"Doe\"}]", MediaType.APPLICATION_JSON_TYPE));

    Response response = ClientBuilder.newClient().target(url.toURI())
      .path("customers")
      .request(MediaType.APPLICATION_JSON)
      .get();

    List<Customer> customers = response.readEntity(new GenericType<List<Customer>>() {});

    Customer john = new Customer();
    john.setFirstName("John");
    john.setLastName("Doe");

    assertThat(customers).isEqualTo(List.of(john));
  }

  @Test
  public void putCustomers(@ArquillianResource URL url) throws URISyntaxException {

    ClientBuilder.newClient().target(url.toURI())
      .path("customers")
      .request()
      .put(entity("[{\"firstName\": \"John\", \"lastName\": \"Doe\"}, {\"firstName\": \"Jane\", \"lastName\": \"Doe\"}]", MediaType.APPLICATION_JSON_TYPE));

    Response response = ClientBuilder.newClient().target(url.toURI())
      .path("customers")
      .request(MediaType.APPLICATION_JSON)
      .get();

    List<Customer> customers = response.readEntity(new GenericType<List<Customer>>() {});

    Customer john = new Customer();
    john.setFirstName("John");
    john.setLastName("Doe");

    Customer jane = new Customer();
    jane.setFirstName("Jane");
    jane.setLastName("Doe");

    assertThat(customers).isEqualTo(List.of(john, jane));
  }
}
