package com.xincheng.tracker.layout

import android.R
import android.app.Activity
import android.app.TabActivity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.xincheng.tracker.Tracker


fun wrap(activity: Activity) {
    val decorView = activity.window.decorView
    if (decorView != null && decorView is ViewGroup) {
        val trackLayout = TrackLayout(activity)
        trackLayout.registerClickFunc { view, ev, time ->
            Tracker.trackView(view, ev, time)
        }
        trackLayout.registerItemClickFunc { adapterView, view, position, id, ev, time ->
            Tracker.trackAdapterView(adapterView, view, position, id, ev, time)
        }
        decorView.addView(
            trackLayout,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        ViewCompat.setElevation(
            trackLayout,
            999F
        )// 提升布局层次，防止fragmentation等库由于侧滑返回添加的布局导致该布局被覆盖，从而导致点击统计失效
    }
}

fun attachTrackerFrameLayout(activity: Activity) {

    // this is a problem: several activity exist in the TabActivity
    if (activity == null || activity is TabActivity) {
        return
    }
    try {

        val decorView = activity.window.decorView
        if (decorView != null && decorView is ViewGroup) {

            if (decorView.childCount > 0) {
                val root = decorView.getChildAt(0)
                if (root !is TrackLayout) {
                    val trackLayout = TrackLayout(activity)

                    trackLayout.registerClickFunc { view, ev, time ->
                        Tracker.trackView(view, ev, time)
                    }
                    trackLayout.registerItemClickFunc { adapterView, view, position, id, ev, time ->
                        Tracker.trackAdapterView(adapterView, view, position, id, ev, time)
                    }

                    while (decorView.childCount > 0) {
                        val view = decorView.getChildAt(0)
                        decorView.removeViewAt(0)
                        trackLayout.addView(view, view.layoutParams)
                    }




                    decorView.addView(
                        trackLayout,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun detachTrackerFrameLayout(activity: Activity?) {
    if (activity == null || activity is TabActivity) {
        return
    }
    try {
        val container =
            activity.findViewById<View>(R.id.content) as ViewGroup
                ?: return
        if (container.getChildAt(0) is TrackLayout) {
            container.removeViewAt(0)
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}