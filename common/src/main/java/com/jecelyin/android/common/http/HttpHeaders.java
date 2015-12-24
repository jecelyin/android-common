/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jecelyin.android.common.http;

import com.squareup.okhttp.Headers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The header fields of a single HTTP message. Values are uninterpreted strings;
 * use {@code Request} and {@code Response} for interpreted headers. This class
 * maintains the order of the header fields within the HTTP message.
 *
 * <p>This class tracks header values line-by-line. A field with multiple comma-
 * separated values on the same line will be treated as a field with a single
 * value by this class. It is the caller's responsibility to detect and split
 * on commas if their field permits multiple values. This simplifies use of
 * single-valued fields whose values routinely contain commas, such as cookies
 * or dates.
 *
 * <p>This class trims whitespace from values. It never returns values with
 * leading or trailing whitespace.
 *
 * <p>Instances of this class are immutable. Use {@link Builder} to create
 * instances.
 */
public final class HttpHeaders {
  private final String[] namesAndValues;

  private HttpHeaders(Builder builder) {
    this.namesAndValues = builder.namesAndValues.toArray(new String[builder.namesAndValues.size()]);
  }

  /** Returns the last value corresponding to the specified field, or null. */
  public String get(String name) {
    return get(namesAndValues, name);
  }

  /** Returns the number of field values. */
  public int size() {
    return namesAndValues.length / 2;
  }

  /** Returns the field at {@code position} or null if that is out of range. */
  public String name(int index) {
    int nameIndex = index * 2;
    if (nameIndex < 0 || nameIndex >= namesAndValues.length) {
      return null;
    }
    return namesAndValues[nameIndex];
  }

  /** Returns the value at {@code index} or null if that is out of range. */
  public String value(int index) {
    int valueIndex = index * 2 + 1;
    if (valueIndex < 0 || valueIndex >= namesAndValues.length) {
      return null;
    }
    return namesAndValues[valueIndex];
  }

  /** Returns an immutable case-insensitive set of header names. */
  public Set<String> names() {
    TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    for (int i = 0, size = size(); i < size; i++) {
      result.add(name(i));
    }
    return Collections.unmodifiableSet(result);
  }

  /** Returns an immutable list of the header values for {@code name}. like: Set-Cookie: */
  public List<String> values(String name) {
    List<String> result = null;
    for (int i = 0, size = size(); i < size; i++) {
      if (name.equalsIgnoreCase(name(i))) {
        if (result == null) result = new ArrayList<>(2);
        result.add(value(i));
      }
    }
    return result != null
        ? Collections.unmodifiableList(result)
        : Collections.<String>emptyList();
  }

  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    for (int i = 0, size = size(); i < size; i++) {
      result.append(name(i)).append(": ").append(value(i)).append("\n");
    }
    return result.toString();
  }

  public Map<String, List<String>> toMap() {
    Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
    for (int i = 0, size = size(); i < size; i++) {
      String name = name(i);
      List<String> values = result.get(name);
      if (values == null) {
        values = new ArrayList<>(2);
        result.put(name, values);
      }
      values.add(value(i));
    }
    return result;
  }

  private static String get(String[] namesAndValues, String name) {
    for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
      if (name.equalsIgnoreCase(namesAndValues[i])) {
        return namesAndValues[i + 1];
      }
    }
    return null;
  }

  public static final class Builder {
    private final List<String> namesAndValues = new ArrayList<>(20);

    public Builder() {
    }

    public Builder(Headers h) {
      int size = h.size();
      for(int i=0; i<size; i++) {
        addLenient(h.name(i), h.value(i));
      }
    }

    /** Add a field with the specified value. */
    public Builder add(String name, String value) {
      checkNameAndValue(name, value);
      return addLenient(name, value);
    }

    /**
     * Add a field with the specified value without any validation. Only
     * appropriate for headers from the remote peer or cache.
     */
    Builder addLenient(String name, String value) {
      namesAndValues.add(name);
      namesAndValues.add(value.trim());
      return this;
    }

    public Builder removeAll(String name) {
      for (int i = 0; i < namesAndValues.size(); i += 2) {
        if (name.equalsIgnoreCase(namesAndValues.get(i))) {
          namesAndValues.remove(i); // name
          namesAndValues.remove(i); // value
          i -= 2;
        }
      }
      return this;
    }

    /**
     * Set a field with the specified value. If the field is not found, it is
     * added. If the field is found, the existing values are replaced.
     */
    public Builder set(String name, String value) {
      checkNameAndValue(name, value);
      removeAll(name);
      addLenient(name, value);
      return this;
    }

    private void checkNameAndValue(String name, String value) {
      if (name == null) throw new IllegalArgumentException("name == null");
      if (name.isEmpty()) throw new IllegalArgumentException("name is empty");
      for (int i = 0, length = name.length(); i < length; i++) {
        char c = name.charAt(i);
        if (c <= '\u001f' || c >= '\u007f') {
          throw new IllegalArgumentException(String.format(
              "Unexpected char %#04x at %d in header name: %s", (int) c, i, name));
        }
      }
      if (value == null) throw new IllegalArgumentException("value == null");
      for (int i = 0, length = value.length(); i < length; i++) {
        char c = value.charAt(i);
        if (c <= '\u001f' || c >= '\u007f') {
          throw new IllegalArgumentException(String.format(
              "Unexpected char %#04x at %d in header value: %s", (int) c, i, value));
        }
      }
    }

    /** Equivalent to {@code build().get(name)}, but potentially faster. */
    public String get(String name) {
      for (int i = namesAndValues.size() - 2; i >= 0; i -= 2) {
        if (name.equalsIgnoreCase(namesAndValues.get(i))) {
          return namesAndValues.get(i + 1);
        }
      }
      return null;
    }

    public HttpHeaders build() {
      return new HttpHeaders(this);
    }
  }
}
