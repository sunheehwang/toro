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
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagingRequestHelper
import toro.pixabay.common.ListModel
import toro.pixabay.common.NetworkState
import toro.pixabay.data.MixedApi
import toro.pixabay.data.PixabayDao
import toro.pixabay.data.entity.PixabayItem
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

/**
 * @author eneim (2018/05/10).
 */
class MainRepository @Inject constructor(
    mixedApi: MixedApi,
    private val dao: PixabayDao,
    @Named("io.executor") private val ioExecutor: Executor,
    @Named("disk.executor") private val diskExecutor: Executor
) {

  companion object {
    private const val DEFAULT_NETWORK_PAGE_SIZE = 10
  }

  private val helper = PagingRequestHelper(ioExecutor)
  private val photoPageSize = DEFAULT_NETWORK_PAGE_SIZE * 3
  private val videoPageSize = DEFAULT_NETWORK_PAGE_SIZE
  private val boundaryCallback = PixabayItemsBoundaryCallback(
      mixedApi, ioExecutor, helper, this::insertToDb, photoPageSize, videoPageSize
  )

  private fun insertToDb(result: List<PixabayItem>?, reset: Boolean) {
    diskExecutor.execute {
      if (reset) dao.deleteAllItems()
      dao.insertItems(result!!)
    }
  }

  fun getItems(query: String): ListModel<PixabayItem> {
    val refreshTrigger = MutableLiveData<Unit>()
    val refreshState = Transformations.switchMap(refreshTrigger) {
      refresh(query)
    }

    return ListModel<PixabayItem>(
        LivePagedListBuilder<Int, PixabayItem>(
            dao.getAllItems(),
            photoPageSize + videoPageSize
        ).setBoundaryCallback(boundaryCallback).build(),
        boundaryCallback.networkState,
        refreshState,
        refresh = { refreshTrigger.value = null },
        retry = { helper.retryAllFailed() }
    )
  }

  private fun refresh(query: String): LiveData<NetworkState> {
    return boundaryCallback.refresh(query)
  }
}

