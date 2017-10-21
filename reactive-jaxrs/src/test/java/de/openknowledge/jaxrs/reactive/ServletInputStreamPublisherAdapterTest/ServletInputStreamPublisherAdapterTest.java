package de.openknowledge.jaxrs.reactive.ServletInputStreamPublisherAdapterTest;

import de.openknowledge.jaxrs.reactive.AnswerArg1;
import de.openknowledge.jaxrs.reactive.ServletInputStreamPublisherAdapter;
import de.openknowledge.jaxrs.reactive.flow.BufferedSubscriber;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(org.junit.runners.JUnit4.class)
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

    AnswerArg1<ReadListener> answer = new AnswerArg1<>();

    BufferedSubscriber<Byte> bufferedSubscriber = new BufferedSubscriber<>();

    servletInputStreamPublisherAdapter.subscribe(bufferedSubscriber);

    // receive setReadListener's argument
    Mockito.doNothing().doAnswer(answer).when(servletInputStreamMock).setReadListener(Mockito.any(ReadListener.class));

    // return one byte on ServletInputStream.read
    when(servletInputStreamMock.read()).thenReturn(255);

    // inform about data availability
    answer.getArg().onDataAvailable();

    // asserts

  }
}
