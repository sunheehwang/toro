/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.common;

import android.net.Uri;
import java.util.ArrayList;

/**
 * @author eneim (2017/12/18).
 */

public final class MediaList extends ArrayList<Uri> {

  private final int size;

  enum URLS {
    MP4_BUNNY(Uri.parse("file:///android_asset/bbb.mp4")),  //
    MP4_TOS(Uri.parse("file:///android_asset/tos.mp4")),  //
    MP4_COSMOS(Uri.parse("file:///android_asset/cosmos.mp4"));

    final Uri uri;

    URLS(Uri uri) {
      this.uri = uri;
    }
  }

  public MediaList(int size) {
    this.size = size;
  }

  public MediaList() {
    this(Integer.MAX_VALUE);
  }

  @Override public int size() {
    return this.size;
  }

  @Override public Uri get(int index) {
    return URLS.values()[index % URLS.values().length].uri;
  }

  @Override public int indexOf(Object o) {
    if (!(o instanceof Uri)) return -1;
    Uri uri = (Uri) o;
    for (int i = 0; i < URLS.values().length; i++) {
      if (uri == URLS.values()[i].uri) return i;
    }

    return -1;
  }

  @Override public boolean add(Uri media) {
    throw new UnsupportedOperationException("Unsupported");
  }

  @Override public Uri remove(int index) {
    throw new UnsupportedOperationException("Unsupported");
  }
}
