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

import android.os.Handler;
import android.os.Message;

/**
 * @author eneim (2018/06/23).
 */
final class Dispatcher extends Handler {

  private static final int MSG_DELAY = 5 * 1000 / 60;
  private static final int MSG_REFRESH = 1;

  private final Manager manager;

  Dispatcher(Manager manager) {
    super();
    this.manager = manager;
  }

  @Override public void handleMessage(Message msg) {
    super.handleMessage(msg);
    int what = msg.what;
    switch (what) {
      case MSG_REFRESH:
        manager.performRefreshAll();
        break;
      default:
        break;
    }
  }

  /// APIs

  void dispatchRefreshAll() {
    removeMessages(MSG_REFRESH);
    sendEmptyMessageDelayed(MSG_REFRESH, MSG_DELAY);
  }
}
