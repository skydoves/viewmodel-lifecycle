/*
 * Designed and developed by 2022 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.viewmodel.lifecycle.coroutines

import androidx.lifecycle.ViewModel
import com.skydoves.viewmodel.lifecycle.DefaultViewModelLifecycleObserver
import com.skydoves.viewmodel.lifecycle.ViewModelLifecycle
import com.skydoves.viewmodel.lifecycle.ViewModelLifecycleObserver
import com.skydoves.viewmodel.lifecycle.ViewModelLifecycleOwner
import com.skydoves.viewmodel.lifecycle.ViewModelState
import com.skydoves.viewmodel.lifecycle.viewModelLifecycle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Add an observer and observes continuously a [ViewModelState] from the [ViewModelLifecycleOwner]. */
public fun ViewModelLifecycleOwner.viewModelLifecycleFlow(): Flow<ViewModelState> =
  callbackFlow {
    val observer = ViewModelLifecycleObserver { trySend(it) }
    viewModelLifecycle.addObserver(observer)

    awaitClose {
      this@viewModelLifecycleFlow.viewModelLifecycle.removeObserver(observer)
    }
  }

/**
 * Add an observer and get notified only once when the [ViewModel] is initialized.
 * The observer will be removed from the [ViewModelLifecycle] once get notified.
 */
public fun ViewModelLifecycleOwner.viewModelOnInitializedFlow(): Flow<Unit> =
  callbackFlow {
    val observer = object : DefaultViewModelLifecycleObserver {
      override fun onInitialized(viewModelLifecycleOwner: ViewModelLifecycleOwner) {
        trySend(Unit)
      }
    }
    viewModelLifecycle.addObserver(observer)

    awaitClose {
      this@viewModelOnInitializedFlow.viewModelLifecycle.removeObserver(observer)
    }
  }

/**
 * Add an observer and get notified only once when the [ViewModel] is cleared.
 * The observer will be removed from the [ViewModelLifecycle] once get notified.
 */
public fun ViewModelLifecycleOwner.viewModelOnClearedFlow(): Flow<Unit> =
  callbackFlow {
    val observer = object : DefaultViewModelLifecycleObserver {
      override fun onCleared(viewModelLifecycleOwner: ViewModelLifecycleOwner) {
        trySend(Unit)
      }
    }
    viewModelLifecycle.addObserver(observer)

    awaitClose {
      this@viewModelOnClearedFlow.viewModelLifecycle.removeObserver(observer)
    }
  }
