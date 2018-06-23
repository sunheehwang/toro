/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package toro.v4;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author eneim (2018/05/26).
 */
final class Utils {
  private Utils() {
    throw new RuntimeException("");
  }

  @SuppressWarnings("SameParameterValue") //
  static void checkSize(int size, int maxSize) {
    if (size > maxSize) {
      throw new IllegalStateException("Expected up to: " + maxSize + ", have: " + size);
    }
  }

  interface Predicate<T> {
    boolean accept(T t);
  }

  @Nullable static <T> T findOne(Iterable<T> items, Predicate<T> predicate) {
    T result = null;
    for (T item : items) {
      if (predicate.accept(item)) {
        result = item;
        break;
      }
    }

    return result;
  }

  @NonNull static <T> List<T> findAll(Iterable<T> items, Predicate<T> predicate) {
    List<T> results = new ArrayList<>();
    for (T item : items) {
      if (predicate.accept(item)) {
        results.add(item);
      }
    }
    return results;
  }
}
