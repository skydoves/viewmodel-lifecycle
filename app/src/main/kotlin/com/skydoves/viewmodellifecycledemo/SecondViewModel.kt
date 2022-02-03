/*
 * Copyright (C) 2022 skydoves
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

package com.skydoves.viewmodellifecycledemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.skydoves.viewmodel.lifecycle.addViewModelOnClearedObserver
import com.skydoves.viewmodel.lifecycle.addViewModelOnInitializedObserver
import com.skydoves.viewmodel.lifecycle.rxkotlin3.autoDisposable
import com.skydoves.viewmodel.lifecycle.viewModelLifecycleOwner
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber

class SecondViewModel : ViewModel() {

  val liveData: MutableLiveData<String> = MutableLiveData()

  val compositeDisposable by autoDisposable(CompositeDisposable())

  init {
    Timber.d("initialize SecondViewModel")

    liveData.value = "SecondViewModel"

    liveData.observe(viewModelLifecycleOwner) {
      Timber.d("observe: $it")
    }

    viewModelLifecycleOwner.addViewModelOnInitializedObserver {
      Timber.d("SecondViewModel initialized, " + viewModelLifecycleOwner.lifecycle.currentState)
    }

    viewModelLifecycleOwner.addViewModelOnClearedObserver {
      Timber.d("SecondViewModel cleared, " + viewModelLifecycleOwner.lifecycle.currentState)
    }
  }
}
