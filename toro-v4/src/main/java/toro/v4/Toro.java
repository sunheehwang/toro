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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import com.google.android.exoplayer2.util.Util;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ToroExo;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import toro.v4.Playback.RequestWeakReference;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static java.lang.Runtime.getRuntime;

/**
 * @author eneim (2018/05/25).
 * @since 4.0.0.2800
 */
@SuppressWarnings("WeakerAccess") //
public final class Toro {

  // Magic number: Build.VERSION.SDK_INT / 6 --> API 16 ~ 18 will set pool size to 2, etc.
  @SuppressWarnings("WeakerAccess") //
  static final int MAX_POOL_SIZE = Math.max(Util.SDK_INT / 6, getRuntime().availableProcessors());

  final Context app;
  final ToroExo toroExo;
  // Adopt from how Picasso clean the reference cache.
  final ReferenceQueue<Object> referenceQueue;
  final CleanupThread cleanupThread;

  final WeakHashMap<Context, Manager> managers;
  final WeakHashMap<Context, Bundle> states;

  Toro(@NonNull Context app) {
    this.app = ToroUtil.checkNotNull(app).getApplicationContext();
    this.toroExo = ToroExo.with(app);
    this.managers = new WeakHashMap<>();
    this.states = new WeakHashMap<>();

    ((Application) this.app).registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
      @Override public void onActivityCreated(Activity activity, Bundle state) {
        Bundle playbackCache = state != null ? state.getBundle(KEY_ACTIVITY_STATES) : null;
        states.put(activity, playbackCache);
      }

      @Override public void onActivityStarted(Activity activity) {
        Manager manager = managers.get(activity);
        if (manager != null) manager.onStart();
      }

      @Override public void onActivityResumed(Activity activity) {

      }

      @Override public void onActivityPaused(Activity activity) {

      }

      @Override public void onActivityStopped(Activity activity) {
        Manager manager = managers.get(activity);
        if (manager != null) manager.onStop();
      }

      @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Manager manager = managers.get(activity);
        Bundle playbackState = manager != null ? manager.fetchPlayableInfoBundle() : null;
        if (playbackState != null) {
          outState.putBundle(KEY_ACTIVITY_STATES, playbackState);
        }
      }

      // TODO [20180527] This method is called before DecorView is detached.
      @Override public void onActivityDestroyed(Activity activity) {
        Manager manager = managers.remove(activity);
        if (manager != null) manager.onDestroy();
      }
    });

    this.referenceQueue = new ReferenceQueue<>();
    this.cleanupThread = new CleanupThread(referenceQueue, HANDLER);
    this.cleanupThread.start();
  }

  static final String KEY_ACTIVITY_STATES = "toro:activity:states";
  static final String KEY_MANAGER_STATES = "toro:manager:states";

  @SuppressLint("StaticFieldLeak") static Toro toro;

  public static Toro with(Context context) {
    if (toro == null) {
      synchronized (Toro.class) {
        if (toro == null) toro = new Toro(context);
      }
    }

    return toro;
  }

  /**
   * Get the {@link Manager} for an {@link Activity} or create new if there is no cached one.
   *
   * @param context the {@link Activity}.
   * @return the {@link Manager} to manages {@link Playback}s of the {@link Activity}.
   */
  @NonNull final Manager getManager(@NonNull Context context) {
    if (!(context instanceof Activity)) {
      throw new RuntimeException("Expect Activity, found: " + context.getClass().getSimpleName());
    }

    View decorView = ((Activity) context).getWindow().peekDecorView();
    if (decorView == null) {
      throw new IllegalStateException("DecorView is null");
    }

    Manager manager = managers.get(context);
    if (manager == null) {
      manager = new Manager(this, decorView);
      managers.put(context, manager);
      Bundle cache = states.get(context);
      if (cache != null) manager.providePlayableInfoBundle(cache);

      if (ViewCompat.isAttachedToWindow(decorView)) {
        manager.onAttached();
      }

      decorView.addOnAttachStateChangeListener(
          new ManagerAttachStateListener(context, decorView, manager.attachFlag));
    }

    return manager;
  }

  /// START Public API

  public final Playable play(@NonNull Uri uri) {
    return new PlayableImpl(this, uri);
  }

  /// END Public API

  static class ManagerAttachStateListener implements View.OnAttachStateChangeListener {

    final WeakReference<Context> context;
    final AtomicBoolean attachFlag;
    final View view;

    ManagerAttachStateListener(Context context, View view, AtomicBoolean attachFlag) {
      this.context = new WeakReference<>(context);
      this.attachFlag = attachFlag;
      this.view = view;
    }

    @Override public void onViewAttachedToWindow(View v) {
      if (!attachFlag.get()) {
        Manager toAttach = toro.managers.get(context.get());
        if (toAttach != null) {
          toAttach.onAttached();
          attachFlag.set(true);
        }
      }
    }

    @Override public void onViewDetachedFromWindow(View v) {
      if (attachFlag.get()) {
        Manager toDetach = toro.managers.remove(context.get());
        if (toDetach != null) {
          toDetach.onDetached();
          attachFlag.set(false);
        }
      }

      if (this.view == v) this.view.removeOnAttachStateChangeListener(this);
    }
  }

  static class GlobalScrollChangeListener implements OnScrollChangedListener {

    private static final int EVENT_SCROLL = 1;
    private static final int EVENT_IDLE = 2;
    private static final int EVENT_DELAY = 50;  // 50 ms, 3 frames

    final AtomicBoolean scrollConsumed = new AtomicBoolean(false);
    final WeakReference<Manager> managerRef;

    GlobalScrollChangeListener(Manager manager) {
      this.managerRef = new WeakReference<>(manager);
    }

    final Handler handler = new Handler(Looper.getMainLooper()) {
      @Override public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Manager container = managerRef.get();
        if (container == null) return;
        switch (msg.what) {
          case EVENT_SCROLL:
            container.setScrolling(true);
            handler.sendEmptyMessageDelayed(EVENT_IDLE, EVENT_DELAY);
            break;
          case EVENT_IDLE:
            container.setScrolling(false);
            if (scrollConsumed.compareAndSet(false, true)) container.performRefreshAll();
            break;
        }
      }
    };

    @Override public void onScrollChanged() {
      scrollConsumed.set(false);
      handler.removeMessages(EVENT_IDLE);
      handler.sendEmptyMessageDelayed(EVENT_SCROLL, EVENT_DELAY);
    }
  }

  // 'Copy' from Picasso

  /**
   * When the target of a playback is weakly reachable but the request hasn't been canceled, it
   * gets added to the reference queue. This thread empties the reference queue and cancels the
   * request.
   */
  static final int REQUEST_GCED = 3;
  static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
    @Override public void handleMessage(Message msg) {
      int what = msg.what;
      switch (what) {
        case REQUEST_GCED: {
          // cancel any ongoing preparation
          Playback playback = (Playback) msg.obj;
          //noinspection unchecked
          playback.manager.cancel(playback.getTarget());
          break;
        }
      }
    }
  };

  static final String THREAD_PREFIX = BuildConfig.LIB_NAME;
  static final int THREAD_LEAK_CLEANING_MS = 1000;

  static class CleanupThread extends Thread {
    private final ReferenceQueue<Object> referenceQueue;
    private final Handler handler;

    CleanupThread(ReferenceQueue<Object> referenceQueue, Handler handler) {
      this.referenceQueue = referenceQueue;
      this.handler = handler;
      setDaemon(true);
      setName(THREAD_PREFIX + "refQueue");
    }

    @Override public void run() {
      Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
      while (true) {
        try {
          // Prior to Android 5.0, even when there is no local variable, the result from
          // remove() & obtainMessage() is kept as a stack local variable.
          // We're forcing this reference to be cleared and replaced by looping every second
          // when there is nothing to do.
          // This behavior has been tested and reproduced with heap dumps.
          RequestWeakReference<?> remove =
              (RequestWeakReference<?>) referenceQueue.remove(THREAD_LEAK_CLEANING_MS);
          Message message = handler.obtainMessage();
          if (remove != null) {
            message.what = REQUEST_GCED;
            message.obj = remove.playback;
            handler.sendMessage(message);
          } else {
            message.recycle();
          }
        } catch (InterruptedException e) {
          break;
        } catch (final Exception e) {
          handler.post(new Runnable() {
            @Override public void run() {
              throw new RuntimeException(e);
            }
          });
          break;
        }
      }
    }

    void shutdown() {
      interrupt();
    }
  }
}
