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

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import kotlinx.android.synthetic.main.activity_main.navigation
import toro.v4.demo.sview.ScrollViewFragment
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

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

    fragments.put(R.id.navigation_home, ScrollViewFragment.newInstance())
    fragments.put(R.id.navigation_dashboard, ScrollViewFragment.newInstance())
    fragments.put(R.id.navigation_notifications, ScrollViewFragment.newInstance())
  }
}
