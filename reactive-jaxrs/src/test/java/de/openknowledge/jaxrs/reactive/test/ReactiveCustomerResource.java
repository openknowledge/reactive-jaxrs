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


import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.Flow;

@ApplicationScoped
@Path("/reactive/customers")
public class ReactiveCustomerResource {

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public void setCustomers(Flow.Publisher<Customer> customers) {
    customers.subscribe(new Flow.Subscriber<Customer>() {

      private int customerCount = 0;
      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Customer item) {
        System.out.println("=====  Customer Count: " + customerCount);
        customerCount++;
      }

      @Override
      public void onError(Throwable throwable) {

      }

      @Override
      public void onComplete() {
        System.out.println("=====  Customer Count: " + customerCount);
      }
    });
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<Customer> getCustomers() {
    return List.of(new Customer("Joe", "Doe "));
  }
}
