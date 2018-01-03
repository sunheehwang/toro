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

package im.ene.toro.extra;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.widget.Container;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.support.v4.net.ConnectivityManagerCompat.getNetworkInfoFromBroadcast;

/**
 * @author eneim (2018/01/02).
 *
 *         Provided by a delegated {@link PlayerSelector}, when network is on, follow its rules, or
 *         select none Player otherwise.
 */

final class NetworkAwareSelector extends BroadcastReceiver implements PlayerSelector {

  private final Collection<ToroPlayer> EMPTY = Collections.emptyList();

  @NonNull final Activity activity;
  @NonNull final PlayerSelector origin;
  @Nullable final OnStatusChangeListener listener;

  NetworkAwareSelector(@NonNull Activity activity, @NonNull PlayerSelector origin,
      OnStatusChangeListener listener) {
    this.activity = activity;
    this.origin = origin;
    this.listener = listener;

    this.init();
  }

  NetworkAwareSelector(Activity activity, PlayerSelector origin) {
    this(activity, origin, null);
  }

  private void init() {
    this.activity.getApplication()
        .registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
          @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

          }

          @Override public void onActivityStarted(Activity activity) {
            if (activity == NetworkAwareSelector.this.activity) {
              IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
              activity.registerReceiver(NetworkAwareSelector.this, filter);
            }
          }

          @Override public void onActivityResumed(Activity activity) {

          }

          @Override public void onActivityPaused(Activity activity) {

          }

          @Override public void onActivityStopped(Activity activity) {
            if (activity == NetworkAwareSelector.this.activity) {
              activity.unregisterReceiver(NetworkAwareSelector.this);
            }
          }

          @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

          }

          @Override public void onActivityDestroyed(Activity activity) {
            if (activity == NetworkAwareSelector.this.activity) {
              activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            }
          }
        });
  }

  @NonNull @Override public Collection<ToroPlayer> select(@NonNull Container container,
      @NonNull List<ToroPlayer> items) {
    return online.get() ? origin.select(container, items) : EMPTY;
  }

  @NonNull @Override public PlayerSelector reverse() {
    return this;
  }

  private final AtomicBoolean online = new AtomicBoolean(true); // true by default

  @Override public void onReceive(Context context, Intent intent) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm != null ? getNetworkInfoFromBroadcast(cm, intent) : null;
    boolean oldStatus = online.get();
    online.set(networkInfo != null && networkInfo.isConnectedOrConnecting());
    if (oldStatus != online.get() && listener != null) listener.onStatusChange(online.get());
  }

  public interface OnStatusChangeListener {

    void onStatusChange(boolean online);
  }
}
