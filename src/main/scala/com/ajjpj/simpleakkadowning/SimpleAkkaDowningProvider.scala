package com.ajjpj.simpleakkadowning

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}
import akka.cluster.ClusterEvent.{ClusterDomainEvent, CurrentClusterState, MemberEvent, ReachabilityEvent}
import akka.cluster.{Cluster, DowningProvider}

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * This DowningProvider implementation is the entry point to the SimpleAkkaDowning library. Configuring it as
  *  akka.cluster.downing-provider-class activates it.
  */
class SimpleAkkaDowningProvider(system: ActorSystem) extends DowningProvider {
  import Helpers._

  private val cc = system.settings.config.getConfig("simple-akka-downing")

  override val downRemovalMargin: FiniteDuration = {
    val key = "down-removal-margin"
    toRootLowerCase(cc.getString(key)) match {
      case "off" => Duration.Zero
      case _     => cc.getMillisDuration(key) requiring (_ >= Duration.Zero, key + " > 0s, or off")
    }
  }

  override def downingActorProps = {
      import Helpers._
      val cc = system.settings.config.getConfig("simple-akka-downing")
      val key = "stable-after"
      val stableInterval = cc.getMillisDuration(key) requiring (_ > Duration.Zero, key + " > 0s")

      val decider = SurvivalDecider(system.settings.config)

      Some (Props(new DowningActor(stableInterval, decider)))
  }
}


private[simpleakkadowning] class DowningActor(stableInterval: FiniteDuration, decider: SurvivalDecider) extends Actor with ActorLogging {
  case class SplitBrainDetected(clusterState: CurrentClusterState)

  private val cluster = Cluster.get(context.system)
  cluster.subscribe(self, classOf[ClusterDomainEvent])

  private var unreachableTimer = Option.empty[Cancellable]

  override def receive = {
    case e: MemberEvent       => onClusterChanged()
    case e: ReachabilityEvent => onClusterChanged()

    //TODO extract side effects for testability
    case SplitBrainDetected(clusterState) if decider.isInMinority(clusterState, cluster.selfAddress) =>
      log.error("Network partition detected. I am not in the surviving partition --> terminating")
      context.system.terminate()
      context.become(Actor.emptyBehavior)
    case SplitBrainDetected(clusterState) if iAmOldest(clusterState) =>
      println("****************************************** oldest !!!")
      log.error("Network partition detected. I am the oldest node in the surviving partition --> terminating unreachable nodes {}", cluster.state.unreachable)
      cluster.state.unreachable.foreach(m => cluster.down(m.address))
    case SplitBrainDetected(clusterState) =>
      println("****************************************** not oldest")

      log.info("Network partition detected. I am in the surviving partition, but I am not the leader, so nothing needs to be done")
  }

  private def oldestReachable(clusterState: CurrentClusterState) = {
    val allReachable = clusterState.members -- clusterState.unreachable
    allReachable.foldLeft(allReachable.iterator.next())((a, b) => if (a.isOlderThan(b)) a else b)
  }

  private def iAmOldest(clusterState: CurrentClusterState) = oldestReachable(clusterState).address == cluster.selfAddress

  private def onClusterChanged(): Unit = {
    println("#############################################  onClusterchanged: " + cluster.state.unreachable.size)

    unreachableTimer.foreach(_.cancel())
    unreachableTimer = None

    if (cluster.state.unreachable.nonEmpty) {
      println("******************************************                   starting timer")

      import context.dispatcher
      // Store the cluster's state in the message to ensure split brain detection is done based on the state that was stable.
      //  If the handler reads the then-current cluster state, that may have changed between the scheduler firing and the event
      //  being handled
      unreachableTimer = Some(context.system.scheduler.scheduleOnce(stableInterval, self, SplitBrainDetected(cluster.state)))
    }
  }

  override def postStop () = {
    super.postStop ()
    unreachableTimer.foreach(_.cancel())
  }
}
