package com.example

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

class WriterBehavior (
                       context: ActorContext[Message],
                       router: ActorRef[Message]
                     ) extends AbstractBehavior[Message](context) {
  override def onMessage(msg: Message): Behavior[Message] = {
    msg match {
      case m: DeleteMessages =>
        DB.removeBySenderId(m.senderId, m.mTime)
        val completeMessage = DeleteMessagesCompleted(m.sourceId,m.mTime, m.senderId)
        completeMessage.isConfirmed = true
        m.sourceRef ! completeMessage
      case m if Message.getMetaInfo(m.getClass).exists(!_.singleton) =>
        DB.addMessage(msg)
        router ! msg
    }
    Behaviors.same
  }
}

object WriterBehavior {
  def apply(
             router: ActorRef[Message]
           ): Behavior[Message] =
    Behaviors.setup(ctx => new WriterBehavior(ctx, router))
}