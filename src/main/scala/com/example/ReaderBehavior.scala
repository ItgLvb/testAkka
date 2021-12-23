package com.example

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

class ReaderBehavior  (
                        context: ActorContext[Message],
                        router: ActorRef[Message]
                      ) extends AbstractBehavior[Message](context) {
  override def onMessage(msg: Message): Behavior[Message] = {
    msg match {
      case mes:ResendMessage =>
        val messages = DB.getBySourceId(mes.sourceId).filter(m => Message.getMetaInfo(m.getClass).exists(an => mes.types.contains(an.id())) && m.mTime > mes.mTime)
        messages.foreach(router ! _)
      case _ => throw new Exception("Invalid message type for reader")
    }
    Behaviors.same
  }
}

object ReaderBehavior {
  def apply(
             router: ActorRef[Message]
           ): Behavior[Message] =
    Behaviors.setup(ctx => new ReaderBehavior(ctx, router))
}