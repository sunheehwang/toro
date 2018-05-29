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

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author eneim (2018/05/26).
 * @since 4.0.0.2800
 */
final class LimitedArrayList<E> extends ArrayList<E> {

  LimitedArrayList(@NonNull Collection<? extends E> c) {
    super(c);
  }

  // Just to make this method available for internal use.
  @Override protected void removeRange(int fromIndex, int toIndex) {
    super.removeRange(fromIndex, toIndex);
  }
}

