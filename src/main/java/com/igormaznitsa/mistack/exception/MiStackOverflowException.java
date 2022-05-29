package com.igormaznitsa.mistack.exception;

import com.igormaznitsa.mistack.MiStack;

/**
 * Exception thrown if Mi-Stack can't take one more element.
 *
 * @since 1.0.0
 */
public class MiStackOverflowException extends MiStackRuntimeException {
  public MiStackOverflowException(MiStack<?> source) {
    this(source, null);
  }

  public MiStackOverflowException(MiStack<?> source, String message) {
    this(source, message, null);
  }

  public MiStackOverflowException(MiStack<?> source, String message,
                                  Throwable cause) {
    super(source, message, cause);
  }
}
