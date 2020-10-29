package com.zgy.tracker

import android.app.Application
import com.xincheng.tracker.Tracker
import com.xincheng.tracker.data.TrackerMode
import com.xincheng.tracker.lifecycle.ITrackerContext

class App: Application(), ITrackerContext{
    override fun onCreate() {
        super.onCreate()
        // 设定一些通用的属性，这些属性在每次统计事件中都会附带
        // 注意：如果此处的属性名与内置属性的名称相同，则内置属性会被覆盖
        Tracker.addProperty("附加的属性1", "附加的属性1")
        Tracker.addProperty("附加的属性2", "附加的属性2")
        // 设定上报数据的主机和接口
        // 注意：该方法一定要在Tracker.initialize()方法前调用
        // 否则会由于上报地址未初始化，在触发启动事件时导致崩溃
//    Tracker.setService(BuildConfig.SERVICE_HOST, BuildConfig.SERVICE_PATH)
        Tracker.setService("https://www.demo.com", "report.php")
        // 设定上报数据的项目名称
        Tracker.setProjectName("projectName")
        // 设定上报数据的模式
        Tracker.setMode(TrackerMode.DEBUG_ONLY)
        // 初始化AndroidTracker
        Tracker.initialize(this)

    }
}