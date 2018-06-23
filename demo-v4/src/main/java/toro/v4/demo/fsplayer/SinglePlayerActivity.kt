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

package toro.v4.demo.fsplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_single_player.playerView
import toro.v4.Playable
import toro.v4.Toro
import toro.v4.demo.R

/**
 * @author eneim (2018/05/30).
 */
class SinglePlayerActivity : AppCompatActivity() {

  companion object {
    const val KEY_PLAYABLE = "toro:demo:playable"

    fun createIntent(context: Context, playableKey: String): Intent {
      val intent = Intent(context, SinglePlayerActivity::class.java)
      intent.putExtras(Bundle().also { it.putString(KEY_PLAYABLE, playableKey) })
      return intent
    }
  }

  private lateinit var playable: Playable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_single_player)
    val key = intent?.getStringExtra(KEY_PLAYABLE)
    playable = Toro.with(this).requirePlayable(key!!)!!
    playable.bind(playerView)
  }
}