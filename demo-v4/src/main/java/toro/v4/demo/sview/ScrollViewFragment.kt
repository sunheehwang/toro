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

package toro.v4.demo.sview

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import toro.v4.Toro
import toro.v4.demo.R

/**
 * @author eneim (2018/05/27).
 */
class ScrollViewFragment : Fragment() {

  companion object {
    fun newInstance() = ScrollViewFragment()
  }

  private val videoUri: Uri =
      Uri.parse(
          "https://storage.googleapis.com/spec-host/mio-material/assets/1MvJxcu1kd5TFR6c5IBhxjLueQzSZvVQz/m2-manifesto.mp4")

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_scroll_view, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Toro.with(requireContext())
        .play(videoUri)
        .tag("Player:" + hashCode())
        .into(view.findViewById(R.id.playerView))
  }
}