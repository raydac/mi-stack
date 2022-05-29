package com.igormaznitsa.mistack.exception;

import com.igormaznitsa.mistack.MiStack;

public class MiStackOverflowException extends MiStackRuntimeException {
  public MiStackOverflowException(final MiStack<?> source) {
    super(source, "Mi-Stack overflow error");
  }

}
