package com.igormaznitsa.mistack.exception;

import com.igormaznitsa.mistack.MiStack;

public class MiStackRuntimeException extends RuntimeException {
  private final MiStack<?> source;

  public MiStackRuntimeException(final MiStack<?> source) {
    super();
    this.source = source;
  }

  public MiStackRuntimeException(final MiStack<?> source, final String message) {
    super(message);
    this.source = source;
  }

  public MiStackRuntimeException(final MiStack<?> source, final String message,
                                 final Throwable cause) {
    super(message, cause);
    this.source = source;
  }

  public MiStack<?> getSource() {
    return this.source;
  }
}
