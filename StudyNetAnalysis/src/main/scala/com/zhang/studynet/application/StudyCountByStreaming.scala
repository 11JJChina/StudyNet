package com.zhang.studynet.application

import com.zhang.studynet.dao.{CourseClickCountDao, CourseSearchClickCountDao}
import com.zhang.studynet.domain.{ClickLog, CourseClickCount, CourseSearchClickCount}
import com.zhang.studynet.utils.DateUtils
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable.ListBuffer

object StudyCountByStreaming {

  def main(args: Array[String]): Unit = {

    if (args.length != 4) {
      System.err.println("Error:you need to input:<zookeeper> <group> <toplics> <threadNum>")
      System.exit(1)
    }

    val Array(zkAddress, group, topics, threadNum) = args

    val sparkConf = new SparkConf()
      .setAppName("StudyCountByStreaming")
      .setMaster("local[*]")
      .set("spark.test.memory","2147480000")

    val ssc = new StreamingContext(sparkConf, Seconds(60))

    val topicsMap = topics.split(",").map((_, threadNum.toInt)).toMap

    val kafkaInputDS = KafkaUtils.createStream(ssc, zkAddress, group, topicsMap)

    val logResourcesDS = kafkaInputDS.map(_._2)

    val cleanDataRDD = logResourcesDS.map(line => {
      val splits = line.split("\t")
      if (splits.length != 5) {
        ClickLog("", "", 0, 0, "")
      } else {
        val ip = splits(0)
        val time = DateUtils.parseToMinute(splits(1)) //获得日志中用户的访问时间，并调用DateUtils格式化时间
        val status = splits(3).toInt //获得访问状态码
        val referer = splits(4)
        val url = splits(2).split(" ")(1) //获得搜索url
        var courseId = 0
        if (url.startsWith("/class")) {
          val courseIdHtml = url.split("/")(2)
          courseId = courseIdHtml.substring(0, courseIdHtml.lastIndexOf(".")).toInt
        }
        ClickLog(ip, time, courseId, status, referer)
      }
    }).filter(_.courseId != 0)

    cleanDataRDD.map(line => {
      //这里相当于定义HBase表"ns1:courses_clickcount"的RowKey，
      // 将‘日期_课程’作为RowKey,意义为某天某门课的访问数
      (line.time.substring(0, 8) + "_" + line.courseId, 1)
    }).reduceByKey(_ + _)
      .foreachRDD(rdd => {
        rdd.foreachPartition(partition => {
          val list = new ListBuffer[CourseClickCount]

          partition.foreach(item => {
            list.append(CourseClickCount(item._1, item._2))
          })

          CourseClickCountDao.save(list)
        })
      })

    /**
      * 统计至今为止通过各个搜索引擎而来的实战课程的总点击数
      * (1)统计数据
      * (2)把统计结果写进HBase中去
      */
    cleanDataRDD.map(line => {
      val refer = line.referer
      val time = line.time.substring(0, 8)
      var url = ""
      if (refer == "-") {
        (url, time)
      } else {
        url = refer.replaceAll("//", ",").split("/")(1)
        (url, time)
      }
    }).filter(_._1 != "").map(line => {
      (line._2 + "_" + line._1, 1)
    }).reduceByKey(_ + _)
      .foreachRDD(rdd => {
        rdd.foreachPartition(partition => {
          val list = new ListBuffer[CourseSearchClickCount]

          partition.foreach(item => {
            list.append(CourseSearchClickCount(item._1, item._2))
          })

          CourseSearchClickCountDao.save(list)
        })
      })

    ssc.start()
    ssc.awaitTermination()

  }

}
