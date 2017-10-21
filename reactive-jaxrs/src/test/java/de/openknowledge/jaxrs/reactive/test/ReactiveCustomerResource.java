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
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Path("/reactive/customers")
public class ReactiveCustomerResource {

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public void setCustomers(Flow.Publisher<Customer> customers, @Suspended AsyncResponse response) {
    customers.subscribe(new Flow.Subscriber<Customer>() {
      private AtomicInteger customerCount = new AtomicInteger(0);
      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Customer item) {
        customerCount.getAndIncrement();
//        System.out.println("=====  Customer Count: " + customerCount.getAndIncrement());
      }

      @Override
      public void onError(Throwable throwable) {

      }

      @Override
      public void onComplete() {
        System.out.println("=====  Customer Count: " + customerCount.get());
        response.resume(Response.noContent().build());
      }
    });
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<Customer> getCustomers() {
    return List.of(new Customer(1, "Joe", "Doe "));
  }
}
