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

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_main.navigation
import toro.v4.Playable
import toro.v4.Toro
import toro.v4.demo.rview.RecyclerViewFragment
import toro.v4.demo.sview.ScrollViewFragment
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity(), PlayableProvider {

  private val videoUri: Uri =
      Uri.parse(
          "https://storage.googleapis.com/spec-host/mio-material/assets/1MvJxcu1kd5TFR6c5IBhxjLueQzSZvVQz/m2-manifesto.mp4")

  private val playable: Playable by lazy {
    Toro.with(this).setUp(videoUri).repeatMode(Player.REPEAT_MODE_ONE)
        .config(DemoApp.config)
        .tag("Player:Material").asPlayable()
  }

  private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
    val oldValue = checkedNav.getAndSet(item.itemId)
    val oldFragment = fragments.get(oldValue)

    var transaction = supportFragmentManager.beginTransaction()
    if (oldFragment != null) {
      transaction = transaction.detach(oldFragment)
      detachedFragments.put(oldValue, oldFragment)
    }

    var newFragment = detachedFragments.get(item.itemId)
    if (newFragment != null) {
      transaction.attach(newFragment)
    } else {
      newFragment = fragments.get(item.itemId)
      transaction.add(R.id.fragment, newFragment)
    }

    transaction.commit()
    true
  }

  private val fragments = SparseArray<Fragment>()
  private val checkedNav = AtomicInteger(-1)

  private val detachedFragments = SparseArray<Fragment>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    fragments.put(R.id.navigation_home, RecyclerViewFragment.newInstance())
    fragments.put(R.id.navigation_dashboard, ScrollViewFragment.newInstance())
    fragments.put(R.id.navigation_notifications, ScrollViewFragment.newInstance())
  }

  override fun requirePlayable(): Playable {
    return this.playable
  }

  override fun requirePlayableTag(): String {
    return "Player:Material"
  }
}
