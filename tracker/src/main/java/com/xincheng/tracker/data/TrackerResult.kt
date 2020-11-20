package com.xincheng.tracker.data

import com.xincheng.tracker.data.TrackerEvent
import retrofit2.Response
import java.util.*


data class TrackerResult(
    var code: Int?,
    var message: String?,
    var data: Any?
)