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

package com.skydoves.viewmodel.lifecycle.rxkotlin3

import androidx.lifecycle.ViewModel
import com.skydoves.viewmodel.lifecycle.addViewModelOnClearedObserver
import com.skydoves.viewmodel.lifecycle.viewModelLifecycleOwner
import io.reactivex.rxjava3.disposables.Disposable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author skydoves (Jaewoong Eum)
 *
 * Creates a [Disposable] read-only delegate property,
 * which will call the [Disposable.dispose] when the ViewModel is cleared.
 *
 * @param value A default [Disposable] value that should be initialized.
 */
public fun <T : Disposable> ViewModel.autoDisposable(value: T): AutoDisposableProperty<T> =
  AutoDisposableProperty(this, value)

/**
 * @author skydoves (Jaewoong Eum)
 *
 * A [Disposable] read-only delegate class to call [Disposable.dispose] when the ViewModel is cleared.
 *
 * @param viewModel A [ViewModel].
 * @param value An initial [Disposable] value.
 */
public class AutoDisposableProperty<T : Disposable>(
  viewModel: ViewModel,
  private val value: T
) : ReadOnlyProperty<ViewModel, T> {

  init {
    viewModel.viewModelLifecycleOwner.addViewModelOnClearedObserver {
      value.dispose()
    }
  }

  override fun getValue(thisRef: ViewModel, property: KProperty<*>): T = value
}
