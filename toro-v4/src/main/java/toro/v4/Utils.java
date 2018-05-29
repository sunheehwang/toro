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

import java.util.Comparator;

/**
 * @author eneim (2018/05/26).
 */
final class Utils {
  private Utils() {
    throw new RuntimeException("");
  }

  static final Comparator<Playback.LocToken> CENTER_Y = new Comparator<Playback.LocToken>() {
    @Override public int compare(Playback.LocToken o1, Playback.LocToken o2) {
      return Float.compare(o1.centerY, o2.centerY);
    }
  };

  @SuppressWarnings("SameParameterValue") //
  static void checkSize(int size, int maxSize) {
    if (size > maxSize) {
      throw new IllegalStateException("Expected up to: " + maxSize + ", have: " + size);
    }
  }
}
