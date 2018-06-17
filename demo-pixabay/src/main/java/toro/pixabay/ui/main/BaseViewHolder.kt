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

package toro.pixabay.ui.main

import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import com.bumptech.glide.request.RequestOptions
import toro.pixabay.R
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author eneim (2018/05/11).
 */
abstract class BaseViewHolder(view: View) : ViewHolder(view) {

  companion object {
    val options = RequestOptions().centerCrop().autoClone()
    private val placeholders = arrayOf(R.drawable.placeholder_1, R.drawable.placeholder_2,
        R.drawable.placeholder_3, R.drawable.placeholder_4, R.drawable.placeholder_5)
    private val index = AtomicInteger(0)
    fun getPlaceHolder() = placeholders[index.getAndIncrement() % placeholders.size]
  }

  @Suppress("UNUSED_PARAMETER")
  open fun bind(item: Any?) {
  }
}