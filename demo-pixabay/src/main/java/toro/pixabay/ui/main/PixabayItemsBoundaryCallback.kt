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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagedList.BoundaryCallback
import android.arch.paging.PagingRequestHelper
import android.arch.paging.PagingRequestHelper.RequestType.AFTER
import android.arch.paging.PagingRequestHelper.RequestType.INITIAL
import android.support.annotation.MainThread
import android.util.Log
import toro.pixabay.common.NetworkState
import toro.pixabay.common.createStatusLiveData
import toro.pixabay.data.MixedApi
import toro.pixabay.data.entity.PixabayItem
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

@Suppress("CanBeParameter")
class PixabayItemsBoundaryCallback(
    private val mixedApi: MixedApi,
    private val ioExecutor: Executor,
    private val helper: PagingRequestHelper,
    private val handleResponse: (List<PixabayItem>?, Boolean) -> Unit,
    private val photoPageSize: Int,
    private val videoPageSize: Int
) : BoundaryCallback<PixabayItem>() {

  companion object {
    const val TAG = "Toro:BCallback"
  }

  @Suppress("MemberVisibilityCanBePrivate")
  val pageNumber = AtomicInteger(1)
  val networkState = helper.createStatusLiveData()

  fun refresh(query: String): LiveData<NetworkState> {
    val networkState = MutableLiveData<NetworkState>()
    networkState.value = NetworkState.LOADING
    pageNumber.set(1)
    helper.runIfNotRunning(INITIAL) {
      ioExecutor.execute {
        try {
          val result = mixedApi.search(query, pageNumber.get(), photoPageSize, videoPageSize)
          handleResponse(result.hits, true)
          it.recordSuccess()
          pageNumber.incrementAndGet()
          networkState.postValue(NetworkState.LOADED)
        } catch (error: Throwable) {
          it.recordFailure(error)
          networkState.postValue(NetworkState.error(error.message))
        }
      }
    }
    return networkState
  }

  /**
   * Database returned 0 items. We should query the backend for more items.
   */
  @MainThread
  override fun onZeroItemsLoaded() {
    pageNumber.set(1)
    helper.runIfNotRunning(INITIAL) {
      ioExecutor.execute {
        try {
          val result = mixedApi.search(null, pageNumber.get(), photoPageSize, videoPageSize)
          handleResponse(result.hits, false)
          pageNumber.incrementAndGet()
        } catch (error: Throwable) {
          it.recordFailure(error)
        }
      }
    }
  }

  /**
   * User reached to the end of the list.
   */
  @MainThread
  override fun onItemAtEndLoaded(item: PixabayItem) {
    helper.runIfNotRunning(AFTER) {
      ioExecutor.execute {
        Log.i(TAG, "onItemAtEndLoaded() is called")
        try {
          val result = mixedApi.search(null, pageNumber.get(), photoPageSize, videoPageSize)
          handleResponse(result.hits, false)
          it.recordSuccess()
          pageNumber.incrementAndGet()
        } catch (error: Throwable) {
          it.recordFailure(error)
        }
      }
    }
  }

  override fun onItemAtFrontLoaded(itemAtFront: PixabayItem) {
    // ignored, since we only ever append to what's in the DB
  }
}