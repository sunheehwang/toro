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

package im.ene.toro.sample.basic;

import android.net.Uri;

/**
 * @author eneim (7/1/17).
 */

public class Content {

  private static final String MP4_BUNNY = "file:///android_asset/bbb.mp4";
  private static final String MP4_TOS = "file:///android_asset/tos.mp4";
  private static final String MP4_COSMOS = "file:///android_asset/cosmos.mp4";

  static final String[] ITEMS = { "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8", "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/TearsOfSteel.m3u8", "https://html5demos.com/assets/dizzy.mp4" };
  //static final String[] ITEMS = { MP4_TOS, MP4_BUNNY, MP4_COSMOS };

  public static class Media {
    public final int index;
    public final Uri mediaUri;

    public Media(int index, Uri mediaUri) {
      this.index = index;
      this.mediaUri = mediaUri;
    }

    static Media getItem(int index) {
      return new Media(index, Uri.parse(ITEMS[index % ITEMS.length]));
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Media)) return false;

      Media media = (Media) o;

      if (index != media.index) return false;
      return mediaUri.equals(media.mediaUri);
    }

    @Override public int hashCode() {
      int result = index;
      result = 31 * result + mediaUri.hashCode();
      return result;
    }
  }
}
