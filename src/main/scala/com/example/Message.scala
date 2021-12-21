package com.example

sealed trait Message {
  def sourceId: Long
  def mTime:Long
  def senderId: Long
  var isConfirmed:Boolean = false
}

@annotations.Message(id=0, singleton=true)
case class DeleteMessages(
                          sourceId:Long,
                          mTime: Long,
                          senderId: Long
                        ) extends Message

@annotations.Message(id=1, singleton=true)
case class ResendMessage(
                          sourceId:Long,
                          mTime: Long,
                          senderId: Long
                        ) extends Message

@annotations.Message(id=2, singleton=false)
case class TestMessage(
                        sourceId:Long,
                        mTime: Long,
                        senderId: Long
                      ) extends Message

@annotations.Message(id=3, singleton=true)
case class TimerMessage(
                        sourceId:Long,
                        mTime: Long,
                        senderId: Long
                      ) extends Message

@annotations.Message(id=3, singleton=false)
case class TestMessage2(
                        sourceId:Long,
                        mTime: Long,
                        senderId: Long
                      ) extends Message
