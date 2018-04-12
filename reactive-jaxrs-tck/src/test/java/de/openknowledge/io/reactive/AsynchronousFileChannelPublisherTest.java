package de.openknowledge.io.reactive;

import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Flow.Publisher;

import org.apache.commons.io.FileUtils;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.FlowPublisherVerification;

public class AsynchronousFileChannelPublisherTest extends FlowPublisherVerification<ByteBuffer> {

  private static final File FILE = new File("target/input.txt");

  public AsynchronousFileChannelPublisherTest() {
    super(new TestEnvironment());
  }

  @Override
  public Publisher<ByteBuffer> createFlowPublisher(long elements) {
    AsynchronousFileChannelPublisher publisher = null;
    try {
      FileUtils.deleteQuietly(FILE);
      while (elements > Integer.MAX_VALUE / 2L) {
        FileUtils.writeByteArrayToFile(FILE, new byte[Integer.MAX_VALUE / 2], true);
        elements -= Integer.MAX_VALUE / 2L;
      }
      FileUtils.writeByteArrayToFile(FILE, new byte[(int)elements], true);
      AsynchronousFileChannel channel = AsynchronousFileChannel.open(FILE.toPath(), StandardOpenOption.READ);
      publisher = new AsynchronousFileChannelPublisher(channel, 1);
    } catch (IOException e) {
      fail(e.getMessage(), e);
    }
    return publisher;
  }

  @Override
  public Publisher<ByteBuffer> createFailedFlowPublisher() {
    return null;
  }
}
