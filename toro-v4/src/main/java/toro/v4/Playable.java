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

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.exoplayer.Config;
import im.ene.toro.media.PlaybackInfo;

/**
 * Re-usable object. Once created, will live with {@link Application} lifetime.
 *
 * @author eneim (2018/05/25).
 * @since 4.0.0.2800
 */
public interface Playable {

  final class Options {
    @NonNull Config config = new Config.Builder().build();
    @NonNull PlaybackInfo playbackInfo = PlaybackInfo.SCRAP;
    @Nullable String mediaType = null;
    @Nullable Object tag = null;

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Options options = (Options) o;
      if (!config.equals(options.config)) return false;
      if (!playbackInfo.equals(options.playbackInfo)) return false;
      if (mediaType != null ? !mediaType.equals(options.mediaType) : options.mediaType != null) {
        return false;
      }
      return tag != null ? tag.equals(options.tag) : options.tag == null;
    }

    @Override public int hashCode() {
      int result = config.hashCode();
      result = 31 * result + playbackInfo.hashCode();
      result = 31 * result + (mediaType != null ? mediaType.hashCode() : 0);
      result = 31 * result + (tag != null ? tag.hashCode() : 0);
      return result;
    }
  }

  @NonNull Playable config(@NonNull Config config);

  // Init the Playback with a pre-defined PlaybackInfo
  @NonNull Playable playbackInfo(@NonNull PlaybackInfo playbackInfo);

  // Custom media type (file extension, etc)
  @NonNull Playable mediaType(@Nullable String type);

  // Non-null tag will ask Manager to save this Playable's state.
  @NonNull Playable tag(@Nullable Object tag);

  // Setting this will always override other options, regardless of the order.
  // This method can be called many times. The last call wins.
  @NonNull Playable options(@NonNull Options options);

  /**
   * Create a {@link Playback} object so that it can be played by a {@link PlayerView}.
   *
   * @param playerView the {@link PlayerView} to play the content.
   * @return the {@link Playback} object to control the playback.
   */
  @NonNull Playback<PlayerView> into(@NonNull PlayerView playerView);
}
