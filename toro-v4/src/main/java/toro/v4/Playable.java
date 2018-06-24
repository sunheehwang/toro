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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.exoplayer.Config;
import im.ene.toro.media.PlaybackInfo;

/**
 * Re-usable object. Once created, will live with {@link Application} lifecycle.
 *
 * Design concept: '[Playable] --> [Target --> Playback]'
 *
 * - Target can be: PlayerView, VideoView, Object?, etc.
 *
 * - Playback: manage lifecycle of Target. One Target can be managed by up to one Playback at a time.
 * The state where one Target belongs to two Playbacks can only be the time of attaching/detaching
 * Target (a.k.a Target transaction).
 *
 * - Playable: should be able to passed around without interrupting the media playback (or at least,
 * audio playback). If a Playable is played 'into' a PlayerView/VideoView/Surface/Texture, it is
 * expected to have Video playback being interrupted when switching Target. Also, a single Playable
 * can be played 'into' different types of Target. In that case, implementation of Playable must
 * guarantee the consistency of Playable's PlaybackInfo.
 *
 * [Added 2018/06/19]
 * ...
 * [Removed 2018/06/22]
 *
 * @author eneim (2018/05/25).
 * @since 4.0.0.2800
 */
public interface Playable {

  final class Bundle {
    @NonNull final Uri uri;
    @NonNull final Options options;

    Bundle(@NonNull Uri uri, @NonNull Options options) {
      this.uri = uri;
      this.options = options;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Bundle bundle = (Bundle) o;

      if (!uri.equals(bundle.uri)) return false;
      return options.equals(bundle.options);
    }

    @Override public int hashCode() {
      int result = uri.hashCode();
      result = 31 * result + options.hashCode();
      return result;
    }
  }

  final class Options {
    final void copyFrom(@NonNull Options options) {
      this.config = options.config;
      this.playbackInfo = options.playbackInfo;
      this.mediaType = options.mediaType;
      this.tag = options.tag;
      this.repeatMode = options.repeatMode;
    }

    @NonNull Config config = Config.DEFAULT;
    @NonNull PlaybackInfo playbackInfo = PlaybackInfo.SCRAP;
    @Nullable String mediaType = null;
    @Nullable Object tag = null;
    boolean alwaysLoad = false;
    @Player.RepeatMode int repeatMode = Player.REPEAT_MODE_OFF;

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

  final class Builder {

    @NonNull final Toro toro;
    @NonNull final Uri uri;
    @NonNull final Options options = new Options();

    public Builder(@NonNull Toro toro, @NonNull Uri uri) {
      this.toro = toro;
      this.uri = uri;
    }

    public Builder config(@NonNull Config config) {
      this.options.config = config;
      return this;
    }

    public Builder playbackInfo(@NonNull PlaybackInfo playbackInfo) {
      this.options.playbackInfo = playbackInfo;
      return this;
    }

    public Builder tag(@NonNull Object tag) {
      this.options.tag = tag;
      return this;
    }

    public Builder repeatMode(@Player.RepeatMode int repeatMode) {
      this.options.repeatMode = repeatMode;
      return this;
    }

    public Builder mediaType(@Nullable String type) {
      this.options.mediaType = type;
      return this;
    }

    public Builder alwaysLoad(boolean alwaysLoad) {
      this.options.alwaysLoad = alwaysLoad;
      return this;
    }

    public Builder options(@NonNull Options options) {
      this.options.copyFrom(options);
      return this;
    }

    public Playable asPlayable() {
      return this.toro.getPlayable(new Bundle(this.uri, this.options));
    }
  }

  /**
   * Create a {@link Playback} object to manages a {@link PlayerView}.
   *
   * @param playerView the {@link PlayerView} to play the content.
   */
  @NonNull Playback<PlayerView> bind(@NonNull PlayerView playerView);

  /// Playback controller

  void play();

  void pause();

  void release();

  /// TODO consider if we need these methods
  void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo);

  @NonNull PlaybackInfo getPlaybackInfo();

  void addVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener);

  void removeVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener);

  //// Experiment

  // TODO [20180622] Should be hidden to User. Consider to make Playable abstract class
  void mayUpdateStatus(Manager manager, boolean active);
}
