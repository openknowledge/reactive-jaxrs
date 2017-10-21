package de.openknowledge.jaxrs.reactive.test;

import de.openknowledge.jaxrs.reactive.ServletInputStreamPublisherAdapter;
import de.openknowledge.jaxrs.reactive.ServletInputStreamPublisherAdapterTest.AnswerArg1;
import de.openknowledge.jaxrs.reactive.flow.BufferedSubscriber;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServletInputStreamPublisherAdapterTest {

  @Mock
  private ServletInputStream servletInputStreamMock;

  @InjectMocks
  private ServletInputStreamPublisherAdapter servletInputStreamPublisherAdapter;

  @Test public void whenSubscribeExpectOnSubscribeCalledOnSubscriber() throws Exception {

  }

  /**
   * TBD
   * @throws Exception
   */
  @Test public void whenDataAvailableExpectReadBytesPublishedToSubscriber() throws Exception {

    byte[] expectedValues = new byte[] {(byte)2, (byte)47};

    AnswerArg1<ReadListener> answer = new AnswerArg1<>();

    BufferedSubscriber<ByteBuffer> bufferedSubscriber = new BufferedSubscriber<>();

    // mock setup
    // receive setReadListener's argument
    Mockito
      .doAnswer(answer)
      .when(servletInputStreamMock).setReadListener(Mockito.any(ReadListener.class));

    // return one byte on ServletInputStream.read
    when(servletInputStreamMock.read(Mockito.any(byte[].class)))
      .thenAnswer(new Answer<Integer>() {
        @Override public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
          byte[] outputArray = (byte[])invocationOnMock.getArgument(0);
          System.arraycopy(expectedValues, 0, outputArray, 0, expectedValues.length);
          return expectedValues.length;
        }
      });

    when(servletInputStreamMock.isReady())
      .thenReturn(true)
      .thenReturn(false);

    servletInputStreamPublisherAdapter.startReading();
    servletInputStreamPublisherAdapter.subscribe(bufferedSubscriber);

    // inform about data availability
    answer.getArg().onDataAvailable();

    // asserts
    List<ByteBuffer> receivedByteBufferList = bufferedSubscriber.toList();
    Assert.assertThat(receivedByteBufferList.size(), CoreMatchers.equalTo(1));

    ByteBuffer byteBuffer = receivedByteBufferList.stream().findFirst().get();
    byteBuffer.flip();
    Assert.assertThat(byteBuffer.get(), CoreMatchers.equalTo((byte)2));
    Assert.assertThat(byteBuffer.get(), CoreMatchers.equalTo((byte)47));
  }

  @Test public void whenNoMoreDataAvailableOnCompletedCalled() throws Exception {

    AnswerArg1<ReadListener> answer = new AnswerArg1<>();

    BufferedSubscriber<Object> bufferedSubscriber = new BufferedSubscriber<>();

    // mock setup
    // receive setReadListener's argument
    Mockito
      .doAnswer(answer)
      .when(servletInputStreamMock).setReadListener(Mockito.any(ReadListener.class));

    // return one byte on ServletInputStream.read
    when(servletInputStreamMock.read(Mockito.any(byte[].class)))
      .thenReturn(-1);

    when(servletInputStreamMock.isReady())
      .thenReturn(true);

    servletInputStreamPublisherAdapter.startReading();
    servletInputStreamPublisherAdapter.subscribe(bufferedSubscriber);

    // inform about data availability
    answer.getArg().onDataAvailable();

    // asserts
    Assert.assertThat(bufferedSubscriber.isCompleted(), CoreMatchers.equalTo(true));
  }

  @Test public void whenIoExceptionThrownOnReadExpectOnErrorCalled() throws Exception {
    IOException expectedException = new IOException();

    AnswerArg1<ReadListener> answer = new AnswerArg1<>();

    BufferedSubscriber<Object> bufferedSubscriber = new BufferedSubscriber<>();

    // mock setup
    // receive setReadListener's argument
    Mockito
      .doAnswer(answer)
      .when(servletInputStreamMock).setReadListener(Mockito.any(ReadListener.class));

    // return one byte on ServletInputStream.read
    when(servletInputStreamMock.read(Mockito.any(byte[].class)))
      .thenThrow(expectedException);

    when(servletInputStreamMock.isReady())
      .thenReturn(true);

    servletInputStreamPublisherAdapter.startReading();
    servletInputStreamPublisherAdapter.subscribe(bufferedSubscriber);

    // inform about data availability
    answer.getArg().onDataAvailable();

    // asserts
    Assert.assertThat(bufferedSubscriber.getException(), CoreMatchers.equalTo(expectedException));
  }
}
