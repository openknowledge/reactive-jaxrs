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

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.net.URL;
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

import static javax.ws.rs.client.Entity.entity;

@RunAsClient
@RunWith(Arquillian.class)
public class CustomerTest {
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
  public void put(@ArquillianResource URL url) throws URISyntaxException {
    ClientBuilder.newClient()
      .target(url.toURI()).path("customers")
      .request()
      .put(entity("[{'firstName': 'Lustiger', 'lastName': 'Peter'}]", MediaType.APPLICATION_JSON_TYPE));
  }
}
