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
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author eneim (2018/05/25).
 */
class ViewPlayback<V extends View> extends Playback<V>
    implements View.OnAttachStateChangeListener, View.OnLayoutChangeListener {

  @SuppressWarnings("WeakerAccess") //
  static final Comparator<ViewToken> CENTER_Y = new Comparator<ViewToken>() {
    @Override public int compare(ViewToken o1, ViewToken o2) {
      return Float.compare(o1.centerY, o2.centerY);
    }
  };

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

  @SuppressWarnings("WeakerAccess") //
  final ToroPlayer.EventListener listener = new ToroPlayer.EventListener() {
    @Override public void onBuffering() {
      Log.d(TAG, "onBuffering() called");
      // do nothing
    }

    @Override public void onPlaying() {
      Log.d(TAG, "onPlaying() called");
      // do nothing
      View target = getTarget();
      if (target != null) target.setKeepScreenOn(true);
    }

    @Override public void onPaused() {
      Log.d(TAG, "onPaused() called");
      // do nothing
    }

    @Override public void onCompleted() {
      Log.d(TAG, "onCompleted() called");
      View target = getTarget();
      if (target != null) target.setKeepScreenOn(false);
    }
  };

  @SuppressWarnings("WeakerAccess") @NonNull  //
  protected final AtomicBoolean targetAttached = new AtomicBoolean(false);

  ViewPlayback(@NonNull Playable playable, @NonNull Uri uri, @NonNull Manager manager,
      @Nullable V target, @NonNull Playable.Options options) {
    super(playable, uri, manager, target, options);
    Log.i(TAG, "Playback: " + this.playable);
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

  @Override void onActive() {
    super.onActive();
    super.addListener(this.listener);
  }

  @Override void onInActive() {
    super.onInActive();
    super.removeListener(this.listener);
  }

  @Override void onRemoved(boolean recreating) {
    super.onRemoved(recreating);
    View target = super.getTarget();
    if (target != null) target.removeOnAttachStateChangeListener(this);
  }

  @Override public void onViewAttachedToWindow(View v) {
    this.targetAttached.set(true);
    View target = super.getTarget();
    if (target != null) {
      // Find a ancestor of target whose parent is a CoordinatorLayout, or null.
      View corChild = findSuitableParent(manager.decorView, target);
      ViewGroup.LayoutParams params = corChild != null ? corChild.getLayoutParams() : null;

      //noinspection StatementWithEmptyBody
      if (params instanceof CoordinatorLayout.LayoutParams) {
        // TODO [20180620] deal with CoordinatorLayout.
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

  @Nullable @Override protected ViewToken getToken() {
    Log.d(TAG, "getToken() called");
    View target = super.getTarget();
    if (target == null || !this.targetAttached.get()) return null;

    Rect playerRect = new Rect();
    boolean visible = target.getGlobalVisibleRect(playerRect, new Point());
    if (!visible) return null;
    Log.d(TAG, "getToken: " + playerRect);

    Rect drawRect = new Rect();
    target.getDrawingRect(drawRect);
    int drawArea = drawRect.width() * drawRect.height();

    float offset = 0.f;
    if (drawArea > 0) {
      int visibleArea = playerRect.height() * playerRect.width();
      offset = visibleArea / (float) drawArea;
    }
    Log.i(TAG, "getToken: " + offset);
    return offset >= 0.75f /* TODO [20180621] make this changeable */ ?  //
        new ViewToken(playerRect.centerX(), playerRect.centerY(), offset) : null;
  }

  // Location on screen, with visible offset within target's parent.
  protected static class ViewToken extends Token {
    final float centerX;
    final float centerY;
    final float areaOffset;

    ViewToken(float centerX, float centerY, float areaOffset) {
      this.centerX = centerX;
      this.centerY = centerY;
      this.areaOffset = areaOffset;
    }

    @Override public int compareTo(@NonNull Token o) {
      return o instanceof ViewToken ? CENTER_Y.compare(this, (ViewToken) o) : super.compareTo(o);
    }
  }
}