package edu.wisc.regfixer.diagnostic;

import java.io.IOException;
import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
  public void write (final int i) throws IOException {}
  public void write (final byte[] bytes) throws IOException {}
  public void write (final byte[] bytes, final int off, final int len) throws IOException {}
}
