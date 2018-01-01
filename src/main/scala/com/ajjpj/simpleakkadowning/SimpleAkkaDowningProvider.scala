package com.ajjpj.simpleakkadowning

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, DowningProvider, MemberStatus}
import com.ajjpj.simpleakkadowning.SurvivalDecider.{ClusterMemberInfo, ClusterState}

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
  case class SplitBrainDetected(clusterState: ClusterState)

  private var state: ClusterState = _

  private val cluster = Cluster.get(context.system)
  cluster.subscribe(self, classOf[ClusterDomainEvent])


  private var unreachableTimer = Option.empty[Cancellable]

  override def receive = {
    // cluster.state may or may not reflect the changes during event handling, so we need to keep track of cluster state ourselves

    case CurrentClusterState(members, unreachable, _, _, _) =>
      val upMembers = members.filter(_.status == MemberStatus.Up).map(m => ClusterMemberInfo(m.uniqueAddress, m.roles, m))
      state = ClusterState(upMembers, unreachable.map(_.uniqueAddress))
      triggerTimer()
    case MemberUp(m) =>
      //TODO is there a good and meaningful way to count 'weaklyUp' members?
      state = state.copy(upMembers = state.upMembers + ClusterMemberInfo(m.uniqueAddress, m.roles, m))
      triggerTimer()
    case MemberLeft(m) =>
      state = state.copy(upMembers = state.upMembers.filterNot (_.uniqueAddress == m.uniqueAddress))
      triggerTimer()
    case ReachableMember(m) =>
      state = state.copy (unreachable =  state.unreachable - m.uniqueAddress)
      println("######################################## reachable: " + m + " -> " + state)
      triggerTimer()
    case UnreachableMember(m) =>
      state = state.copy (unreachable = state.unreachable + m.uniqueAddress)
      println("######################################## unreachable: " + m + " -> " + state)
      triggerTimer()

    case SplitBrainDetected(clusterState) if decider.isInMinority(clusterState, cluster.selfAddress) =>
      log.error("Network partition detected. I am not in the surviving partition --> terminating")
      context.system.terminate()
      context.become(Actor.emptyBehavior)
    case SplitBrainDetected(clusterState) if iAmResponsibleAction(clusterState) =>
      log.error("Network partition detected. I am the responsible node in the surviving partition --> terminating unreachable nodes {}", cluster.state.unreachable)
      cluster.state.unreachable.foreach(m => cluster.down(m.address))
    case SplitBrainDetected(clusterState) =>
      log.info("Network partition detected. I am in the surviving partition, but I am not the responsible node, so nothing needs to be done")
  }

  private def triggerTimer(): Unit = {
    unreachableTimer.foreach(_.cancel())
    unreachableTimer = None

    if (state.unreachable.nonEmpty) {
      import context.dispatcher
      // Store the cluster's state in the message to ensure split brain detection is done based on the state that was stable.
      //  If the handler reads the then-current cluster state, that may have changed between the scheduler firing and the event
      //  being handled
      unreachableTimer = Some(context.system.scheduler.scheduleOnce(stableInterval, self, SplitBrainDetected(state)))
    }
  }

  private def iAmResponsibleAction(clusterState: ClusterState) = {
    clusterState.sortedUpAndReachable.head.uniqueAddress.address == cluster.selfAddress
  }

  override def postStop () = {
    super.postStop ()
    unreachableTimer.foreach(_.cancel())
  }
}
