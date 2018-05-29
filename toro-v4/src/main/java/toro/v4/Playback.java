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
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.media.PlaybackInfo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;

/**
 * Instance of this class will be tight to a Target. And that target is not reusable, so instance
 * of this class must not be passed around out of Activity's scope.
 *
 * @author eneim (2018/05/25).
 */
@SuppressWarnings("WeakerAccess") //
public abstract class Playback<T> {

  protected String TAG;

  // Location on screen, with visible offset within target's parent.
  // Would be null if the Target is not a View.
  static class LocToken {
    final float centerX;
    final float centerY;
    final float areaOffset;

    LocToken(float centerX, float centerY, float areaOffset) {
      this.centerX = centerX;
      this.centerY = centerY;
      this.areaOffset = areaOffset;
    }
  }

  static class RequestWeakReference<M> extends WeakReference<M> {
    final Playback playback;

    RequestWeakReference(Playback playback, M referent, ReferenceQueue<? super M> q) {
      super(referent, q);
      this.playback = playback;
    }
  }

  @Retention(RetentionPolicy.SOURCE)  //
  @IntDef({ State.STATE_IDLE, State.STATE_BUFFERING, State.STATE_READY, State.STATE_END })  //
  public @interface State {
    int STATE_IDLE = 1;
    int STATE_BUFFERING = 2;
    int STATE_READY = 3;
    int STATE_END = 4;
  }

  protected final HashSet<ToroPlayer.EventListener> listeners = new HashSet<>();
  protected final HashSet<ToroPlayer.OnVolumeChangeListener> volumeListener = new HashSet<>();

  @NonNull protected final Manager manager;
  @NonNull protected final Uri uri;
  @NonNull protected final Playable.Options options;

  @NonNull private final Object tag;
  @Nullable private final WeakReference<T> target;

  protected Playback(@NonNull Uri uri, @NonNull Manager manager, @Nullable T target,
      @NonNull Playable.Options options) {
    this.uri = uri;
    this.manager = manager;
    this.target = target == null ? null
        : new RequestWeakReference<>(this, target, manager.toro.referenceQueue);
    this.tag = (options.tag != null ? options.tag : this);
    this.options = options;
    TAG = "Toro:Playback@" + hashCode();
  }

  @Nullable final T getTarget() {
    return target == null ? null : target.get();
  }

  @NonNull final Object getTag() {
    return tag;
  }

  // Only playback with 'valid tag' will be cached for restoring.
  final boolean validTag() {
    return this.tag != this;
  }

  public final void addListener(@NonNull ToroPlayer.EventListener listener) {
    this.listeners.add(ToroUtil.checkNotNull(listener));
  }

  public final void removeListener(ToroPlayer.EventListener listener) {
    this.listeners.remove(listener);
  }

  public final void addVolumeChangeListener(@NonNull ToroPlayer.OnVolumeChangeListener listener) {
    this.volumeListener.add(ToroUtil.checkNotNull(listener));
  }

  public final void removeVolumeChangeListener(ToroPlayer.OnVolumeChangeListener listener) {
    this.volumeListener.remove(listener);
  }

  @SuppressWarnings("SameParameterValue") //
  abstract void prepare(boolean prepareSource);

  abstract void play();

  abstract void pause();

  // TODO FIXME clarify when to call this.
  abstract void release();

  abstract boolean isPlaying();

  // Return null LocToken will indicate that this Playback cannot start a playback.
  @Nullable abstract LocToken getLocToken();

  abstract void setPlaybackInfo(PlaybackInfo playbackInfo);

  abstract PlaybackInfo getPlaybackInfo();

  /// internal APIs

  // being added to Manager
  // the target may not be attached to View/Window.
  @CallSuper void onAdded() {
    // No ops
  }

  // being removed from Manager
  @CallSuper void onRemoved() {
    // No ops
  }

  // ~ View is attached
  @CallSuper void onActive() {
    // No ops
  }

  @CallSuper void onInActive() {
    // No ops
  }
}
