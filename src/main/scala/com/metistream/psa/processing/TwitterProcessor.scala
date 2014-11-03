package com.metistream.psa.processing

import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.SparkConf
import twitter4j._
import org.apache.hadoop.hbase.{ HBaseConfiguration, HColumnDescriptor, HTableDescriptor }
import org.apache.hadoop.hbase.client.{ HBaseAdmin, Put }
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import com.google.gson.Gson;
import org.apache.spark.rdd.{ PairRDDFunctions, RDD }

object TwitterProcessor {

  def main(args: Array[String]) {

    val consumerKey = "AHqVA6rXUfstmcMdJygEAhzTA"
    val consumerSecret = "VIOkyLjGmsMFDrIFcNVBLTzQhriMTTfJoJchcq3YesZvRPKr8W"
    val accessToken = "247099581-LjRYk1bGll5tH1YZvrRAQrxJWnQs6jTOGtKFd6NV"
    val accessTokenSecret = "CrBg0WPLlBNKcDJPLQVZfwEcCNMemOIVffq3qS5jRxDX5"

    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    val conf = HBaseConfiguration.create()

    conf.set("hbase.zookeeper.quorum", "quickstart.cloudera")

    // Initialize hBase table if necessary
    val admin = new HBaseAdmin(conf)

    val hbaseTableName = "tweets"
    if (!admin.isTableAvailable(hbaseTableName)) {
      val tableDesc = new HTableDescriptor(hbaseTableName)
      tableDesc.addFamily(new HColumnDescriptor("twits"))
      tableDesc.addFamily(new HColumnDescriptor("metrics"))
      admin.createTable(tableDesc)
    }

    val sparkConf = new SparkConf().setAppName("TwitterProcessor")
    val ssc = new StreamingContext(sparkConf, Seconds(2))

    val filters = Array("InformationWeek", "Inc", "HarvardBiz", "Forbes", "FastCompany", "businessinsider", "allthingsd", "ycombinator", "allthingshadoop", "espn", "mlb")

    val stream = TwitterUtils.createStream(ssc, None, filters)

    def mapTweet(s: Status): String = {
      val gson = new Gson
      var m = new Message()
      m.id = s.getId
      m.inReplyToStatusId = s.getInReplyToStatusId
      m.inReplyToUserId = s.getInReplyToUserId
      m.retweetCount = s.getRetweetCount
      m.text = s.getText
      m.latitude = Option(s.getGeoLocation).map(_.getLatitude()).getOrElse(0.0)
      m.longitude = Option(s.getGeoLocation).map(_.getLongitude()).getOrElse(0.0)
      m.source = s.getSource
      m.userId = s.getUser.getId
      m.createdAt = s.getCreatedAt
      m.userName = s.getUser.getName
      m.userScreenName = s.getUser.getScreenName
      m.userCreatedAt = s.getUser.getCreatedAt
      m.userFollowersCount = s.getUser.getFollowersCount
      m.userFavouritesCount = s.getUser.getFavouritesCount
      m.userLanguage = s.getUser.getLang
      m.userLocation = s.getUser.getLocation
      m.userTimeZone = s.getUser.getTimeZone

      gson.toJson(m)
    }

    def createTwitHBaseRow(status: Status) = {
      val record = new Put(Bytes.toBytes(status.getId()))

      val tweetString = mapTweet(status)
      record.add(Bytes.toBytes("twits"), Bytes.toBytes("info"), Bytes.toBytes(tweetString))

      (new ImmutableBytesWritable, record)

    }

    def getGroup(s: String): String = s match {
      case "nurse" | "nurses" => "Nurse Communication"
      case "doctor" | "doctors" => "Doctor Communication"
      case "admininstration" | "staff" => "Staff Responsiveness"
      case "pain" => "Pain Management"
      case "medication" => "Communication about Medicines"
      case "discharge" => "Discharge Information"
      case "bathroon" | "hallway" => "Cleanliness"
      case "quite" => "Quietness"
      case "parking" => "Overall Rating of Hospital"
      case _ => ""
    }

    def replaceChars(s: String): Array[String] = {
      s.replaceAll("[^\\x00-\\x7F]", "").replaceAll("[\\[\\]!,:.()@#]", "").split(" ").filterNot(_.contains("http")).filter(_.size > 2)
    }

    def getSentiment(i: Int): String = i match {
      case 0 => "neutral"
      case x if x > 4 => "neutral"
      case x if x < 0 => "neutral"
      case x if x > 2 => "positive"
      case _ => "negative"
    }

    def createMetricHBaseRow(status: Status) = {
      val record = new Put(Bytes.toBytes(status.getId()))

      val message = status.getText()

      val result = replaceChars(message).mkString(",")

      val categories = result.split(",").groupBy(w => getGroup(w)).keySet.filter(w => w.length > 0).mkString(",")

      val keyWordsString = "patience,kind,courtesy,respect,bad,great,wonderful,best,nice,thoughtful,caring,usually,always,rude,mean,respect,ignore,never,attentive,attention,unclear,confusing,confused,complex,never,responsive,response,attention,ignore"

      val keyWordsArray = keyWordsString.split(",")
      val stringArray = result.split(",")

      val commonKeyWords = keyWordsArray.intersect(stringArray).mkString(",")

      record.add(Bytes.toBytes("metrics"), Bytes.toBytes("categories"), Bytes.toBytes(categories))
      record.add(Bytes.toBytes("metrics"), Bytes.toBytes("keywords"), Bytes.toBytes(commonKeyWords))
      record.add(Bytes.toBytes("metrics"), Bytes.toBytes("sentimental_score"), Bytes.toBytes(getSentiment(com.metistream.psa.nlp.SentimentAnalysis.findSentiment(result))))

      (new ImmutableBytesWritable, record)

    }

    stream.foreachRDD { (rdd, time) =>
      val sc = rdd.context
      val conf = HBaseConfiguration.create()
      // val tweetString = mapTweet(rdd)
      conf.set("hbase.zookeeper.quorum", "quickstart.cloudera")
      val tableName = "tweets"
      val jobConfig = new JobConf(conf)
      jobConfig.set(TableOutputFormat.OUTPUT_TABLE, tableName)
      jobConfig.setOutputFormat(classOf[TableOutputFormat])

      new PairRDDFunctions(rdd.map(createTwitHBaseRow)).saveAsHadoopDataset(jobConfig)
      new PairRDDFunctions(rdd.map(createMetricHBaseRow)).saveAsHadoopDataset(jobConfig)
    }

    ssc.start()
    ssc.awaitTermination()
  }

}