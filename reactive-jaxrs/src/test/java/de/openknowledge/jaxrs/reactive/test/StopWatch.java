package de.openknowledge.jaxrs.reactive.test;

import java.time.Duration;
import java.time.Instant;

public class StopWatch {

  interface MyRunnable {
    void run () throws Exception;
  }

  public static Duration time(MyRunnable runnable) throws Exception {
    Instant start = Instant.now();
    runnable.run();
    Instant end = Instant.now();
    return Duration.between(start, end);
  }

}
