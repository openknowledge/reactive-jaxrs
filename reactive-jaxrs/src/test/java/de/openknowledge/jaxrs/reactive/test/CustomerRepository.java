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
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.IOUtils;

import de.openknowledge.jaxrs.reactive.flow.SingleItemPublisher;
import de.openknowledge.reactive.AbstractSimpleProcessor;
import de.openknowledge.reactive.charset.DecodingProcessor;
import de.openknowledge.reactive.charset.EncodingProcessor;
import de.openknowledge.reactive.commons.io.AsynchronousFileChannelPublisher;
import de.openknowledge.reactive.commons.io.AsynchronousFileChannelSubscriber;
import de.openknowledge.reactive.json.JsonArrayProcessor;
import de.openknowledge.reactive.json.JsonTokenizer;

@ApplicationScoped
public class CustomerRepository {

  private Path path;

  @PostConstruct
  public void initialize() {
    try {
      path = Paths.get("target/customers.json");
      if (!Files.exists(path)) {
        Files.createFile(path);
      }
      Files.write(path, "[]".getBytes());
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
        // semaphore to prevent calling onComplete multiple times from different threads
        Semaphore completionSemaphore = new Semaphore(1);
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
              completionSemaphore.acquireUninterruptibly();
              if (subscriber != null) {
                subscriber.onComplete();
                subscriber = null;
                completionSemaphore.release();
              }
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
    CustomerWriter writer = new CustomerWriter();
    Processor<String, CharBuffer> converter = new AbstractSimpleProcessor<String, CharBuffer>() {
      private boolean first = true;
      @Override
      public void onNext(String item) {
        if (first) {
          publish(CharBuffer.wrap(("[" + item).toCharArray()));
          first = false;
        } else {
          publish(CharBuffer.wrap(("," + item).toCharArray()));
        }
      }

      public void onComplete() {
        // TODO publish without request
        publish(CharBuffer.wrap("]".toCharArray()));
        super.onComplete();
      }
    };
    EncodingProcessor encoder = new EncodingProcessor(StandardCharsets.UTF_8, 4096);
    SingleItemPublisher<Integer> resultPublisher = new SingleItemPublisher<>();
    AsynchronousFileChannelSubscriber fileWriter = new AsynchronousFileChannelSubscriber(fileChannel) {
      private AtomicInteger count = new AtomicInteger();
      public void onNext(ByteBuffer buffer) {
        count.addAndGet(buffer.remaining());
        super.onNext(buffer);
      }
      public void onComplete() {
        super.onComplete();
        resultPublisher.publish(count.get());
      }
    };
    customers.subscribe(writer);
    writer.subscribe(converter);
    converter.subscribe(encoder);
    encoder.subscribe(fileWriter);
    return resultPublisher;
  }

  public List<Customer> findAll() throws IOException {
    String customers = IOUtils.toString(new FileReader("target/customers.json")).trim();
    customers = customers.substring(1, customers.length() - 1);
    return asList(customers.split("\\{")).stream().filter(s -> !s.isEmpty()).map(c -> c.substring(0, c.lastIndexOf('}'))).map(c -> c.split(",")).map(c -> {
      String firstName = c[0].substring(c[0].indexOf(':') + 3, c[0].length() - 1);
      String lastName = c[1].substring(c[1].indexOf(':') + 3, c[1].length() - 1);
      return new Customer(firstName, lastName);
    }).collect(toList());
  }

  public Publisher<Customer> findAllAsync() throws IOException {
    AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
    AsynchronousFileChannelPublisher filePublisher = new AsynchronousFileChannelPublisher(fileChannel, 4096);
    DecodingProcessor decoder = new DecodingProcessor(Charset.defaultCharset(), 4096);
    JsonTokenizer tokenizer = new JsonTokenizer();
    JsonArrayProcessor arrayProcessor = new JsonArrayProcessor();
    CustomerReader customerProcessor = new CustomerReader();
    filePublisher.subscribe(decoder);
    decoder.subscribe(tokenizer);
    tokenizer.subscribe(arrayProcessor);
    arrayProcessor.subscribe(customerProcessor);
    return customerProcessor;
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

  private static int throwException(Throwable t) throws Exception {
    if (t instanceof Error) {
      throw (Error)t;
    } else {
      throw (Exception)t;
    }
  }
}
