package com.metistream.psa.processing

import java.util.Date;

class Message {
  
  var id = 0L
  var inReplyToStatusId = 0L
  var inReplyToUserId = 0L
  var retweetCount = 0L
  var text = ""
  var latitude = 0.0
  var longitude = 0.0
  var source = ""
  var createdAt = new Date()
  var userId = 0L
  var userName = ""
  var userScreenName = ""
  var userCreatedAt = new Date()
  var userFollowersCount = 0
  var userFavouritesCount = 0
  var userLanguage = ""
  var userLocation = ""
  var userTimeZone = ""

}