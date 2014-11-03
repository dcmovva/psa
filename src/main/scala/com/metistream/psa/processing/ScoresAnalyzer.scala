package com.metistream.psa.processing

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd._
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.{HBaseConfiguration, HTableDescriptor}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat

object ScoresAnalyzer {

  def main(args: Array[String]) {

    val scconf = new SparkConf().setAppName("Scores Analyzer").setMaster("local")
    val sc = new SparkContext(scconf)
    
    
    val conf = HBaseConfiguration.create()
    conf.set("hbase.zookeeper.quorum", "quickstart.cloudera")
    conf.set(TableInputFormat.INPUT_TABLE, "tweets")


    val hBaseRDD = sc.newAPIHadoopRDD(conf, classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])

   val categories =  hBaseRDD.map(tuple => tuple._2).map(result => (result.getRow, result.getColumn("metrics".getBytes(), "categories".getBytes())))

   val keywords = hBaseRDD.map(tuple => tuple._2).map(result => (result.getRow, result.getColumn("metrics".getBytes(), "keywords".getBytes())))

   val sentiments = hBaseRDD.map(tuple => tuple._2).map(result => (result.getRow, result.getColumn("metrics".getBytes(), "sentimental_score".getBytes())))

   
   println(hBaseRDD.count())
    sc.stop()

  }

}