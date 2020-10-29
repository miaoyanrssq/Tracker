package com.xincheng.tracker.data

/**
 * 运营商枚举
 *
 */
internal enum class TrackerMNC {
  /** 其他  */
  OTHER {
    override fun desc(): String = "未知"
  },
  /** 中国移动  */
  CMCC {
    override fun desc(): String = "中国移动"
  },
  /** 中国联通  */
  CUCC {
    override fun desc(): String = "中国联通"
  },
  /** 中国电信  */
  CTCC {
    override fun desc(): String = "中国电信"
  };

  abstract fun desc(): String
}