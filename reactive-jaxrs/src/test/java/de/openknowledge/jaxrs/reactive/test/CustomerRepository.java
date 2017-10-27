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

import static de.openknowledge.jaxrs.reactive.flow.CompletableSubscriber.pullEach;
import static de.openknowledge.jaxrs.reactive.flow.CompletableSubscriber.pushEach;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.IOUtils;

import de.openknowledge.jaxrs.reactive.flow.SingleItemPublisher;

@ApplicationScoped
public class CustomerRepository {

  private Path path;

  @PostConstruct
  public void initialize() {
    try {
      path = Paths.get("customers.json");
      if (!Files.exists(path)) {
        Files.createFile(path);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int save(List<Customer> newCustomers) throws Exception {
    AtomicInteger count = new AtomicInteger(0);
    AtomicReference<Throwable> error = new AtomicReference<>(null);
    save(new Publisher<Customer>() {

      private List<Customer> customers = new ArrayList<>(newCustomers);
      private Subscriber<? super Customer> subscriber;

      @Override
      public void subscribe(Subscriber<? super Customer> s) {
        subscriber = s;
        subscriber.onSubscribe(new Subscription() {

          @Override
          public void request(long count) {
            if (subscriber == null) {
              return;
            }
            for (int i = 0; i < count; i++) {
              if (!customers.isEmpty()) {
                subscriber.onNext(customers.remove(0));
              }
            }
            if (customers.isEmpty()) {
              subscriber.onComplete();
              subscriber = null;
            }
          }

          @Override
          public void cancel() {
            throw new IllegalStateException("cancel not supported");
          }
        });
      }
    }).subscribe(pushEach((Integer item) -> count.set(item))
        .andThen(() -> {
          synchronized (count) {
            count.notify();
          }
        })
        .exceptionally(e -> {
          error.set(e);
          synchronized (count) {
            count.notify();
          }
        }));
    synchronized (count) {
      count.wait();
    }
    if (error.get() != null) {
      return throwException(error.get());
    }
    return count.get();
  }

  public Publisher<Integer> save(Publisher<Customer> customers) throws IOException {
    AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
    AtomicReference<WriteState> writeState = new AtomicReference<>(new WriteState());
    AtomicLong offset = new AtomicLong(0);
    AtomicInteger resultCount = new AtomicInteger(0);
    writeState.updateAndGet(increment());
    SingleItemPublisher<Integer> resultPublisher = new SingleItemPublisher<>();
    fileChannel.write(ByteBuffer.wrap("[".getBytes()), 0, resultPublisher,
        andThen((count, s) -> {
          writeState.updateAndGet(decrement());
          customers.subscribe(pullEach((Customer customer, Subscription subscription) -> {
              String json = String.format("%s{\"firstName\": \"%s\", \"lastName\": \"%s\"}", offset.longValue() == 0 ? "" : ",",
                  customer.getFirstName(), customer.getLastName());
              offset.addAndGet(count);
              writeState.updateAndGet(increment());
              System.out.println("write " + json + " at offset " + offset.get());
              fileChannel.write(ByteBuffer.wrap(json.getBytes()), offset.get(), resultPublisher,
                  andThen((size, c) -> {
                    WriteState state = writeState.updateAndGet(decrement());
                    offset.addAndGet(size);
                    resultCount.incrementAndGet();
                    if (state.isCompleted()) {
                      fileChannel.write(ByteBuffer.wrap("]".getBytes()), offset.get(), resultPublisher,
                          andThen((d, sub) -> {
                            try {
                              fileChannel.close();
                              resultPublisher.publish(resultCount.intValue());
                            } catch (Exception error) {
                              resultPublisher.publish(error);
                            }
                          }));
                    }
                    subscription.request(1);                  
                  }));
            }).andThen(() -> {
              if (!writeState.updateAndGet(complete()).hasOutstanding()) {
                fileChannel.write(ByteBuffer.wrap("]".getBytes()), offset.longValue(), resultPublisher,
                    andThen((d, e) -> {
                      try {
                        fileChannel.close();
                        resultPublisher.publish(resultCount.intValue());
                      } catch (IOException error) {
                        resultPublisher.publish(error);
                      }
                    }));
              }
            }).exceptionally(error -> resultPublisher.publish(error)));
        }));
    return resultPublisher;
  }

  public List<Customer> findAll() throws IOException {
    String customers = IOUtils.toString(new FileReader("customers.json")).trim();
    customers = customers.substring(1, customers.length() - 1);
    return asList(customers.split("\\{")).stream().filter(s -> !s.isEmpty()).map(c -> c.substring(0, c.lastIndexOf('}'))).map(c -> c.split(",")).map(c -> {
      String firstName = c[0].substring(c[0].indexOf(':') + 3, c[0].length() - 1);
      String lastName = c[1].substring(c[1].indexOf(':') + 3, c[1].length() - 1);
      return new Customer(firstName, lastName);
    }).collect(toList());
  }

  public static <V, A> CompletionHandler<V, SingleItemPublisher<A>> andThen(BiConsumer<V, SingleItemPublisher<A>> consumer) {
    return new CompletionHandler<V, SingleItemPublisher<A>>() {

      @Override
      public void completed(V value, SingleItemPublisher<A> publisher) {
        consumer.accept(value, publisher);
      }

      @Override
      public void failed(Throwable error, SingleItemPublisher<A> publisher) {
        publisher.publish(error);
      }
    };
  }

  private static UnaryOperator<WriteState> increment() {
    return w -> {
      return w.increment();
    };
  }

  private static UnaryOperator<WriteState> decrement() {
    return w -> {
      return w.decrement();
    };
  }

  private static UnaryOperator<WriteState> complete() {
    return w -> {
      return w.complete();
    };
  }

  private static int throwException(Throwable t) throws Exception {
    if (t instanceof Error) {
      throw (Error)t;
    } else {
      throw (Exception)t;
    }
  }

  private static class WriteState {
    private boolean completed = false;
    private int outstanding = 0;

    public boolean isCompleted() {
      return !hasOutstanding() && completed;
    }

    public boolean hasOutstanding() {
      return outstanding > 0;
    }

    public WriteState increment() {
      WriteState newState = new WriteState();
      newState.outstanding = outstanding + 1;
      newState.completed = completed;
      return newState;
    }

    public WriteState decrement() {
      WriteState newState = new WriteState();
      newState.outstanding = outstanding - 1;
      newState.completed = completed;
      return newState;
    }

    public WriteState complete() {
      WriteState newState = new WriteState();
      newState.outstanding = outstanding;
      newState.completed = true;
      return newState;
    }
  }
}
