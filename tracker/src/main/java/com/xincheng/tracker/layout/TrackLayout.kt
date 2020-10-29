package com.xincheng.tracker.layout

import android.content.Context
import android.graphics.Rect
import android.util.ArrayMap
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import com.xincheng.tracker.R
import com.xincheng.tracker.exposure.ExposureManager
import com.xincheng.tracker.exposure.ExposureModel
import com.xincheng.tracker.exposure.ReuseLayoutHook
import com.xincheng.tracker.exposure.TrackerInternalConstants
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * 统计用的Layout
 */
class TrackLayout : FrameLayout, GestureDetector.OnGestureListener {
  private val rect = Rect()
  private var clickFunc: ((View, MotionEvent, Long) -> Unit)? = null
  private var itemClickFunc: ((AdapterView<*>, View, Int, Long, MotionEvent, Long) -> Unit)? = null

  private val listenerInfoField by lazy {
    // 通过反射拿到mListenerInfo，并且设置为可访问（用于后续替换点击事件）
    val declaredField = View::class.java.getDeclaredField("mListenerInfo")
    declaredField.isAccessible = true
    return@lazy declaredField
  }

  /**
   * Custom threshold is used to determine whether it is a click event,
   * When the user moves more than 20 pixels in screen, it is considered as the scrolling event instead of a click.
   */
  private val CLICK_LIMIT = 20f

  /**
   * the X Position
   */
  private var mOriX = 0f

  /**
   * the Y Position
   */
  private var mOriY = 0f

  private var mGestureDetector: GestureDetector? = null

  private var mReuseLayoutHook: ReuseLayoutHook? = null

  /**
   * common info attached with the view inside page
   */
  var commonInfo =
    HashMap<String, Any>()

  /**
   * all the visible views inside page, key is viewName
   */
  private val lastVisibleViewMap: Map<String, ExposureModel> =
    ArrayMap<String, ExposureModel>()

  private var lastOnLayoutSystemTimeMillis: Long = 0


  constructor(context: Context) : super(context){
    initView()
  }
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
    initView()
  }
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr){
    initView()
  }

  private fun initView(){
    mGestureDetector = GestureDetector(context, this)
    mReuseLayoutHook = ReuseLayoutHook(this, commonInfo)
  }


  internal fun registerClickFunc(func: ((View, MotionEvent, Long) -> Unit)) {
    this@TrackLayout.clickFunc = func
  }

  internal fun registerItemClickFunc(
      func: ((AdapterView<*>, View, Int, Long, MotionEvent, Long) -> Unit)) {
    this@TrackLayout.itemClickFunc = func
  }

  /**
   * 根据当前坐标值查找对应位置的View
   *
   * 该方法为递归实现，如果传入的布局为[ViewGroup]，则执行递归；否则，则对[View]进行判断。
   * 在递归过程中，如果发现[ViewGroup]中没有合适的[View]，则会对[ViewGroup]本身进行判断，
   * 如果[ViewGroup]本身可点击，则会将[ViewGroup]当做点击的[View]
   */
  private fun findHitView(parent: View, x: Int, y: Int): ArrayList<View> {
    val hitViews = ArrayList<View>()
    if (parent.isVisible() && parent.hitPoint(x, y)) {
      // 仅在parent可见，并且命中了点击位置时才对该parent进行判断/递归查找，减少查找的次数，提高效率
      if (parent is AdapterView<*>) {
        hitViews.add(parent)
        // 由于在AdapterView中可能会有局部的View可点击的情况，故此处需要对AdapterView中的子View进行递归查询
        // 如果子View可点击，则只会触发子View的点击，而不会触发AdapterView的点击
        findHitViewsInGroup(parent, x, y, hitViews)
      } else if (parent is ViewGroup) {
        // 如果是ViewGroup，则去对其子View进行查询
        findHitViewsInGroup(parent, x, y, hitViews)
      } else if (parent !is ViewGroup && parent.isClickable) {
        // 如果parent本身不是ViewGroup，且可点击，则当做可触发点击事件的View返回
        hitViews.add(parent)
      }
    }
    return hitViews
  }

  /**
   * 对ViewGroup中的所有子View进行查询，如果子View中没有符合条件的View
   * 则会对父View进行检查，如果父View可点击，则List中会包含父View
   */
  private fun findHitViewsInGroup(parent: ViewGroup, x: Int, y: Int,
      hitViews: ArrayList<View>) {
    val childCount = parent.childCount
    for (i in 0 until childCount) {
      val child = parent.getChildAt(i)
      val hitChildren = findHitView(child, x, y)
      if (!hitChildren.isEmpty()) {
        hitViews.addAll(hitChildren)
      } else if (child.isVisible() && child.isClickable && child.hitPoint(x, y)) {
        hitViews.add(child)
      }
    }
  }

  private fun View.isVisible(): Boolean = this.visibility == View.VISIBLE

  /**
   * 判断一个View是否包含了对应的坐标
   */
  private fun View.hitPoint(x: Int, y: Int): Boolean {
    this.getGlobalVisibleRect(rect)
    return rect.contains(x, y)
  }

  /**
   * 对View的点击事件进行包装，便于增加统计代码
   */
  private fun wrapClick(view: View, ev: MotionEvent) {
    if (view.hasOnClickListeners()) {
      val viewInfo = listenerInfoField.get(view)
      val clickInfo = viewInfo?.javaClass?.getDeclaredField("mOnClickListener")
      val source = clickInfo?.get(viewInfo) as? OnClickListener
      source?.let {
        // 如果source已经是ClickWrapper则不需继续处理
        if (it !is ClickWrapper) {
          // 如果source不是ClickWrapper，则首先尝试复用原先已有的ClickWrapper（可能在RecyclerView中对View重新设置了OnClickListener，但是其ClickWrapper对象还在）
          var wrapper = view.getTag(R.id.android_tracker_click_listener)
          if (wrapper is ClickWrapper) {
            // 如果原先已存在ClickWrapper
            // 则对比原先ClickWrapper中的OnClickListener是否与source为同一个实例
            if (wrapper.source != source) {
              wrapper.source = source
            }
          } else {
            // 如果原先不存在ClickWrapper，则创建ClickWrapper
            wrapper = ClickWrapper(source, ev)
            view.setTag(R.id.android_tracker_click_listener, wrapper)
          }
          clickInfo.let {
            it.isAccessible = true
            it.set(viewInfo, wrapper)
          }
        }
      }
    }
  }

  /**
   * 对AdapterView条目的点击监听进行包装,便于增加统计代码
   */
  private fun wrapItemClick(view: AdapterView<*>, ev: MotionEvent) {
    val source = view.onItemClickListener
    source?.let {
      if (source !is ItemClickWrapper) {
        // 如果原先设置的监听不为ItemClickWrapper类型，则对source进行包装
        // 如果已经为ItemClickWrapper，则直接复用原先监听即可，不需要再次包装
        view.onItemClickListener = ItemClickWrapper(source, ev)
      }
    }
  }

  /**
   * [View.OnClickListener]的包装类，内部包装了View的原[View.OnClickListener]，并且增加了点击统计
   *
   * @param source View的原[View.OnClickListener]
   * @param ev 触发点击时的坐标位置
   */
  private inner class ClickWrapper(var source: OnClickListener?,
      val ev: MotionEvent) : OnClickListener {
    override fun onClick(view: View) {
      source?.let {
        // 由于clickFunc的执行是在实际的onClick()方法后，故在统计时可能会有延时
        // 此处首先记录onClick()的触发时间，在统计时对时间进行纠正，这样数据在入库时
        // 会记录正确的时间
        // 但是在日志中查看和上报过程中，可能还是会有滞后
        val time = System.currentTimeMillis()
        source?.onClick(view)
        clickFunc?.invoke(view, ev, time)
      }

    }
  }

  /**
   * [AdapterView.OnItemClickListener]的包装类，内部包装了原监听器，并且增加了点击统计
   */
  private inner class ItemClickWrapper(val source: AdapterView.OnItemClickListener,
      val ev: MotionEvent) : AdapterView.OnItemClickListener {
    override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
      val time = System.currentTimeMillis()
      source.onItemClick(adapterView, view, position, id)
      itemClickFunc?.invoke(adapterView, view, position, id, ev, time)
    }
  }


  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    mGestureDetector!!.onTouchEvent(ev)


    when(ev.action){
      MotionEvent.ACTION_DOWN ->{
        mOriX = ev.x
        mOriY = ev.y
        val hitViews = findHitView(rootView, ev.x.toInt(), ev.y.toInt())
        hitViews.forEach {
          if (it is AdapterView<*>) {
            wrapItemClick(it, ev)
          } else {
            wrapClick(it, ev)
          }
        }
      }
      MotionEvent.ACTION_MOVE ->{
        if((abs(ev.x - mOriX) > CLICK_LIMIT) || (abs(ev.y - mOriY) > CLICK_LIMIT)){
          val time = System.currentTimeMillis()
          ExposureManager.getInstance().triggerViewCalculate(
            TrackerInternalConstants.TRIGGER_VIEW_CHANGED,
            this,
            commonInfo,
            lastVisibleViewMap
          )

        }
      }

    }

    return super.dispatchTouchEvent(ev)

  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    // duplicate message in 1s
    val time = System.currentTimeMillis()
    if (time - lastOnLayoutSystemTimeMillis > 1000) {
      lastOnLayoutSystemTimeMillis = time
      ExposureManager.getInstance().traverseViewTree(this, mReuseLayoutHook)
    }
    Log.v("Tag","onLayout traverseViewTree end costTime=" + (System.currentTimeMillis() - time))
    super.onLayout(changed, left, top, right, bottom)
  }

  override fun onShowPress(e: MotionEvent?) {
  }

  override fun onSingleTapUp(e: MotionEvent?): Boolean {
    return false
  }

  override fun onDown(e: MotionEvent?): Boolean {
    return false
  }

  override fun onFling(
    e1: MotionEvent?,
    e2: MotionEvent?,
    velocityX: Float,
    velocityY: Float
  ): Boolean {
    val time = System.currentTimeMillis()
    postDelayed({
      ExposureManager.getInstance().triggerViewCalculate(
        TrackerInternalConstants.TRIGGER_VIEW_CHANGED,
        this@TrackLayout,
        commonInfo,
        lastVisibleViewMap
      )
    }, 1000)
    return false
  }

  override fun onScroll(
    e1: MotionEvent?,
    e2: MotionEvent?,
    distanceX: Float,
    distanceY: Float
  ): Boolean {
    return false
  }

  override fun onLongPress(e: MotionEvent?) {
  }

  /**
   * the state change of window trigger the exposure event.
   * Scene 3: switch back and forth when press Home button.
   * Scene 4: enter into the next page
   * Scene 5: window replace
   *
   * @param hasFocus
   */
  override fun dispatchWindowFocusChanged(hasFocus: Boolean) {
    val ts = System.currentTimeMillis()
    ExposureManager.getInstance().triggerViewCalculate(
      TrackerInternalConstants.TRIGGER_WINDOW_CHANGED,
      this,
      commonInfo,
      lastVisibleViewMap
    )
    super.dispatchWindowFocusChanged(hasFocus)
  }

  override fun dispatchVisibilityChanged(changedView: View, visibility: Int) {
    // Scene 6: switch page in the TabActivity
    if (visibility == View.GONE) {
      val ts = System.currentTimeMillis()
      ExposureManager.getInstance().triggerViewCalculate(
        TrackerInternalConstants.TRIGGER_WINDOW_CHANGED,
        this,
        commonInfo,
        lastVisibleViewMap
      )
    } else {
    }
    super.dispatchVisibilityChanged(changedView, visibility)
  }

}