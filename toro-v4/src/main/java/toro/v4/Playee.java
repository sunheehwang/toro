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
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.exoplayer.ExoPlayable;
import im.ene.toro.media.PlaybackInfo;

/**
 * @author eneim (2018/06/21).
 */
final class Playee implements Playable, Playback.Callback {

  @NonNull private final Toro toro;
  @NonNull private final Uri uri;
  @NonNull private final Playable.Options options;
  @NonNull private final Bundle bundle;

  private im.ene.toro.exoplayer.Playable helper;
  private im.ene.toro.exoplayer.Playable.EventListener listener;

  Playee(Toro toro, Bundle bundle) {
    this.toro = toro;
    this.bundle = bundle;
    this.uri = bundle.uri;
    this.options = bundle.options;

    ExoCreator creator = toro.toroExo.getCreator(options.config);
    this.helper = new ExoPlayable(creator, this.uri, options.mediaType);
    this.helper.setPlaybackInfo(options.playbackInfo);
    this.helper.setRepeatMode(options.repeatMode);
    this.helper.prepare(this.options.alwaysLoad);
  }

  @Override public void onAdded(final Playback playback) {
    if (this.listener == null) {
      this.listener = new im.ene.toro.exoplayer.Playable.DefaultEventListener() {
        @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
          playback.dispatchPlayerStateChanged(playWhenReady, playbackState);
        }
      };
      this.helper.addEventListener(this.listener);
    }
  }

  @Override public void onActive(final Playback playback) {
    if (playback.getTarget() instanceof PlayerView) {
      this.helper.setPlayerView((PlayerView) playback.getTarget());
    }
  }

  @Override public void onInActive(Playback playback) {
    // This will release current MediaCodec instances, which are expensive to retain.
    if (playback.manager.playablesThisActiveTo.contains(this)) {
      this.helper.setPlayerView(null);
    }
  }

  @Override public void onRemoved(Playback playback, boolean recreating) {
    playback.removeCallback(this);
    if (this.listener != null) {
      this.helper.removeEventListener(listener);
      this.listener = null;
    }
    if (recreating) return;
    Manager active = Utils.findOne(toro.managers.values(), new Utils.Predicate<Manager>() {
      @Override public boolean accept(Manager manager) {
        return manager.playablesThisActiveTo.contains(Playee.this);
      }
    });
    if (active == null) this.release();
  }

  ////

  @NonNull @Override public Playback<PlayerView> bind(@NonNull PlayerView playerView) {
    Manager manager = toro.getManager(ToroUtil.checkNotNull(playerView).getContext());
    Object oldTarget = manager.mapPlayableToTarget.put(this, playerView);
    if (oldTarget != null) {
      Playback oldPlayback = manager.mapTargetToPlayback.remove(oldTarget);
      if (oldPlayback != null) {
        manager.removePlayback(oldPlayback);
      }
    }
    Playback<PlayerView> playback = new ViewPlayback<>(this, uri, manager, playerView, options);
    playback.addCallback(this);
    if (playback.validTag()) {
      toro.playablePacks.put(playback.getTag().toString(), this);
    }
    return manager.addPlayback(playback);
  }

  @Override public void play() {
    this.helper.play();
  }

  @Override public void pause() {
    this.helper.pause();
  }

  @Override public void release() {
    this.helper.release();
    toro.playableStore.remove(this.bundle);
    if (this.options.tag != null) {
      toro.playablePacks.remove(this.options.tag.toString());
    }
  }

  @Override public void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo) {
    this.helper.setPlaybackInfo(playbackInfo);
  }

  @NonNull @Override public PlaybackInfo getPlaybackInfo() {
    return this.helper.getPlaybackInfo();
  }

  @Override
  public void addVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
    this.helper.addOnVolumeChangeListener(listener);
  }

  @Override public void removeVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener) {
    this.helper.removeOnVolumeChangeListener(listener);
  }

  //// Experiment

  @Override public void mayUpdateStatus(Manager manager, boolean active) {
    if (active) {
      for (Manager m : toro.managers.values()) {
        m.playablesThisActiveTo.remove(this);
      }
      manager.playablesThisActiveTo.add(this);
    } else {
      manager.playablesThisActiveTo.remove(this);
    }
  }
}
