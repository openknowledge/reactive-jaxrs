package de.openknowledge.io.reactive;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.atomic.AtomicLong;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;

import de.openknowledge.reactive.commons.io.AsynchronousFileChannelPublisher;

public class AsynchronousFileChannelPublisherTest extends FlowPublisherVerification<ByteBuffer> {

  private ExecutorService executorService = Executors.newFixedThreadPool(1);

  public AsynchronousFileChannelPublisherTest() {
    super(new TestEnvironment(200));
  }

  public boolean skipStochasticTests() {
    return true;
  }

  @Override
  public Publisher<ByteBuffer> createFlowPublisher(long elements) {
    AsynchronousFileChannel channel = mock(AsynchronousFileChannel.class);
    doAnswer(new AsynchronousReadAnswer(elements)).when(channel).read(any(), anyLong(), any(), any());
    return new AsynchronousFileChannelPublisher(channel, 1);
  }

  @Override
  public Publisher<ByteBuffer> createFailedFlowPublisher() {
    return null;
  }

  private class AsynchronousReadAnswer implements Answer<Void> {

    private AtomicLong remainingElements = new AtomicLong();

    public AsynchronousReadAnswer(long elements) {
      remainingElements.set(elements);
    }

    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      long elements = remainingElements.getAndDecrement();
      CompletionHandler<Integer, Long> handler = invocation.getArgument(3);
      if (elements == 0) {
        executorService.execute(() -> handler.completed(-1, invocation.getArgument(2)));
      } else {
        executorService.execute(() -> handler.completed(1, invocation.getArgument(2)));
      }
      return null;
    }
  }
}
