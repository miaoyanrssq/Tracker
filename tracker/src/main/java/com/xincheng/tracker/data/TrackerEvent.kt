package com.xincheng.tracker.data

import com.xincheng.tracker.Tracker
import com.google.gson.annotations.SerializedName
import com.xincheng.tracker.utils.*
import com.xincheng.tracker.utils.buildInLib
import com.xincheng.tracker.utils.buildInObject
import com.xincheng.tracker.utils.buildInProperties

/**
 * 统计事件
 */
data class TrackerEvent(
    @SerializedName("event")
    @EventType private var event: String
) {

    @SerializedName("properties")
    private var properties = HashMap<String, Any>()

    @SerializedName("time")
    internal var time = System.currentTimeMillis()

    @SerializedName("screenName")
    private var screenName = Tracker.screenName

    @SerializedName("screenClass")
    private var screenClass = Tracker.screenClass

    @SerializedName("screenTitle")
    private var screenTitle = Tracker.screenTitle

    @SerializedName("referer")
    private var referer = Tracker.referer

    @SerializedName("refererClass")
    private var refererClass = Tracker.refererClass

    @SerializedName("parent")
    private var parent = Tracker.parent

    @SerializedName("parentClass")
    private var parentClass = Tracker.parentClass

    init {
        Tracker.additionalProperties.filter { it.value != null }.forEach {
            this@TrackerEvent.properties[it.key] = it.value!!
        }
    }

    fun addProperties(properties: Map<String, Any?>?) {
        if (properties == null) {
            return
        }
        properties.filter { it.value != null }.forEach {
            this@TrackerEvent.properties[it.key] = it.value!!
        }
    }

//  fun build(): Map<String, Any> {
//    val o = HashMap<String, Any>()
//    o.putAll(buildInObject)
//    o[EVENT] = event
//    o[TIME] = time
//
//    o[LIB] = buildInLib
//    val properties = HashMap<String, Any>()
//    properties.putAll(buildInProperties)
//    properties[SCREEN_NAME] = screenName
//    properties[SCREEN_CLASS] = screenClass
//    properties[TITLE] = screenTitle
//    properties[REFERER] = referer
//    properties[REFERER_CLASS] = refererClass
//    properties[PARENT] = parent
//    properties[PARENT_CLASS] = parentClass
//    Tracker.trackContext.let {
//      properties[NETWORK_TYPE] = it.getApplicationContext().getNetworkType().desc()
//      properties.put(WIFI, it.getApplicationContext().isWiFi())
//    }
//    Tracker.channelId?.let {
//      properties.put(CHANNEL, it)
//    }
//    this@TrackerEvent.properties.let {
//      properties.putAll(it)
//    }
//
//    o[PROPERTIES] = properties
//    return o
//  }

    fun build(): Map<String, String?> {
        val map = mutableMapOf<String, String?>(
            EID to event,
            XTP to screenName,
            XTPREF to referer
        )
        map.putAll(buildInProperties)
        this@TrackerEvent.properties.forEach {
            if(it.value is String){
                map[it.key] = it.value as String
            }
        }
        return map
    }


    fun toPrettyJson(): String {
        return PRETTY_GSON.toJson(build())
    }
}