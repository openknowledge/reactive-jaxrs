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
import java.io.OutputStream;

/**
 * Copy of Resteasy Util Library
 *
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version 3.0.19.Final
 */
public class DelegatingOutputStream extends OutputStream
{
  protected OutputStream delegate;

  public DelegatingOutputStream(OutputStream delegate)
  {
    this.delegate = delegate;
  }

  public OutputStream getDelegate()
  {
    return delegate;
  }

  public void setDelegate(OutputStream delegate)
  {
    this.delegate = delegate;
  }

  @Override
  public void write(int i) throws IOException
  {
    getDelegate().write(i);
  }

  @Override
  public void write(byte[] bytes) throws IOException
  {
    getDelegate().write(bytes);
  }

  @Override
  public void write(byte[] bytes, int i, int i1) throws IOException
  {
    getDelegate().write(bytes, i, i1);
  }

  @Override
  public void flush() throws IOException
  {
    getDelegate().flush();
  }

  @Override
  public void close() throws IOException
  {
    getDelegate().close();
  }
}
