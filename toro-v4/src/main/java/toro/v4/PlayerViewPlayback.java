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
import android.util.Log;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.exoplayer.ExoPlayable;
import im.ene.toro.exoplayer.MediaSourceBuilder;
import im.ene.toro.media.PlaybackInfo;

/**
 * {@link Playback} whose target is a {@link PlayerView}.
 *
 * @author eneim (2018/05/25).
 * @since 4.0.0.2800
 */
class PlayerViewPlayback extends ViewPlayback<PlayerView> {

  @SuppressWarnings({ "FieldCanBeLocal", "unused" })  //
  private final im.ene.toro.exoplayer.Playable player;
  private im.ene.toro.exoplayer.Playable.EventListener listener;

  PlayerViewPlayback(Playable playable, @NonNull Uri uri, @NonNull Manager manager,
      @NonNull PlayerView target, @NonNull Playable.Options options) {
    super(playable, uri, manager, target, options);
    ExoCreator creator = manager.toro.toroExo.getCreator(
        options.config.newBuilder().setMediaSourceBuilder(MediaSourceBuilder.LOOPING).build());
    this.player = new ExoPlayable(creator, uri, options.mediaType);
    this.player.setPlaybackInfo(options.playbackInfo);
  }

  @Override protected void prepare(boolean prepareSource) {
    Log.i(TAG, "prepare() called with: prepareSource = [" + prepareSource + "]");
    player.setPlayerView(getTarget());
    player.prepare(prepareSource);
    if (listener == null) {
      listener = new im.ene.toro.exoplayer.Playable.DefaultEventListener() {
        @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
          super.onPlayerStateChanged(playWhenReady, playbackState);
          PlayerViewPlayback.super.dispatchPlayerStateChanged(playWhenReady, playbackState);
        }
      };
      player.addEventListener(listener);
    }
  }

  @Override public void play() {
    Log.i(TAG, "play() called");
    player.play();
  }

  @Override public void pause() {
    Log.i(TAG, "pause() called");
    player.pause();
  }

  @Override public void release() {
    Log.i(TAG, "release() called");
    if (listener != null) {
      player.removeEventListener(listener);
      listener = null;
    }
    player.release();
    // Call after player.release() so that the last messages are also delivered.
    super.release();
  }

  @Override public boolean isPlaying() {
    Log.i(TAG, "isPlaying() called");
    return player.isPlaying();
  }

  @Override void setPlaybackInfo(PlaybackInfo playbackInfo) {
    Log.i(TAG, "setPlaybackInfo() called with: playbackInfo = [" + playbackInfo + "]");
    player.setPlaybackInfo(playbackInfo);
  }

  @Override PlaybackInfo getPlaybackInfo() {
    Log.i(TAG, "getPlaybackInfo() called");
    return player.getPlaybackInfo();
  }

  @Override void onAdded() {
    Log.i(TAG, "onAdded() called");
    super.onAdded();
  }

  @Override void onRemoved() {
    Log.i(TAG, "onRemoved() called");
    super.onRemoved();
  }

  @Override void onActive() {
    Log.i(TAG, "onActive() called");
    super.onActive();
  }

  @Override void onInActive() {
    Log.i(TAG, "onInActive() called");
    super.onInActive();
  }
}
