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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import toro.v4.Playable
import toro.v4.demo.R
import toro.v4.demo.fsplayer.SinglePlayerActivity

/**
 * @author eneim (2018/06/19).
 */
class RecyclerViewFragment : Fragment(), VideoViewHolder.ClickListener {

  companion object {
    fun newInstance() = RecyclerViewFragment()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_recycler_view, container, false)
  }

  private val adapter by lazy { SectionContentAdapter(this) }
  private val disposable = CompositeDisposable()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

    recyclerView.apply {
      adapter = this@RecyclerViewFragment.adapter
      layoutManager = LinearLayoutManager(requireContext())
    }

    disposable.add(Motion.contents()
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .doOnNext { adapter.updateElements(it) }
        .subscribe()
    )
  }

  override fun onClick(view: View?, tag: String, playable: Playable) {
    startActivity(SinglePlayerActivity.createIntent(requireContext(), tag))
  }

}