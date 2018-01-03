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
import im.ene.toro.PlayerSelector;

/**
 * @author eneim (2018/01/02).
 */

public class Selectors {

  private Selectors() {
  }

  public static PlayerSelector getNetworkAwareSelector(Activity activity) {
    return new NetworkAwareSelector(activity, PlayerSelector.DEFAULT);
  }

  public static PlayerSelector getNetworkAwareSelector(Activity activity, PlayerSelector origin) {
    return new NetworkAwareSelector(activity, origin);
  }

  public static PlayerSelector getNetworkAwareSelector(Activity activity, PlayerSelector origin,
      NetworkAwareSelector.OnStatusChangeListener listener) {
    return new NetworkAwareSelector(activity, origin, listener);
  }

  public static PlayerSelector getNetworkAwareSelector(Activity activity,
      NetworkAwareSelector.OnStatusChangeListener listener) {
    return new NetworkAwareSelector(activity, PlayerSelector.DEFAULT, listener);
  }
}
