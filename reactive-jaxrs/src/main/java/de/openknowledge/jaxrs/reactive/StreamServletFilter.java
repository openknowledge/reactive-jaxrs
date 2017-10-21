package de.openknowledge.jaxrs.reactive;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class StreamServletFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    chain.doFilter(request, new WrappedServletResponse(response));
  }

  @Override
  public void destroy() {

  }

  class WrappedServletResponse extends HttpServletResponseWrapper {
    WrappedServletResponse(ServletResponse response) {
      // TODO cast check
      super((HttpServletResponse)response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      return new WrappedServletOutputStream(super.getOutputStream());
    }
  }

  class WrappedServletOutputStream extends ServletOutputStream {
    private boolean delayClose = false;
    private ServletOutputStream wrappedStream;

    public WrappedServletOutputStream(ServletOutputStream wrappedStream) {
      this.wrappedStream = wrappedStream;
    }

    @Override
    public void close() throws IOException {
      if (!delayClose) {
        wrappedStream.close();
      }
    }

    public void closeWrappedStream() throws IOException {
      wrappedStream.close();
    }

    @Override
    public boolean isReady() {
      return wrappedStream.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      wrappedStream.setWriteListener(writeListener);
    }

    @Override
    public void write(int b) throws IOException {
      wrappedStream.write(b);
    }

    public void delayClose() {
      this.delayClose = true;
    }
  }
}
