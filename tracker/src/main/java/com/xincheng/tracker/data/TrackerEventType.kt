package com.xincheng.tracker.data

import androidx.annotation.StringDef

/** 页面浏览事件 */
const val VIEW_SCREEN = "AppViewScreen"  // 0
/** 点击事件 */
const val CLICK = "AppClick"   // 2
/** APP启动（切换到前台）事件 */
const val APP_START = "AppStart"
/** APP关闭（切换到后台）事件 */
const val APP_END = "AppEnd"
/** APP统计事件（用于自定义） */
const val APP_TRACK = "track"

const val VIEW_EXPOSURE = "viewExposure" // 1

@StringDef(VIEW_SCREEN, CLICK, APP_START, APP_END, VIEW_EXPOSURE)
@Retention(AnnotationRetention.SOURCE) internal annotation class EventType
