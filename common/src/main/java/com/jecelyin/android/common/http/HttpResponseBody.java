/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jecelyin.android.common.http;

import java.io.IOException;

public class HttpResponseBody {
  private final byte[] bytes;

  HttpResponseBody(byte[] bytes) {
    this.bytes = bytes;
  }

  public final byte[] bytes() {
    return bytes;
  }

  /**
   * Returns the response as a string decoded with the charset of the
   * Content-Type header. If that header is either absent or lacks a charset,
   * this will attempt to decode the response body as UTF-8.
   */
  public final String string() throws IOException {
    return new String(bytes(), "UTF-8");
  }

}
