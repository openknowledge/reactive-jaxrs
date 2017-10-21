package de.openknowledge.jaxrs.reactive.test;

import static java.util.Arrays.asList;

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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.IOUtils;

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

  public void save(List<Customer> customers) throws IOException {
    save(new Publisher<Customer>() {

      private Iterator<Customer> customerIterator = customers.iterator();

      @Override
      public void subscribe(Subscriber<? super Customer> subscriber) {
        subscriber.onSubscribe(new Subscription() {

          @Override
          public void request(long count) {
            for (int i = 0; i < count; i++) {
              if (customerIterator.hasNext()) {
                subscriber.onNext(customerIterator.next());
              }
            }
            if (!customerIterator.hasNext()) {
              subscriber.onComplete();
            }
          }

          @Override
          public void cancel() {
            throw new IllegalStateException("cancel not supported");
          }
        });
      }
    });

  }

  public void save(Publisher<Customer> customers) throws IOException {
    AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);

    fileChannel.write(ByteBuffer.wrap("[".getBytes()), 0, null, new CompletionHandler<Integer, ByteBuffer>() {

      @Override
      public void completed(Integer result, ByteBuffer attachment) {
        customers.subscribe(new Subscriber<Customer>() {

          private Subscription subscription;
          private boolean first = true;
          private int offset = result;

          @Override
          public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
          }

          @Override
          public void onNext(Customer customer) {

            String json = String.format("%s{\"firstName\": \"%s\", \"lastName\": \"%s\"}", first ? "" : ",",
                customer.getFirstName(), customer.getLastName());
            first = false;
            fileChannel.write(ByteBuffer.wrap(json.getBytes()), offset, null,
                new CompletionHandler<Integer, ByteBuffer>() {

                  @Override
                  public void completed(Integer result, ByteBuffer attachment) {
                    offset += result;
                    subscription.request(1);
                  }

                  @Override
                  public void failed(Throwable t, ByteBuffer attachment) {
                    t.printStackTrace();
                  }
                });
          }

          @Override
          public void onComplete() {
            fileChannel.write(ByteBuffer.wrap("]".getBytes()), offset, null,
                new CompletionHandler<Integer, ByteBuffer>() {

                  @Override
                  public void completed(Integer offset, ByteBuffer buffer) {
                    try {
                      fileChannel.close();
                    } catch (IOException e) {
                      // ignore
                    }
                  }

                  @Override
                  public void failed(Throwable t, ByteBuffer buffer) {
                    t.printStackTrace();
                  }
                });
          }

          @Override
          public void onError(Throwable t) {
            t.printStackTrace();
          }
        });
      }

      @Override
      public void failed(Throwable t, ByteBuffer attachment) {
        t.printStackTrace();
      }
    });
  }

  public List<Customer> findAll() throws IOException {
    String customers = IOUtils.toString(new FileReader("customers.json")).trim();
    customers = customers.substring(1, customers.length() - 2);
    List<Customer> result = new ArrayList<Customer>();
    asList(customers.split("{")).stream().map(c -> c.substring(0, c.lastIndexOf('}'))).map(c -> c.split(",")).map(c -> {
      String firstName = c[0].substring(c[0].indexOf(':') + 1, c[0].length() - 1);
      String lastName = c[1].substring(c[1].indexOf(':') + 1, c[1].length() - 1);
      return new Customer(0, firstName, lastName);
    });
    return result;
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  private static class Customers {
    private List<Customer> customers;
  }
}
