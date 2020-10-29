package com.xincheng.tracker.lifecycle

import androidx.fragment.app.Fragment

/**
 * 用于监听[Fragment]的可见性
 */
interface ITrackerFragmentVisible {
  /**
   * 在Fragment中的setUserVisibleHint()和onHidden()方法被调用时，同步调用该方法
   * 以便于能够正确的观察到Fragment状态的变化
   */
  fun onFragmentVisibilityChanged(visible: Boolean, f: Fragment?)
}