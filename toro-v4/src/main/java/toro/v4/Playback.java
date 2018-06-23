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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
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

  public static class Token implements Comparable<Token> {

    @Override public int compareTo(@NonNull Token o) {
      return 0;
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

  protected final Handler handler = new Handler(new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      Log.i(TAG, "handleMessage() called with: msg = [" + msg + "]");
      boolean playWhenReady = (boolean) msg.obj;
      switch (msg.what) {
        case State.STATE_IDLE:
          break;
        case State.STATE_BUFFERING /* Player.STATE_BUFFERING */:
          for (ToroPlayer.EventListener listener : listeners) {
            listener.onBuffering();
          }
          break;
        case State.STATE_READY /*  Player.STATE_READY */:
          for (ToroPlayer.EventListener listener : listeners) {
            if (playWhenReady) {
              listener.onPlaying();
            } else {
              listener.onPaused();
            }
          }
          break;
        case State.STATE_END /* Player.STATE_ENDED */:
          for (ToroPlayer.EventListener listener : listeners) {
            listener.onCompleted();
          }
          break;
        default:
          break;
      }
      return true;
    }
  });

  protected final HashSet<ToroPlayer.EventListener> listeners = new HashSet<>();
  protected final HashSet<Callback> callbacks = new HashSet<>();

  @NonNull protected final Manager manager;
  @NonNull protected final Playable playable;
  @NonNull protected final Playable.Options options;
  @NonNull protected final Uri uri;

  @NonNull private final Object tag;
  @Nullable private final WeakReference<T> target;

  protected Playback(@NonNull Playable playable, @NonNull Uri uri, @NonNull Manager manager,
      @Nullable T target, @NonNull Playable.Options options) {
    this.playable = playable;
    this.uri = uri;
    this.manager = manager;
    this.target = target == null ? null
        : new RequestWeakReference<>(this, target, manager.toro.referenceQueue);
    this.tag = (options.tag != null ? options.tag : this);
    this.options = options;
    TAG = "Toro:Playable@" + playable.hashCode();
  }

  // Used by subclasses to dispatch internal event listeners
  @SuppressWarnings("WeakerAccess") //
  protected final void dispatchPlayerStateChanged(boolean playWhenReady, @State int playbackState) {
    handler.obtainMessage(playbackState, playWhenReady).sendToTarget();
  }

  public final void addListener(@NonNull ToroPlayer.EventListener listener) {
    this.listeners.add(ToroUtil.checkNotNull(listener));
  }

  public final void removeListener(ToroPlayer.EventListener listener) {
    this.listeners.remove(listener);
  }

  public final void addCallback(@NonNull Callback callback) {
    this.callbacks.add(ToroUtil.checkNotNull(callback));
  }

  public final void removeCallback(@Nullable Callback callback) {
    this.callbacks.remove(callback);
  }

  // Return null Token will indicate that this Playback cannot start.
  // Token is comparable.
  @Nullable protected Token getToken() {
    return null;
  }

  /// internal APIs

  @Nullable public final T getTarget() {
    return target == null ? null : target.get();
  }

  @NonNull public final Object getTag() {
    return tag;
  }

  // Only playback with 'valid tag' will be cached for restoring.
  final boolean validTag() {
    return this.tag != this;
  }

  void pause() {
    if (this.manager.playablesThisActiveTo.contains(playable)) {
      playable.pause();
    }
  }

  // being added to Manager
  // the target may not be attached to View/Window.
  @CallSuper void onAdded() {
    for (Callback callback : this.callbacks) {
      callback.onAdded(this);
    }
  }

  // being removed from Manager
  @CallSuper void onRemoved(boolean recreating) {
    for (Callback callback : this.callbacks) {
      callback.onRemoved(this, recreating);
    }
    this.listeners.clear();
    this.callbacks.clear();
  }

  // ~ View is attached
  @CallSuper void onActive() {
    for (Callback callback : this.callbacks) {
      callback.onActive(this);
    }
  }

  @CallSuper void onInActive() {
    handler.removeCallbacksAndMessages(null);
    for (Callback callback : this.callbacks) {
      callback.onInActive(this);
    }
  }

  public interface Callback {

    void onAdded(Playback playback);

    void onActive(Playback playback);

    void onInActive(Playback playback);

    void onRemoved(Playback playback, boolean recreating);
  }
}
