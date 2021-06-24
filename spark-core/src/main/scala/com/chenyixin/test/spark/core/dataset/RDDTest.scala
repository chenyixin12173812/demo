package com.chenyixin.test.spark.core.dataset

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object RDDTest {
  def main(args: Array[String]): Unit = {

    val sparConf = new SparkConf().setMaster("local").setAppName("RDD")
    val sc = new SparkContext(sparConf)

    val rdd :RDD[(String,Int)]= sc.makeRDD(List(("aa",1), ("333",2) , ("333",2)))
    val reduceRDD: RDD[(String, Int)] = rdd.reduceByKey((x:Int,y:Int)=>x+y)
    reduceRDD.foreach(println);
    val groupkey: RDD[(String, Iterable[Int])]= rdd.groupByKey()
    groupkey.foreach(println)
    val group: RDD[(String, Iterable[(String,Int)])]= rdd.groupBy(x=>x._1)
    group.foreach(println)



  }


}










class RDDTest {

}
