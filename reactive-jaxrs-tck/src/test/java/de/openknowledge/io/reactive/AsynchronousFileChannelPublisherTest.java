package de.openknowledge.io.reactive;

import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
    super(new TestEnvironment(1000, 1000));
  }

  public boolean skipStochasticTests() {
	return true;
  }

  @Override
  public Publisher<ByteBuffer> createFlowPublisher(long elements) {
    AsynchronousFileChannelPublisher publisher = null;
    try {
      FileUtils.deleteQuietly(FILE);
      RandomAccessFile file = new RandomAccessFile(FILE, "rw");
      file.setLength(elements);
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
