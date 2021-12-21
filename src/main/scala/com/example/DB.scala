package com.example

import scala.collection._
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._



  object DB {
    val map: concurrent.Map[Long, List[Message]] = new ConcurrentHashMap[Long, List[Message]]().asScala

    def addMessage(m: Message): Unit = {
      val current = map.get(m.sourceId)
      if (current.nonEmpty) map.update(m.sourceId, current.getOrElse(Nil) :+ m)
      else map.put(m.sourceId, List(m))
    }

    def getBySourceId(sId: Long): List[Message] = map.getOrElse(sId, Nil)

    def removeBySenderId(sId:Long) = {
      val keys = map.keySet
      keys.foreach(k => {
        val lst = map(k)
        map.update(k, lst.filter(_.senderId != sId))
      })
    }
}
