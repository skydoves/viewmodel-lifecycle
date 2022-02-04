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

package com.skydoves.viewmodel.lifecycle

import androidx.lifecycle.Lifecycle

/**
 * @author skydoves (Jaewoong Eum)
 *
 * Returns a corresponding [ViewModelState] from a [Lifecycle.State].
 */
internal fun Lifecycle.State.toViewModelState(): ViewModelState {
  return if (this == Lifecycle.State.DESTROYED) {
    ViewModelState.CLEARED
  } else {
    ViewModelState.INITIALIZED
  }
}

/**
 * @author skydoves (Jaewoong Eum)
 *
 * Returns a corresponding [ViewModelState] from a [Lifecycle.Event].
 */
internal fun Lifecycle.Event.toViewModelState(): ViewModelState {
  return if (this == Lifecycle.Event.ON_DESTROY) {
    ViewModelState.CLEARED
  } else {
    ViewModelState.INITIALIZED
  }
}

/**
 * @author skydoves (Jaewoong Eum)
 *
 * Returns a corresponding [Lifecycle.Event] from a [Lifecycle.State].
 */
internal fun Lifecycle.State.toEvent(): Lifecycle.Event {
  return when (this) {
    Lifecycle.State.INITIALIZED,
    Lifecycle.State.CREATED -> Lifecycle.Event.ON_CREATE
    Lifecycle.State.STARTED -> Lifecycle.Event.ON_START
    Lifecycle.State.RESUMED -> Lifecycle.Event.ON_RESUME
    else -> Lifecycle.Event.ON_DESTROY
  }
}
