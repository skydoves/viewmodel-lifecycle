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
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
internal class ViewModelLifecycleTest {

  @Test
  fun verifyViewModeLifecycleState() {
    var viewModel: LifecycleTestViewModel? = null

    val scenario = ActivityScenario.launch(ViewModelLifecycleTestActivity::class.java).use {
      it.moveToState(Lifecycle.State.CREATED)
      it.onActivity { activity ->
        assertThat(activity.viewModel).isNotNull()
        assertThat(
          activity.viewModel.viewModelLifecycleOwner.viewModelLifecycle.viewModelState
        ).isEqualTo(
          ViewModelState.INITIALIZED
        )

        viewModel = activity.viewModel
      }
    }

    scenario.moveToState(Lifecycle.State.DESTROYED)

    assertThat(viewModel).isNotNull()
    assertThat(
      viewModel?.viewModelLifecycleOwner?.viewModelLifecycle?.viewModelState
    ).isEqualTo(
      ViewModelState.CLEARED
    )
  }

  @Test
  fun verifyVieModelLifecycleObserver() {
    var viewModel: LifecycleTestViewModel? = null

    val lifecycleObserver = mock<ViewModelLifecycleObserver>()

    val scenario = ActivityScenario.launch(ViewModelLifecycleTestActivity::class.java).use {
      it.moveToState(Lifecycle.State.CREATED)
      it.onActivity { activity ->
        assertThat(activity.viewModel).isNotNull()
        viewModel = activity.viewModel.also { vm ->
          vm.viewModelLifecycleOwner.viewModelLifecycle.addObserver(lifecycleObserver)
        }
      }
    }

    assertThat(viewModel).isNotNull()

    verify(lifecycleObserver).onStateChanged(ViewModelState.INITIALIZED)

    scenario.moveToState(Lifecycle.State.DESTROYED)

    verify(lifecycleObserver).onStateChanged(ViewModelState.CLEARED)
  }
}
