package de.openknowledge;

import java.util.concurrent.Flow;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.tck.SubscriberWhiteboxVerification;
import org.reactivestreams.tck.SubscriberWhiteboxVerification.WhiteboxSubscriberProbe;

import de.openknowledge.reactive.commons.AbstractSimpleProcessor;

/**
 * @author Christian Schulz - open knowledge GmbH
 */
public class DelegatingProbeProcessor<T, R> extends AbstractSimpleProcessor<T, R> {
  private final AbstractSimpleProcessor<T, R> processor;
  private final Flow.Subscriber<R> subscriber;
  private final WhiteboxSubscriberProbe<T> probe;

  public DelegatingProbeProcessor(AbstractSimpleProcessor<T, R> delegated, Flow.Subscriber<R> subscriber, WhiteboxSubscriberProbe<T> probe) {
    this.processor = delegated;
    this.subscriber = subscriber;
    this.probe = probe;
  }

  @Override
  public void onSubscribe(Flow.Subscription s) {
    processor.onSubscribe(s);

    // register a successful Subscription, and create a Puppet,
    // for the WhiteboxVerification to be able to drive its tests:
    probe.registerOnSubscribe(new SubscriberWhiteboxVerification.SubscriberPuppet() {

      @Override
      public void triggerRequest(long elements) {
        s.request(elements);
      }

      @Override
      public void signalCancel() {
        s.cancel();
      }
    });
    processor.subscribe(subscriber);
  }

  @Override
  public void onNext(T element) {
    // in addition to normal Subscriber work that you're testing, register onNext with the probe
    processor.onNext(element);
    probe.registerOnNext(element);
  }

  @Override
  public void onError(Throwable cause) {
    // in addition to normal Subscriber work that you're testing, register onError with the probe
    processor.onError(cause);
    probe.registerOnError(cause);
  }

  @Override
  public void onComplete() {
    // in addition to normal Subscriber work that you're testing, register onComplete with the probe
    processor.onComplete();
    probe.registerOnComplete();
  }

  public static interface VoidAnswer extends Answer<Void> {
    default Void answer(InvocationOnMock invocation) throws Throwable {
      execute(invocation);
      return null;
    }

    void execute(InvocationOnMock invocation) throws Throwable;
  }
}
