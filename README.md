# 埋点

## 使用

```groovy
implementation 'com.android.lib.tracker:tracker:0.0.8-SNAPSHOT'
```

## 初始化

```kotlin
 fun initTracker(){
        // 设定一些通用的属性，这些属性在每次统计事件中都会附带
        // 注意：如果此处的属性名与内置属性的名称相同，则内置属性会被覆盖
        Tracker.addProperty(CHANNELID, "")
        Tracker.addProperty(SESSIONID, UUID.randomUUID().toString())
        Tracker.addProperty(DEVICEID, DeviceUtil.generateDeviceId(this))
        Tracker.addProperty(ACCOUNTID, "0")
//        Tracker.addProperty("longitude", "")
//        Tracker.addProperty("latitude", "")
        // 设定上报数据的主机和接口
        // 注意：该方法一定要在Tracker.initialize()方法前调用
        // 否则会由于上报地址未初始化，在触发启动事件时导致崩溃
        Tracker.setService(getTrackerHost(), "/goblin/track/action/v1", "/goblin/track/action/batch/v1")
        // 设定上报数据的项目名称
        Tracker.setProjectName("辛选精灵")
        //设置批量上传时间间隔, 单位s
        Tracker.setReportInterval(5 * 60)
        // 设定上报数据的模式
        Tracker.setMode(TrackerMode.RELEASE)
        // 初始化AndroidTracker
        Tracker.initialize(this)
    }
```

## 配置

在基类中实现如下接口

```kotlin
open class BaseActivity : AppCompatActivity(), ITrackerHelper, ITrackerIgnore, ITrack {
  ///////////////////////////////////////////////////////////////////////////
  // 该类实现ITrackerHelper接口，此处两个方法全部返回null
  // 则页面名称（别名）会直接取使用canonicalName来当做标题
  // 并且不会有附加的属性
  ///////////////////////////////////////////////////////////////////////////
  override fun getTrackName(context: Context): String? = null

  override fun getTrackProperties(context: Context): Map<String, Any?>? = null

  ///////////////////////////////////////////////////////////////////////////
  // ITrackerIgnore接口用于确定当前Activity中是否包含Fragment
  // 如果返回值为true，则表明当前Activity中有包含Fragment，则此时不会对Activity进行统计
  // 如果返回值为false，则表明当前Activity中不包含Fragment，则此时会对Activity进行统计
  // 此处默认不包含Fragment，如有需要应该在子类中覆写该方法并修改返回值
  ///////////////////////////////////////////////////////////////////////////
  override fun isIgnored(): Boolean = false
  ///////////////////////////////////////////////////////////////////////////
  //track 规则，必须实现此接口才有效统计页面，以及有效的路径,可以有效避免三方库的一些页面被记录页面跳转路径
  ///////////////////////////////////////////////////////////////////////////
   override fun track(): Boolean {
    return super.track()
  }
}
```



```kotlin
open class BaseFragment : Fragment(), ITrackerHelper, ITrackerIgnore {

  ///////////////////////////////////////////////////////////////////////////
  // Tracker.setUserVisibleHint()和Tracker.onHiddenChanged()方法用于同步Fragment
  // 的可见性，解决在Fragment显隐/与ViewPager结合使用时无法触发生命周期的问题
  ///////////////////////////////////////////////////////////////////////////
  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    Tracker.setUserVisibleHint(this, isVisibleToUser)
  }

  override fun onHiddenChanged(hidden: Boolean) {
    super.onHiddenChanged(hidden)
    Tracker.onHiddenChanged(this, hidden)
  }

  ///////////////////////////////////////////////////////////////////////////
  // 该类实现ITrackerHelper接口，此处两个方法全部返回null
  // 则页面名称（别名）会直接取使用canonicalName来当做标题
  // 并且不会有附加的属性
  ///////////////////////////////////////////////////////////////////////////
  override fun getTrackName(context: Context): String? = null

  override fun getTrackProperties(context: Context): Map<String, Any?>? = null

  ///////////////////////////////////////////////////////////////////////////
  // ITrackerIgnore接口用于确定当前Fragment中是否包含子Fragment
  // 如果返回值为true，则表明当前Fragment中有包含子Fragment，则此时不会对当前Fragment进行统计
  // 如果返回值为false，则表明当前Fragment中不包含子Fragment，则此时会对当前Fragment进行统计
  // 此处默认不包含子Fragment，如有需要应该在子类中覆写该方法并修改返回值
  ///////////////////////////////////////////////////////////////////////////
  override fun isIgnored(): Boolean = false
}
```



## 示例

配置页面属性

```kotlin
override fun getTrackName(context: Context): String? {
    return "C.00002.0.0." + System.currentTimeMillis()
}

override fun getTrackProperties(context: Context): Map<String, Any?>? {
    return mapOf(EVENTID to "C201110002")
}
```

采集点击事件

```kotlin
Tracker.trackView(v, mapOf(EVENTID to "C201120005"))
```

自定义事件

```kotlin
Tracker.trackEvent("2", mapOf(EVENTID to "C201120003"))
```



## 说明

1. 目前RELEASE模式下，上传会检测eventId，其他模式可以直接上传，其他可以参考代码说明
2. RELEASE模式下，位置曝光事件是批量上传，其他事件单次上传