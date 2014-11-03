package com.metistream.psa.processing

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd._

object ScoringTest {

 def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("My Test").setMaster("local")
    val sc = new SparkContext(conf)

    val categories = sc.textFile("/Users/dilip/categories.txt")
    
    val sentiments = sc.textFile("/Users/dilip/sentiment.txt")
    
     def mapSentiments() : RDD[(String, String)] = {
     sentiments.map { x => 
         val array = x.split(":")
         array(0) -> array(1) 
       }
    }
     
    def extractList(s: Iterable[(String, String)], t : scala.collection.Map[String, String]): String = {
      
      s.map(w => t(w._2)).mkString(",")
    }

   def mapCategories(s: String): Map[String, String] = {

      val array = s.split(":")

      val array1 = array(1).split(",")

      array1.map(x => x.trim() -> array(0)).toMap

    }

    def getCategories(t : scala.collection.Map[String, String]): RDD[(String, String)] = {
      val cats = categories.flatMap(mapCategories(_)).groupBy(w => w._1)
      
      cats.map(kv => kv._1 -> extractList(kv._2,t))
   //   cats.map(kv => (kv._2.toSeq).groupBy(w => w).mapValues(_.size))
      
    }
    
    def groupSentiments(s : String) : String = {
     s.split(",").groupBy(w => w).mapValues(_.size).mkString(",")
    }
    
    val sents = mapSentiments() 
    val t =   sents.collectAsMap()
    
    val res = getCategories(t)
    
    
    res.foreach(f => println(f._1 + " " + groupSentiments(f._2)))
  }
 
 
 
 
 
// val conf = new SparkConf().setAppName("My Test").setMaster("local")
//  val sc = new SparkContext(conf)
//
//  val keywords = sc.textFile("/Users/dilip/keywords.txt")
//  
//  val categories = sc.textFile("/Users/dilip/categories.txt")
//  
//  def mapKeywords(): RDD[(String, String)] = {
//     keywords.map { x => 
//         val array = x.split(":")
//         array(0) -> array(1) 
//       }
//    }
//     
//    def extractList(s: Iterable[(String, String)], t : scala.collection.Map[String, String]): String = {
//      
//      s.map(w => t(w._2)).mkString(",")
//    }
//
//   def mapCategories(s: String): Map[String, String] = {
//
//      val array = s.split(":")
//
//      val array1 = array(1).split(",")
//
//      array1.map(x => x.trim() -> array(0)).toMap
//
//    }
//
//    def getCategories(t : scala.collection.Map[String, String]): RDD[(String, String)] = {
//      val cats = categories.flatMap(mapCategories(_)).groupBy(w => w._1)
//      
//      cats.map(kv => kv._1 -> extractList(kv._2,t))
//   //   cats.map(kv => (kv._2.toSeq).groupBy(w => w).mapValues(_.size))
//      
//    }
//    
//    def groupSentiments(s : String) : Map[String,Int] = {
//     s.split(",").groupBy(w => w).mapValues(_.size)
//    }
//    
//    val keys = mapKeywords() 
//    val t =   keys.collectAsMap()
//    
//    val res = getCategories(t)
//    
//    
//   val ti =  res.map(f => f._1 -> groupSentiments(f._2))
//   
//   ti.saveAsTextFile("/Users/dilip/teskjhkjt.txt")
//   
//   
//  }

}