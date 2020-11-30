package com.xincheng.tracker.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.xincheng.tracker.BuildConfig
import com.xincheng.tracker.data.*
import com.xincheng.tracker.data.DEVICEID
import com.xincheng.tracker.data.DISTINCT_ID
import com.xincheng.tracker.data.PLATFORM
import com.xincheng.tracker.data.TrackerMNC
import com.xincheng.tracker.data.TrackerNetworkType
import java.util.*
import kotlin.collections.HashMap


internal val buildInObject: HashMap<String, Any> = HashMap()
internal val buildInLib: HashMap<String, Any> = HashMap()
internal val buildInProperties: HashMap<String, String> = HashMap()

internal var buildInUUID = ""

// 用于标识是否已经登录
internal var isLogin = false

/**
 * 获取内置属性
 */
internal fun initBuildInProperties(context: Context) {
    buildInUUID = context.getUUID()
//    if (!isLogin) {
//        buildInObject.put(DISTINCT_ID, buildInUUID)
//    }

//    buildInLib.put("lib", "Android")
//    buildInLib.put("lib_version", BuildConfig.VERSION_NAME)
//    buildInLib.put("app_version", context.getVersionName())
//
//    buildInProperties.put("lib", "Android")
//    buildInProperties.put("lib_version", BuildConfig.VERSION_NAME)
//    buildInProperties.put("app_version", context.getVersionName())
//    buildInProperties.put("manufacturer", Build.BRAND)
//    buildInProperties.put("model", Build.MODEL)
//    buildInProperties.put("os", "Android")
//    buildInProperties.put("os_version", Build.VERSION.RELEASE)
//    buildInProperties.put("os_version", Build.VERSION.RELEASE)
//    buildInProperties.put("screen_height", context.getScreenWidth())
//    buildInProperties.put("screen_width", context.getScreenHeight())
//    buildInProperties.put("carrier", context.getMNC().desc())
//    buildInProperties.put("imeicode", context.getIMEI())
//    buildInProperties.put("device_id", context.getAndroidId())

//    buildInProperties[ACCOUNTID] = "0" //公共字段，外部设置
    buildInProperties[DEVICEID] = context.getAndroidId() //可以外部覆盖
    buildInProperties[PLATFORM] = "3"
    buildInProperties[CLIENTINFO] = "|${Build.BRAND}|${Build.MODEL}"
    buildInProperties[OS] = Build.VERSION.RELEASE
    buildInProperties[VERSIONNAME] = context.getVersionName()
//    buildInProperties[CHANNELID] = "" //公共字段，外部设置
    buildInProperties[CLIENTTIME] = System.currentTimeMillis().toString()
    buildInProperties[NETWORK] = getNetWork(context).toString()
//    buildInProperties[LONGITUDE] = "" //公共字段，外部设置
//    buildInProperties[LATITUDE] = ""
//    buildInProperties[SESSIONID] = buildInUUID//公共字段，外部设置

//    buildInProperties[URL] = ""
//    buildInProperties[REF] = ""


}

internal fun login(userId: String) {
    buildInObject.put(ACCOUNTID, userId)
    isLogin = true
}

internal fun logout() {
    buildInObject.put(ACCOUNTID, "0")
    isLogin = false
}

/**
 * 获取当前app的版本名称
 */
private fun Context.getVersionName(): String {
    var versionName = ""
    try {
        val packageInfo =
            packageManager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS)
        versionName = packageInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
    }
    return versionName
}


/**
 * 使用上下文对象获取当前手机屏幕宽度
 */
private fun Context.getScreenWidth(): Int {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val metrics = DisplayMetrics()
    display.getMetrics(metrics)
    return metrics.widthPixels
}

/**
 * 使用上下文对象获取当前手机屏幕高度
 */
private fun Context.getScreenHeight(): Int {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val metrics = DisplayMetrics()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        display.getRealMetrics(metrics)
    } else {
        display.getMetrics(metrics)
    }
    return metrics.heightPixels
}

@SuppressLint("MissingPermission")
/**
 * 判断当前网络状态是否为WiFi
 */
internal fun Context.isWiFi(): Boolean {
    return if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val connectivityManager = this.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        // 执行到此处时，已有权限，忽略该警告
        val activeNetInfo = connectivityManager.activeNetworkInfo
        activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI
    } else {
        false
    }
}

private fun Context.getMNC(): TrackerMNC {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val telManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val operator = telManager.networkOperator
        //        int mcc = Integer.parseInt(operator.substring(0, 3));//移动国家代码（中国的为460）；
        try {//防止启动就崩溃，这里加上异常处理
            val mnc = Integer.parseInt(operator.substring(3))//，移动网络号码（中国移动为0,2，中国联通为1，中国电信为3）；
            when (mnc) {
                0 -> return TrackerMNC.CMCC
                1 -> return TrackerMNC.CUCC
                2 //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号 //中国移动
                -> return TrackerMNC.CMCC
                3 -> return TrackerMNC.CTCC
                11//在电信4g的情况下返回46011
                -> return TrackerMNC.CTCC
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    return TrackerMNC.OTHER
}

/**
 * 根据[Context]来获取网络类型
 *
 * 需要 android.permission.ACCESS_NETWORK_STATE 权限
 *
 * @return [NetType]
 */
internal fun Context.getNetworkType(): TrackerNetworkType {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        if (isWiFi()) {
            return TrackerNetworkType.WIFI
        }

        if (!isNetworkAvailable()) {
            return TrackerNetworkType.NO_NET
        }

        //下面类型的判断不包含WIFI，如果是wifi类型会返回 UNKNOWN
        val telManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkType = telManager.networkType
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> TrackerNetworkType.G5  //5G,目前大部分5G信号都是痛殴4G基站发射，所以大多数情况这个地方返回的还是4G的type
            TelephonyManager.NETWORK_TYPE_LTE  // 4G
                , TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_EHRPD -> TrackerNetworkType.G4
            TelephonyManager.NETWORK_TYPE_UMTS // 3G
                , TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B, 17//隐藏API
            -> TrackerNetworkType.G3
            TelephonyManager.NETWORK_TYPE_GPRS // 2G
                , TelephonyManager.NETWORK_TYPE_EDGE, 16 -> TrackerNetworkType.G2
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> TrackerNetworkType.UNKNOWN
            else -> TrackerNetworkType.NO_DEAL
        }
    } else {
        return TrackerNetworkType.UNKNOWN
    }
}

/**
 * 获取network 枚举值
 */
internal fun getNetWork(context: Context): Int {
    val type = context.getNetworkType()
    val mnc = context.getMNC()
    when (type) {
        TrackerNetworkType.WIFI -> return 1
        TrackerNetworkType.G5 -> {
            return when (mnc) {
                TrackerMNC.CMCC -> 11
                TrackerMNC.CUCC -> 12
                TrackerMNC.CTCC -> 13
                else -> 0
            }
        }
        TrackerNetworkType.G4 -> {
            return when (mnc) {
                TrackerMNC.CMCC -> 2
                TrackerMNC.CUCC -> 5
                TrackerMNC.CTCC -> 8
                else -> 0
            }
        }
        TrackerNetworkType.G3 -> {
            return when (mnc) {
                TrackerMNC.CMCC -> 3
                TrackerMNC.CUCC -> 4
                TrackerMNC.CTCC -> 9
                else -> 0
            }
        }
        TrackerNetworkType.G2 -> {
            return when (mnc) {
                TrackerMNC.CMCC -> 4
                TrackerMNC.CUCC -> 7
                TrackerMNC.CTCC -> 10
                else -> 0
            }
        }
        else -> return 0
    }

}

@SuppressLint("MissingPermission")
/**
 * 当前是否连接网络
 *
 * 需要 android.permission.ACCESS_NETWORK_STATE 权限
 *
 * @return true联网，false没有联网
 */
private fun Context.isNetworkAvailable(): Boolean {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val connectivityManager = this.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        // 执行到此处时，已有权限，忽略该警告
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }
    // 默认网络可用
    return true
}

@SuppressLint("HardwareIds")
private fun Context.getAndroidId(): String {
    return android.provider.Settings.Secure.getString(
        contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
    ) ?: ""
}

public fun Context.getUUID(): String =
    (Build.BRAND + Build.MODEL).hashCode().toString() + getAndroidId()

@SuppressLint("MissingPermission")
/**
 * 获取设备Id(IMEI)
 *
 * @param context 上下文对象
 * @return 设备Id
 */
private fun Context.getIMEI(): String {
    var deviceId: String? = null
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        // 如果有权限才获取设备id,防止崩溃
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tm.imei
        } else {
            // 执行到此处时，已有权限，忽略该警告
            @Suppress("DEPRECATION")
            tm.deviceId
        }
    }
    return deviceId ?: ""
}

/**
 * 获取设备唯一Id的一种方式
 */
private fun Context.generateId(): String {
    var deviceId: String? = null
    val videoVineUuid = UUID(-0x2342352352345L, -0x32532523525235L)
    try {
        val wvDrm = MediaDrm(videoVineUuid)
        val videoVineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
        deviceId = String((videoVineId)).encodeBASE64()

    } catch (e: UnsupportedSchemeException) {
        e.printStackTrace()
    }
    return deviceId ?: ""
}