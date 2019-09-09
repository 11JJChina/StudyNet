package com.zhang.studynet.utils

import org.apache.commons.lang3.time.FastDateFormat

object DateUtils {

  //指定输入的日期格式
  val YYYYMMDDHMMSS_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss")
  //指定输出格式
  val TARGET_FORMAT = FastDateFormat.getInstance("yyyyMMddhhmmss")

  def getTime(time: String): Long = {
    YYYYMMDDHMMSS_FORMAT.parse(time).getTime
  }

  def parseToMinute(time: String) = {
    TARGET_FORMAT.format(getTime(time))
  }

}
