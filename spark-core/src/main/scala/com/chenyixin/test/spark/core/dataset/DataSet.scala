package com.chenyixin.test.spark.core.dataset

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
object DataSet {

  def upper(str: String) = {
    str.toUpperCase
  }

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("SparkSQL")
      .master("local")
      .getOrCreate()
    import spark.implicits._
    val  df = spark.createDataset(List(("aaa",List(1,2)),("bbb",List(3,4)),("ccc",List(5,6)),("bbb",List(5,6)),("aaa",List(7,6))))
      .toDF("key","value")

    df.show(20)
    df.printSchema

  }



}
