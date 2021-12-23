package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable


class DispatcherBehavior2(
                          context: ActorContext[Message],
                          router: ActorRef[Message]
                        ) extends AbstractBehavior[Message](context) {
  val actorsMap: mutable.Map[Long, ActorRef[Message]] = mutable.Map[Long, ActorRef[Message]]()

  val readPositions: mutable.Map[Long,Long] = mutable.Map[Long,Long]()

  override def onMessage(msg: Message): Behavior[Message] = {
    val position = readPositions.get(msg.sourceId)
    val actor = actorsMap.get(msg.sourceId)
    if(position.nonEmpty && position.exists(_ < msg.mTime)) {
      actor.foreach(_ ! msg)
    } else {
      actor.foreach(ref => context.stop(ref))
      val nextWorker = context.spawn(DispatcherBehavior2.WorkerBehavior(msg.sourceId, router), "dispatcher2_worker")
      actorsMap.update(msg.sourceId, nextWorker)
      nextWorker ! msg
    }
    Behaviors.same[Message]
  }
}

object DispatcherBehavior2 {
  def apply(
             router: ActorRef[Message]
           ): Behavior[Message] =
    Behaviors.setup(ctx => new DispatcherBehavior2(ctx, router))

  class WorkerBehavior(
                        context: ActorContext[Message],
                        sourceId: Long,
                        router: ActorRef[Message]
                      ) extends AbstractBehavior[Message](context) {
    override def onMessage(msg: Message): Behavior[Message] =
      msg match {
        case m: TestMessage2 =>
          println(s"TestMessage2 ${m.mTime}")
          router ! TestMessage(sourceId + 1, m.mTime, sourceId)
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
}