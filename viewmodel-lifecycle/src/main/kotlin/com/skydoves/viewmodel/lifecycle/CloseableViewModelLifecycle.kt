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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import java.io.Closeable

private const val JOB_KEY = "com.skydoves.viewmodel.lifecycle.ViewModelLifecycleOwner.JOB_KEY"

/**
 * @author skydoves (Jaewoong Eum)
 *
 * [ViewModelLifecycle] tied to this [ViewModel]. The lifecycle owner will be destroyed
 * when ViewModel will be cleared, i.e [ViewModel.onCleared] is called.
 */
@ViewModelLifespan
public val ViewModel.viewModelLifecycleOwner: ViewModelLifecycleOwner
  get() {
    val viewModelClass = traverseViewModel()
      ?: throw ClassNotFoundException("Couldn't find ViewModel class from super classes.")
    val getTagMethod = viewModelClass.getDeclaredMethod("getTag", String::class.java)
    getTagMethod.isAccessible = true
    val lifecycleOwner: ViewModelLifecycleOwner? =
      getTagMethod.invoke(this, JOB_KEY) as? ViewModelLifecycleOwner
    if (lifecycleOwner != null) {
      return lifecycleOwner
    }
    val setTagIfAbsentMethod = viewModelClass.getDeclaredMethod(
      "setTagIfAbsent",
      String::class.java,
      Any::class.java
    )
    setTagIfAbsentMethod.isAccessible = true
    return setTagIfAbsentMethod.invoke(
      this,
      JOB_KEY,
      CloseableViewModelLifecycleOwner()
    ) as ViewModelLifecycleOwner
  }

/**
 * @author skydoves (Jaewoong Eum)
 *
 * Traverses until find [ViewModel] from the super classes.
 */
private fun ViewModel.traverseViewModel(): Class<in ViewModel>? {
  var superclass = this.javaClass.superclass
  while (superclass != null && superclass.name.toString() != "androidx.lifecycle.ViewModel") {
    superclass = superclass.superclass
  }
  return superclass
}

/**
 * @author skydoves (Jaewoong Eum)
 *
 * CloseableViewModelLifecycleOwner represents the lifecycle of a [ViewModel], and implements
 * [Closeable] and [ViewModelLifecycle].
 */
internal class CloseableViewModelLifecycleOwner : Closeable, ViewModelLifecycleOwner {

  private val viewModelLifecycle: ViewModelLifecycle = ViewModelLifecycle(
    provider = this,
    isEnforceMainThread = true
  )

  init {
    viewModelLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
  }

  override fun close() {
    viewModelLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  }

  override fun getLifecycle(): Lifecycle = viewModelLifecycle
}
