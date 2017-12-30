/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro.sample.selectors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.net.ConnectivityManagerCompat;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.widget.Container;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author eneim (2017/12/28).
 */

public class NetworkAwareSelector extends BroadcastReceiver implements PlayerSelector {

  final AtomicBoolean online = new AtomicBoolean(true);
  final PlayerSelector origin;

  public NetworkAwareSelector(PlayerSelector origin) {
    this.origin = origin;
  }

  @Override public void onReceive(Context context, Intent intent) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = ConnectivityManagerCompat.getNetworkInfoFromBroadcast(cm, intent);
    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    online.set(isConnected);
  }

  @NonNull @Override public Collection<ToroPlayer> select(@NonNull Container container,
      @NonNull List<ToroPlayer> items) {
    return online.get() ? Collections.emptyList() : items;
  }

  @NonNull @Override public PlayerSelector reverse() {
    return this;
  }
}
