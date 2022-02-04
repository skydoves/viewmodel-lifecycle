/*
 * Designed and developed by 2020 skydoves (Jaewoong Eum)
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

package com.skydoves.viewmodel.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

/**
 * @author skydoves (Jaewoong Eum)
 *
 * Obtains a [ViewModelLifecycle] from the [ViewModelLifecycleOwner].
 */
@ViewModelLifespan
public val ViewModelLifecycleOwner.viewModelLifecycle: ViewModelLifecycle
  inline get() = lifecycle as ViewModelLifecycle

/** Add an observer and observes continuously a [ViewModelState] from the [ViewModelLifecycleOwner]. */
public fun ViewModelLifecycleOwner.addViewModelLifecycleObserver(
  onStateChanged: (viewModelState: ViewModelState) -> Unit
) {
  viewModelLifecycle.addViewModelLifecycleObserver(onStateChanged)
}

/**
 * @author skydoves (Jaewoong Eum)
 *
 * Add an observer and get notified only once when the [ViewModel] is initialized.
 * The observer will be removed from the [ViewModelLifecycle] once get notified.
 */
public fun ViewModelLifecycleOwner.addViewModelOnInitializedObserver(
  onInitialized: (owner: LifecycleOwner) -> Unit
) {
  viewModelLifecycle.addViewModelLifecycleObserver(
    onInitialized = onInitialized,
    onCleared = {},
  )
}

/**
 * @author skydoves (Jaewoong Eum)
 *
 * Add an observer and get notified only once when the [ViewModel] is cleared.
 * The observer will be removed from the [ViewModelLifecycle] once get notified.
 */
public fun ViewModelLifecycleOwner.addViewModelOnClearedObserver(
  onCleared: (owner: LifecycleOwner) -> Unit
) {
  viewModelLifecycle.addViewModelLifecycleObserver(
    onInitialized = {},
    onCleared = onCleared
  )
}
