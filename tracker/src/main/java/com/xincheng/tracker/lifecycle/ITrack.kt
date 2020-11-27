package com.xincheng.tracker.lifecycle
/**
* @author zhengy
* 版本：1.0
* 创建时间：2020/11/27 11:27 AM
* description: track 规则，必须实现此接口才有效统计页面，以及有效的路径,可以有效避免三方库的一些页面被记录页面跳转路径
*/
interface ITrack {

    fun track(): Boolean = true
}