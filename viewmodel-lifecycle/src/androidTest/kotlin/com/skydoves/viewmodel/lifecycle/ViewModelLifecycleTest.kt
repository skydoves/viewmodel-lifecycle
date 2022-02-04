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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
internal class ViewModelLifecycleTest {

  @Rule
  @JvmField
  val instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @Test
  fun verifyViewModeLifecycleState() {
    var viewModelLifecycle: ViewModelLifecycle? = null

    val scenario = ActivityScenario.launch(ViewModelLifecycleTestActivity::class.java).use {
      it.moveToState(Lifecycle.State.CREATED)
      it.onActivity { activity ->
        assertThat(activity.viewModel).isNotNull()
        viewModelLifecycle = activity.viewModel.viewModelLifecycleOwner.viewModelLifecycle
        assertThat(viewModelLifecycle?.viewModelState).isEqualTo(ViewModelState.INITIALIZED)
        assertThat(viewModelLifecycle?.isCleared).isFalse()
      }
    }

    scenario.moveToState(Lifecycle.State.DESTROYED)

    assertThat(viewModelLifecycle?.viewModelState).isEqualTo(ViewModelState.CLEARED)
    assertThat(viewModelLifecycle?.isCleared).isTrue()
  }

  @Test
  fun verifyVieModelLifecycleObserver() {
    var viewModelLifecycle: ViewModelLifecycle? = null
    val lifecycleObserver = mock<ViewModelLifecycleObserver>()

    val scenario = ActivityScenario.launch(ViewModelLifecycleTestActivity::class.java).use {
      it.moveToState(Lifecycle.State.CREATED)
      it.onActivity { activity ->
        assertThat(activity.viewModel).isNotNull()
        viewModelLifecycle = activity.viewModel.viewModelLifecycleOwner.viewModelLifecycle
        viewModelLifecycle?.addObserver(lifecycleObserver)
        assertThat(viewModelLifecycle?.getObserverCount()).isEqualTo(1)
      }
    }

    verify(lifecycleObserver).onStateChanged(ViewModelState.INITIALIZED)

    scenario.moveToState(Lifecycle.State.DESTROYED)

    verify(lifecycleObserver).onStateChanged(ViewModelState.CLEARED)

    viewModelLifecycle?.removeObserver(lifecycleObserver)

    assertThat(viewModelLifecycle?.getObserverCount()).isEqualTo(0)
  }
}
