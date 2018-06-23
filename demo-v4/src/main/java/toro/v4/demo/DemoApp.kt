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

package toro.v4.demo

import android.app.Application
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.squareup.leakcanary.LeakCanary
import im.ene.toro.exoplayer.Config

/**
 * @author eneim (2018/05/27).
 */
class DemoApp : Application() {

  companion object {
    var app: DemoApp? = null
    var config: Config = Config.Builder().build()
  }

  override fun onCreate() {
    super.onCreate()
    app = this
    config = config.newBuilder().setCache(
        SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(8 * 1024 * 1024))).build()
    if (LeakCanary.isInAnalyzerProcess(this)) {
      return
    }

    LeakCanary.install(this)
  }
}