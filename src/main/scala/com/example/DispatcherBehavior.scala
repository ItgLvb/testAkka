package com.example

import akka.actor.PoisonPill
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.collection.mutable


class DispatcherBehavior(
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
      val nextWorker = context.spawn(DispatcherBehavior.WorkerBehavior(msg.sourceId, router), "dispatcher_worker")
      actorsMap.update(msg.sourceId, nextWorker)
      nextWorker ! msg
    }
    Behaviors.same[Message]
  }
}

object DispatcherBehavior {
  def apply(
             router: ActorRef[Message]
           ): Behavior[Message] =
    Behaviors.setup(ctx => new DispatcherBehavior(ctx, router))

  class WorkerBehavior(
                        context: ActorContext[Message],
                        sourceId: Long,
                        router: ActorRef[Message]
                      ) extends AbstractBehavior[Message](context) {
    override def onMessage(msg: Message): Behavior[Message] =
      msg match {
        case m: TestMessage =>
          println(s"TestMessage ${m.mTime}")
          router ! TestMessage2(sourceId + 1, m.mTime, sourceId)
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