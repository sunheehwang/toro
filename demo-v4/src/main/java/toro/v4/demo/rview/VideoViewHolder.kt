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

package toro.v4.demo.rview

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import im.ene.toro.ToroPlayer.EventListener
import org.jsoup.nodes.Element
import toro.v4.Playable
import toro.v4.Toro
import toro.v4.demo.DemoApp
import toro.v4.demo.R
import java.util.regex.Pattern

/**
 * @author eneim (2018/01/23).
 */
class VideoViewHolder(inflater: LayoutInflater, parent: ViewGroup,
    private val listener: ClickListener) :
    BaseViewHolder(inflater.inflate(R.layout.exo_article_part_video, parent, false)),
    OnClickListener {

  init {
    itemView.setOnClickListener(this)
  }

  companion object {
    val ratioRegex = Pattern.compile("(\\d)+\\.(\\d+)")!!
    const val defaultRatio = 100 * 165.78F / 360F // magic number.
  }

  private val playerFrame by lazy { itemView as AspectRatioFrameLayout }
  private val player = itemView.findViewById(R.id.player) as PlayerView
  private val status = itemView.findViewById(R.id.playerStatus) as TextView
  private var videoUrl: String? = null
  private var playable: Playable? = null

  override fun bind(item: Any?) {
    super.bind(item)
    videoUrl = (item as Element).select("video > source[type=video/mp4]").attr("src")
    val videoUri = if (videoUrl != null) Uri.parse(videoUrl) else null
    val style = item.getElementsByClass("qp-ui-video-player-mouse").attr("style")
    if (style !== null) {
      val match = ratioRegex.matcher(style)
      var ratio = if (match.find()) match.group().toFloat() else null
      if (ratio === null) ratio = defaultRatio
      playerFrame.setAspectRatio(100F / ratio)
    }

    this.playable = Toro.with(itemView.context)
        .setUp(videoUri!!)
        .tag(videoUrl!!)
        .repeatMode(Player.REPEAT_MODE_ONE)
        .config(DemoApp.config)
        .asPlayable()

    this.playable!!.bind(player).addListener(object : EventListener {
      override fun onBuffering() {
        status.text = "Buffering"
      }

      override fun onPlaying() {
        status.text = "Playing"
      }

      override fun onPaused() {
        status.text = "Paused"
      }

      override fun onCompleted() {
        status.text = "Ended"
      }
    })
  }

  override fun onClick(v: View?) {
    if (this.playable != null) this.listener.onClick(v, videoUrl!!, this.playable!!)
  }

  interface ClickListener {
    fun onClick(view: View?, tag: String, playable: Playable)
  }
}