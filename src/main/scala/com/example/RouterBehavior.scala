package com.example

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

class RouterBehavior (
                       context: ActorContext[Message]
                     ) extends AbstractBehavior[Message](context) {

  val childDispatcher = context.spawn(DispatcherBehavior(context.self),"testDispatcher")
  val reader = context.spawn(ReaderBehavior(context.self), "reader")
  val writer = context.spawn(WriterBehavior(context.self), "writer")

  override def onMessage(msg: Message): Behavior[Message] = {
    msg match {
      case m:ResendMessage => reader ! m
      case _ => if (msg.isConfirmed) childDispatcher ! msg else writer ! msg
    }
    Behaviors.same[Message]
    }

}
