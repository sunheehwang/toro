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

import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import im.ene.toro.ToroPlayer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author eneim (2018/05/25).
 */
abstract class ViewPlayback<V extends View> extends Playback<V>
    implements View.OnAttachStateChangeListener, View.OnLayoutChangeListener {

  // Find a CoordinatorLayout parent of View, which doesn't reach 'root' View.
  @Nullable private static View findSuitableParent(@NonNull View root, @Nullable View view) {
    do {
      if (view != null && view.getParent() instanceof CoordinatorLayout) {
        return view;
      } else if (view == root) {
        return null;
      }

      if (view != null) {
        // Else, we will loop and crawl up the view hierarchy and try to find a parent
        final ViewParent parent = view.getParent();
        view = parent instanceof View ? (View) parent : null;
      }
    } while (view != null);

    return null;
  }

  private static boolean layoutChanged(int left, int top, int right, int bottom, int oldLeft,
      int oldTop, int oldRight, int oldBottom) {
    return top != oldTop || bottom != oldBottom || left != oldLeft || right != oldRight;
  }

  private final Handler handler = new Handler(new Handler.Callback() {
    @Override public boolean handleMessage(Message msg) {
      Log.w(TAG, "handleMessage() called with: msg = [" + msg + "]");
      boolean playWhenReady = (boolean) msg.obj;
      switch (msg.what) {
        case State.STATE_IDLE:
          // TODO do something!?
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

  @SuppressWarnings("WeakerAccess") //
  final ToroPlayer.EventListener listener = new ToroPlayer.EventListener() {
    @Override public void onBuffering() {
      Log.w(TAG, "onBuffering() called");
      // do nothing
    }

    @Override public void onPlaying() {
      Log.w(TAG, "onPlaying() called");
      // do nothing
      View target = getTarget();
      if (target != null) target.setKeepScreenOn(true);
    }

    @Override public void onPaused() {
      Log.w(TAG, "onPaused() called");
      // do nothing
    }

    @Override public void onCompleted() {
      Log.w(TAG, "onCompleted() called");
      View target = getTarget();
      if (target != null) target.setKeepScreenOn(false);
    }
  };

  protected final Playable playable;  // TODO clarify how to use this
  @SuppressWarnings("WeakerAccess") @NonNull  //
  protected final AtomicBoolean targetAttached = new AtomicBoolean(false);

  ViewPlayback(@NonNull Playable playable, @NonNull Uri uri, @NonNull Manager manager, @Nullable V target,
      @NonNull Playable.Options options) {
    super(uri, manager, target, options);
    this.playable = playable;
  }

  // Used by subclasses to dispatch internal event listeners
  @SuppressWarnings("WeakerAccess") //
  protected final void dispatchPlayerStateChanged(boolean playWhenReady, @State int playbackState) {
    handler.obtainMessage(playbackState, playWhenReady).sendToTarget();
  }

  @CallSuper @Override void onAdded() {
    super.onAdded();
    V target = getTarget();
    if (target != null) {
      if (ViewCompat.isAttachedToWindow(target)) {
        this.onViewAttachedToWindow(target);
      }
      target.addOnAttachStateChangeListener(this);
    }
  }

  @Override void onRemoved() {
    super.onRemoved();
    View target = super.getTarget();
    if (target != null) target.removeOnAttachStateChangeListener(this);
  }

  @Override void onActive() {
    super.onActive();
    super.addListener(this.listener);
    this.prepare(false);
  }

  @Override void onInActive() {
    super.onInActive();
    super.removeListener(this.listener);
    this.release();
  }

  @Override public void onViewAttachedToWindow(View v) {
    this.targetAttached.set(true);
    View target = super.getTarget();
    if (target != null) {
      // Find a ancestor of target whose parent is a CoordinatorLayout, or null.
      View colChild = findSuitableParent(manager.decorView, target);
      ViewGroup.LayoutParams params = colChild != null ? colChild.getLayoutParams() : null;

      if (params instanceof CoordinatorLayout.LayoutParams) {
        // TODO deal with CoordinatorLayout.
      }

      manager.onTargetActive(target);
      target.addOnLayoutChangeListener(this);
    }
  }

  @Override public void onViewDetachedFromWindow(View v) {
    this.targetAttached.set(false);
    View target = super.getTarget();
    if (target != null) {
      target.removeOnLayoutChangeListener(this);
      manager.onTargetInActive(target);
    }
  }

  @Override
  public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
      int oldTop, int oldRight, int oldBottom) {
    if (layoutChanged(left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)) {
      manager.onPlaybackInternalChanged(this);
    }
  }

  @CallSuper @Override public void release() {
    handler.removeCallbacksAndMessages(null);
  }

  @Override LocToken getLocToken() {
    View target = super.getTarget();
    if (target == null || !this.targetAttached.get()) return null;

    Rect playerRect = new Rect();
    boolean visible = target.getGlobalVisibleRect(playerRect, new Point());
    if (!visible) return null;

    Rect drawRect = new Rect();
    target.getDrawingRect(drawRect);
    int drawArea = drawRect.width() * drawRect.height();

    float offset = 0.f;
    if (drawArea > 0) {
      int visibleArea = playerRect.height() * playerRect.width();
      offset = visibleArea / (float) drawArea;
    }

    return offset >= 0.65f ?  //
        new LocToken(playerRect.centerX(), playerRect.centerY(), offset) : null;
  }
}