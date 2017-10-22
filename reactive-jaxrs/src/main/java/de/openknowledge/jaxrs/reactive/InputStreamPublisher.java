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
package de.openknowledge.jaxrs.reactive;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class InputStreamPublisher implements Publisher<byte[]> {

  private InputStream stream;
  
  public InputStreamPublisher(InputStream stream) {
    this.stream = stream;
  }

  @Override
  public void subscribe(Subscriber<? super byte[]> subscriber) {
    byte[] buffer = new byte[1024];
    subscriber.onSubscribe(new Subscription() {
      
      @Override
      public void request(long count) {
        
        try {
          for (long i = 0; i < count; i++) {
            int read = stream.read(buffer);
            byte[] item = buffer;
            if (read != buffer.length) {
              item = new byte[read];
              System.arraycopy(buffer, 0, item, 0, read);
            }
            subscriber.onNext(item);
          }
        } catch (IOException e) {
          subscriber.onError(e);
        }
      }
      
      @Override
      public void cancel() {
      }
    });
  }
}
