package com.zhang.studynet.dao

import com.zhang.studynet.domain.CourseClickCount
import com.zhang.studynet.utils.HBaseUtils
import org.apache.hadoop.hbase.util.Bytes

import scala.collection.mutable.ListBuffer

/**
  * 实战课程点击数统计访问层
  */

object CourseClickCountDao {

  val tableName = "ns1:courses_clickcount" //表名
  val cf = "info" //列族
  val qualifer = "click_count" //列

  def save(list: ListBuffer[CourseClickCount]) = {
    val table = HBaseUtils.getInstance().getTable(tableName)
    for (item <- list) {
      table.incrementColumnValue(
        Bytes.toBytes(item.day_course),
        Bytes.toBytes(cf),
        Bytes.toBytes(qualifer),
        item.click_count)
    }
  }


}
