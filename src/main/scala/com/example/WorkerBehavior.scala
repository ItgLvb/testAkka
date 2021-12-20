package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

class WorkerBehavior(
                      context: ActorContext[Message],
                      sourceId: Long,
                      router: ActorRef[Message]
                    ) extends AbstractBehavior[Message](context) {
  override def onMessage(msg: Message): Behavior[Message] =
    msg match {
      case m: TestMessage =>
        println(s"TestMessage ${m.mTime}")
        router ! TestMessage2(0, m.mTime, sourceId)
        Behaviors.same
    }
}

object WorkerBehavior {
  def apply(
             sourceId: Long,
             router: ActorRef[Message]
           ): Behavior[Message] =
    Behaviors.setup(ctx => new WorkerBehavior(ctx, sourceId, router))
}