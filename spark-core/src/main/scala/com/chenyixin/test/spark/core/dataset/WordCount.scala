package com.chenyixin.test.spark.core.dataset

import org.apache.spark.{SparkConf, SparkContext}


object WordCount {
  def main(args: Array[String]): Unit = {
    val sparConf = new SparkConf().setMaster("local").setAppName("SparkSQL")
    val sc = new SparkContext(sparConf)

    val inputFile =  "g://word.txt"
    val textFile = sc.textFile(inputFile)
    val wordCount = textFile.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey((a, b) => a + b)
    wordCount.foreach(println)
  }
}







class WordCount {

}
