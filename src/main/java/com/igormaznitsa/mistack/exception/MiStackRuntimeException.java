/*
 * Copyright 2022 Igor Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mistack.exception;

import com.igormaznitsa.mistack.MiStack;

/**
 * Abstract exception thrown in Mi-Stack operations.
 *
 * @since 1.0.0
 */
public abstract class MiStackRuntimeException extends RuntimeException {
  private final MiStack<?, ?> source;

  /**
   * Constructor to provide only source mi-stack.
   *
   * @param source source mi-stack, can be null.
   * @since 1.0.0
   */
  public MiStackRuntimeException(final MiStack<?, ?> source) {
    this(source, null);
  }

  /**
   * Constructor to provide only source mi-stack and message.
   *
   * @param source  source mi-stack, can be null.
   * @param message message describes error, can be null.
   * @since 1.0.0
   */
  public MiStackRuntimeException(final MiStack<?, ?> source, final String message) {
    this(source, message, null);
  }

  /**
   * Constructor to provide source mi-stack, message and root cause.
   *
   * @param source  source mi-stack, can be null.
   * @param message message describes error, can be null.
   * @param cause   root cause of the exception, can be null
   * @since 1.0.0
   */
  public MiStackRuntimeException(final MiStack<?, ?> source, final String message,
                                 final Throwable cause) {
    super(message, cause);
    this.source = source;
  }

  /**
   * Get source mi-stack for the exception.
   *
   * @return the source mi-stack, can be null.
   * @since 1.0.0
   */
  public MiStack<?, ?> getSource() {
    return this.source;
  }
}
