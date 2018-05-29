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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.Config;
import im.ene.toro.media.PlaybackInfo;

/**
 * @author eneim (2018/05/25).
 */
final class PlayableImpl implements Playable {

  private final Toro toro;
  private final Uri uri;
  private final Options options;
  private Options customOptions = null;

  /// Options

  PlayableImpl(Toro toro, Uri uri) {
    this.toro = toro;
    this.uri = uri;
    this.options = new Options();
  }

  @NonNull @Override public Playable config(@NonNull Config config) {
    this.options.config = config;
    return this;
  }

  @NonNull @Override public Playable playbackInfo(@NonNull PlaybackInfo playbackInfo) {
    this.options.playbackInfo = playbackInfo;
    return this;
  }

  @NonNull @Override public Playable mediaType(@Nullable String type) {
    this.options.mediaType = type;
    return this;
  }

  @NonNull @Override public Playable tag(@Nullable Object tag) {
    this.options.tag = tag;
    return this;
  }

  @NonNull @Override public Playable options(@NonNull Options options) {
    this.customOptions = ToroUtil.checkNotNull(options);
    this.options.tag = customOptions.tag;
    this.options.config = customOptions.config;
    this.options.mediaType = customOptions.mediaType;
    this.options.playbackInfo = customOptions.playbackInfo;
    return this;
  }

  @NonNull @Override public Playback<PlayerView> into(@NonNull PlayerView playerView) {
    if (customOptions != null) {
      options.tag = customOptions.tag;
      options.config = customOptions.config;
      options.mediaType = customOptions.mediaType;
      options.playbackInfo = customOptions.playbackInfo;
    }
    Manager manager = toro.getManager(ToroUtil.checkNotNull(playerView).getContext());
    Playback<PlayerView> playback = new PlayerViewPlayback(this, uri, manager, playerView, options);
    return manager.addPlayback(playback);
  }
}
