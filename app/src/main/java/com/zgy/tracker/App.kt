package com.zgy.tracker

import android.app.Application
import com.xincheng.tracker.Tracker
import com.xincheng.tracker.data.TrackerMode
import com.xincheng.tracker.lifecycle.ITrackerContext
import com.xincheng.tracker.utils.getUUID

class App: Application(), ITrackerContext{
    override fun onCreate() {
        super.onCreate()
        // 设定一些通用的属性，这些属性在每次统计事件中都会附带
        // 注意：如果此处的属性名与内置属性的名称相同，则内置属性会被覆盖
        Tracker.addProperty("channelId", "")
        Tracker.addProperty("sessionId", getUUID()+ System.currentTimeMillis())
        Tracker.addProperty("longitude", "")
        Tracker.addProperty("latitude", "")
        // 设定上报数据的主机和接口
        // 注意：该方法一定要在Tracker.initialize()方法前调用
        // 否则会由于上报地址未初始化，在触发启动事件时导致崩溃
//    Tracker.setService(BuildConfig.SERVICE_HOST, BuildConfig.SERVICE_PATH)
        Tracker.setService("https://dev-goblin.xinc818.com", "/goblin/track/action/v1", "/goblin/track/action/batch/v1")
        // 设定上报数据的项目名称
        Tracker.setProjectName("辛选精灵")
        //设置批量上传时间间隔, 单位s
        Tracker.setReportInterval(10L)
        // 设定上报数据的模式
        Tracker.setMode(TrackerMode.RELEASE)
        // 初始化AndroidTracker
        Tracker.initialize(this)

    }
}