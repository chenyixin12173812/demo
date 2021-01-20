package com.chenyixin.test.spark.core.dataset

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
//https://blog.csdn.net/wzj_wp/article/details/103967979
object DataSet {

  def upper(str: String) = {
    str+"1";
  }

  def main(args: Array[String]): Unit = {
    val sparConf = new SparkConf().setMaster("local").setAppName("WordCount")
    val sc = new SparkContext(sparConf)


    val spark = SparkSession.builder()
      .appName("SparkSQLDemo")
      .master("local")
      .getOrCreate()
    import spark.implicits._
    val  df = spark.createDataset(List(("aaa",List(1,2)),("bbb",List(3,4)),("ccc",List(5,6)),("bbb",List(5,6)),("aaa",List(7,6)))).toDF("key1","key2")
    df.printSchema
    val rdd :RDD[(String,Int)]= sc.makeRDD(List(("aa",1), ("333",2) , ("333",2)))
   val reduceRDD: RDD[(String, Int)] = rdd.reduceByKey((x:Int,y:Int)=>x+y)
      reduceRDD.foreach(println);
    val groupkey: RDD[(String, Iterable[Int])]= rdd.groupByKey()
    groupkey.foreach(println)
    val group: RDD[(String, Iterable[(String,Int)])]= rdd.groupBy(x=>x._1)
    group.foreach(println)



  }
}
