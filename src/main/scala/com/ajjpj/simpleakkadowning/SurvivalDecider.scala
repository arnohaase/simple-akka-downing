package com.ajjpj.simpleakkadowning

import akka.actor.Address
import akka.cluster.{Member, UniqueAddress}
import com.ajjpj.simpleakkadowning.SurvivalDecider.ClusterState
import com.typesafe.config.Config

import scala.collection.Set
import scala.collection.immutable.SortedSet


trait SurvivalDecider {
  def isInMinority(clusterState: ClusterState, selfAddress: Address): Boolean
}

object SurvivalDecider {
  private val memberOrdering = new Ordering[ClusterMemberInfo] {
    override def compare (x: ClusterMemberInfo, y: ClusterMemberInfo) =
      Member.addressOrdering.compare(x.uniqueAddress.address, y.uniqueAddress.address)
  }

  case class ClusterMemberInfo(uniqueAddress: UniqueAddress, roles: Set[String], member: Member)
  case class ClusterState(upMembers: Set[ClusterMemberInfo], unreachable: Set[UniqueAddress]) {
    lazy val sortedUpMembers = SortedSet.empty(memberOrdering) ++  upMembers
    lazy val sortedUpAndReachable = sortedUpMembers.filterNot (x => unreachable.contains(x.uniqueAddress))
    lazy val upReachable = upMembers.filterNot(x => unreachable(x.uniqueAddress))
    lazy val upUnreachable = upMembers.filter(x => unreachable(x.uniqueAddress))
  }


  def apply(config: Config): SurvivalDecider = {
    val cc = config.getConfig("simple-akka-downing")

    cc.getString("active-strategy") match {
      case "static-quorum" =>
        val ccc = cc.getConfig("static-quorum")
        val quorumSize = ccc.getInt("quorum-size")
        val role = ccc.getString("role") match {
          case r if r.trim.isEmpty => None
          case r => Some(r)
        }
        new FixedQuorumDecider(quorumSize, role)
      case "keep-majority" =>
        val ccc = cc.getConfig("keep-majority")
        val role = ccc.getString("role") match {
          case r if r.trim.isEmpty => None
          case r => Some(r)
        }
        new KeepMajorityDecider(role)
      case "keep-oldest" =>
        val ccc = cc.getConfig("keep-oldest")
        val downIfAlone = ccc.getBoolean("down-if-alone")
        new KeepOldestDecider(downIfAlone)
    }
  }


  class FixedQuorumDecider(quorumSize: Int, role: Option[String]) extends SurvivalDecider {
    override def isInMinority(clusterState: ClusterState, selfAddress: Address) = {
      val relevantMembers = role match {
        case Some (r) => clusterState.upMembers.filter (_.roles contains r)
        case None =>     clusterState.upMembers
      }

      (relevantMembers -- clusterState.upUnreachable).size < quorumSize
    }
  }

  class KeepMajorityDecider(role: Option[String]) extends SurvivalDecider {
    override def isInMinority (clusterState: ClusterState, selfAddress: Address) = {
      role match {
        case Some(r) =>
          val all = clusterState.upMembers.filter(_.roles contains r)
          val unreachable = clusterState.upUnreachable.filter(_.roles contains r)
          all.size <= 2*unreachable.size
        case None =>
          clusterState.upMembers.size <= 2*clusterState.upUnreachable.size
      }
    }
  }

  class KeepOldestDecider(downIfAlone: Boolean) extends SurvivalDecider {
    override def isInMinority (clusterState: ClusterState, selfAddress: Address) = {
      val allRelevant = clusterState.upMembers
      val oldestRelevant = allRelevant.foldLeft(allRelevant.head)((a, b) => if (a.member isOlderThan b.member) a else b)

      if (downIfAlone) {
        clusterState.upReachable match {
          case s if s == Set(oldestRelevant) => true                                       // only the oldest node --> terminate
          case _ if clusterState.unreachable == Set(oldestRelevant.uniqueAddress) => false // the oldest node is the only unreachable node --> survive
          case _ => clusterState.unreachable contains oldestRelevant.uniqueAddress
        }
      }
      else {
        clusterState.unreachable contains oldestRelevant.uniqueAddress
      }
    }
  }
}