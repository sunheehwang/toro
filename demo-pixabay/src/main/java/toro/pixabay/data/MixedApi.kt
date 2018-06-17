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

package toro.pixabay.data

import android.util.Log
import toro.pixabay.data.entity.PhotoSearchResult
import toro.pixabay.data.entity.PixabayItem
import toro.pixabay.data.entity.PixabayItem.fromPhotoItem
import toro.pixabay.data.entity.PixabayItem.fromVideoItem
import toro.pixabay.data.entity.PixabayItemResult
import toro.pixabay.data.entity.VideoSearchResult
import javax.inject.Inject

/**
 * Mix the result of Photos and Videos.
 *
 * @author eneim (2018/06/15).
 */
class MixedApi @Inject constructor(private val api: Api) {

  companion object {
    const val TAG = "Toro:MixedApi"
  }

  fun search(query: String?, page: Int, photoPageSize: Int, videoPageSize: Int): PixabayItemResult {
    Log.d(TAG, "Call: $page")
    val photoResult = api.searchAllPhotos(page, photoPageSize).execute().body() as PhotoSearchResult
    val videoResult = api.searchAllVideos(page, videoPageSize).execute().body() as VideoSearchResult

    val photos = photoResult.hits
    val videos = videoResult.hits
    val itemCount = photos.size + videos.size
    val indexLimit = Math.min(itemCount / 3, videos.size)
    val result = arrayListOf<PixabayItem>()
    val nowNano = System.nanoTime()
    var index = 0
    while (index < indexLimit) {
      result += fromPhotoItem(photos[index * 3 + 0], nowNano).also { it.query = query }
      result += fromPhotoItem(photos[index * 3 + 1], nowNano).also { it.query = query }
      result += fromPhotoItem(photos[index * 3 + 2], nowNano).also { it.query = query }
      result += fromVideoItem(videos[index], nowNano).also { it.query = query }
      index++
    }

    return PixabayItemResult(
        result.size,
        photoResult.total + videoResult.total,
        result
    )
  }
}