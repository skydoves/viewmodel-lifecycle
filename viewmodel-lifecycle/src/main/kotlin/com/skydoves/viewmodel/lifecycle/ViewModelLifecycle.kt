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

package com.skydoves.viewmodel.lifecycle

import androidx.annotation.MainThread
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycling
import androidx.lifecycle.ViewModel
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * @author skydoves (Jaewoong Eum)
 *
 * An implementation of [Lifecycle], which follows the [ViewModel]'s lifecycle.
 * ViewModelLifecycle handles multiple [LifecycleObserver] and [ViewModelLifecycleObserver]
 * to track [ViewModel]'s lifecycle. [ViewModelLifecycle] belongs to [ViewModelLifecycleOwner],
 * you can get this directly from the [ViewModelLifecycleOwner].
 *
 * [ViewModelLifecycleOwner] only handles [Lifecycle.Event.ON_START] and [Lifecycle.Event.ON_DESTROY],
 * which are the same as the [ViewModelState.INITIALIZED] and [ViewModelState.CLEARED].
 */
public class ViewModelLifecycle constructor(
  provider: ViewModelLifecycleOwner,
  private val isEnforceMainThread: Boolean = true
) : Lifecycle() {

  /**
   * The provider that owns this lifecycle. Only WeakReference on ViewModelLifecycleOwner is kept.
   */
  private val lifecycleOwner: WeakReference<ViewModelLifecycleOwner> = WeakReference(provider)

  /** A list of the [LifecycleObserver]-s, which tracks [ViewModel]'s lifecycle states. */
  private val observerMap:
    ConcurrentHashMap<LifecycleObserver, ObserverWithState> = ConcurrentHashMap()

  /** Current state of the [ViewModel] lifecycle. */
  private var state: State = State.INITIALIZED

  /** Current [ViewModelState]. */
  private val viewModelState: ViewModelState
    get() = state.toViewModelState()

  /** Check the current [ViewModelState] is cleared or not. */
  public val isCleared: Boolean
    get() = viewModelState == ViewModelState.CLEARED

  /**
   * Returns the current state of the [ViewModel] lifecycle.
   * [ViewModelLifecycle] has only two lifecycle states; [Lifecycle.Event.ON_START] and
   * [Lifecycle.Event.ON_DESTROY], so make sure don't depend other lifecycle states.
   */
  override fun getCurrentState(): State = state

  /**
   * Adds a [LifecycleObserver] that will be notified when the [ViewModelLifecycle] changes state.
   *
   * Highly recommend observing lifecycle states with [ViewModelLifecycleObserver].
   *
   * If you use other [LifecycleObserver] such as [DefaultLifecycleObserver] or [LifecycleEventObserver],
   * it'll observe only two two lifecycle states; [Lifecycle.Event.ON_START] and
   * [Lifecycle.Event.ON_DESTROY].
   *
   * @param observer A new [LifecycleObserver] that will be notified when the [ViewModelLifecycle] changes state.
   */
  @MainThread
  override fun addObserver(observer: LifecycleObserver) {
    enforceMainThreadIfNeeded("addObserver")
    if (viewModelState == ViewModelState.CLEARED) return
    val statefulObserver = ObserverWithState(observer, state)
    val previous = observerMap.putIfAbsent(observer, statefulObserver)
    if (previous != null) return
    val lifecycleOwner = lifecycleOwner.get() ?: return
    statefulObserver.dispatchEvent(lifecycleOwner, state.toEvent())
  }

  /**
   * Adds a [ViewModelLifecycleObserver] that will be notified when the [ViewModelLifecycle] changes state.
   *
   * @param onStateChanged A receiver that has [ViewModelState].
   */
  public fun addViewModelLifecycleObserver(
    onStateChanged: (viewModelState: ViewModelState) -> Unit
  ) {
    addObserver(ViewModelLifecycleObserver(onStateChanged))
  }

  /**
   * Adds a [ViewModelLifecycleObserver] that will be notified when the [ViewModelLifecycle] changes state.
   *
   * @param onInitialized A receiver that will be invoked when the ViewModel is initialized.
   * @param onCleared A receiver that will be invoked when the ViewModel is cleared.
   */
  public fun addViewModelLifecycleObserver(
    onInitialized: (owner: LifecycleOwner) -> Unit,
    onCleared: (owner: LifecycleOwner) -> Unit,
  ) {
    addObserver(
      object : DefaultViewModelLifecycleObserver {
        override fun onInitialized(viewModelLifecycleOwner: ViewModelLifecycleOwner) =
          onInitialized.invoke(viewModelLifecycleOwner)

        override fun onCleared(viewModelLifecycleOwner: ViewModelLifecycleOwner) =
          onCleared.invoke(viewModelLifecycleOwner)
      }
    )
  }

  /**
   * Remove a [LifecycleObserver] from the internal observers list.
   *
   * @param observer The [LifecycleObserver] to be removed.
   */
  @MainThread
  override fun removeObserver(observer: LifecycleObserver) {
    enforceMainThreadIfNeeded("removeObserver")
    observerMap.remove(observer)
  }

  /** Returns the current count of the observers. */
  @MainThread
  public fun getObserverCount(): Int {
    enforceMainThreadIfNeeded("getObserverCount")
    return observerMap.size
  }

  /** Sets the current state and notifies a new target state to all observers.  */
  @MainThread
  internal fun handleLifecycleEvent(event: Event) {
    enforceMainThreadIfNeeded("handleLifecycleEvent")

    if (viewModelState == ViewModelState.CLEARED) return

    val lifecycleOwner = lifecycleOwner.get()
      ?: throw IllegalStateException(
        "LifecycleOwner of this ViewModelLifecycle is already garbage collected. " +
          "It is too late to change lifecycle state."
      )

    // sets a new target state
    state = event.targetState

    // dispatches a new target event to all observers
    observerMap.entries.forEach {
      it.value.dispatchEvent(lifecycleOwner, event)
    }
  }

  private fun enforceMainThreadIfNeeded(methodName: String) {
    if (isEnforceMainThread) {
      val archTaskExecutorMethod = ArchTaskExecutor::class.java.getDeclaredMethod("getInstance")
      val archTaskExecutor = archTaskExecutorMethod.invoke(null) as? ArchTaskExecutor
      val isMainThreadMethod = ArchTaskExecutor::class.java.getDeclaredMethod("isMainThread")
      val isMainThread = isMainThreadMethod.invoke(archTaskExecutor) as? Boolean ?: false
      if (!isMainThread) {
        throw IllegalStateException("Method $methodName must be called on the main thread")
      }
    }
  }

  /** A class that have [LifecycleObserver] and [Lifecycle.State]. */
  private data class ObserverWithState constructor(
    private val lifecycleObserver: LifecycleObserver,
    private var lifecycleState: State,
  ) {

    /** dispatches a new event to observers. */
    fun dispatchEvent(lifecycleOwner: LifecycleOwner, event: Event) {
      val viewModelState = event.toViewModelState()
      if (lifecycleObserver is FullViewModelLifecycleObserver) {
        handleFullViewModelLifecycleObserver(
          fullViewModelLifecycleObserver = lifecycleObserver,
          viewModelLifecycleOwner = lifecycleOwner as ViewModelLifecycleOwner,
          viewModelState = viewModelState
        )
      } else if (lifecycleObserver is ViewModelLifecycleObserver) {
        lifecycleObserver.onStateChanged(viewModelState)
      } else {
        val observerMethod =
          Lifecycling::class.java.getDeclaredMethod("lifecycleEventObserver", Any::class.java)
        observerMethod.isAccessible = true
        val observer = observerMethod.invoke(null, lifecycleObserver) as? LifecycleEventObserver
        if (observer != null) {
          val newState = event.targetState
          lifecycleState = min(lifecycleState, newState)
          observer.onStateChanged(lifecycleOwner, event)
          lifecycleState = newState
        }
      }
    }

    /** handles [FullViewModelLifecycleObserver]. */
    private fun handleFullViewModelLifecycleObserver(
      fullViewModelLifecycleObserver: FullViewModelLifecycleObserver,
      viewModelLifecycleOwner: ViewModelLifecycleOwner,
      viewModelState: ViewModelState,
    ) {
      when (viewModelState) {
        ViewModelState.INITIALIZED -> fullViewModelLifecycleObserver.onInitialized(
          viewModelLifecycleOwner
        )
        ViewModelState.CLEARED -> fullViewModelLifecycleObserver.onCleared(viewModelLifecycleOwner)
      }
    }

    private fun min(state1: State, state2: State): State {
      return if (state2 < state1) state2 else state1
    }
  }
}
