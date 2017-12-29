package com.ajjpj.simpleakkadowning

import akka.actor.Address
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.MemberStatus
import com.typesafe.config.Config


trait SurvivalDecider {
  def isInMinority(clusterState: CurrentClusterState, selfAddress: Address): Boolean

  //TODO is there a good and meaningful way to count 'weaklyUp' members?
  /**
    * This convenience method extracts all members that should count as cluster members.
    */
  protected def upMembers(clusterState: CurrentClusterState) = clusterState.members.filter(_.status == MemberStatus.Up)
  protected def upUnreachable(clusterState: CurrentClusterState) = clusterState.unreachable.filter(_.status == MemberStatus.Up)
}

private[simpleakkadowning] object SurvivalDecider {
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
        val role = ccc.getString("role") match {
          case r if r.trim.isEmpty => None
          case r => Some(r)
        }
        new KeepOldestDecider(downIfAlone, role)
    }
  }


  class FixedQuorumDecider(quorumSize: Int, role: Option[String]) extends SurvivalDecider {
    override def isInMinority(clusterState: CurrentClusterState, selfAddress: Address) = {
      val relevantMembers = role match {
        case Some (r) => upMembers(clusterState).filter (_.roles contains r)
        case None =>     upMembers(clusterState)
      }

      println (s"isInMinotity: unreachable ${clusterState.unreachable.flatMap(_.address.port).mkString("[", ", ", "]")}, relevant: ${relevantMembers.flatMap(_.address.port).mkString("[", ",", "]")}")
//      println ("isInMinority: " + clusterState.members.map(_.address.port) + "/" + clusterState.unreachable.map(_.address.port) + ": " +  relevantMembers.map(_.address.port) + " -- " + upUnreachable(clusterState).map(_.address.port) + " -> " + (relevantMembers -- upUnreachable(clusterState)).map(_.address.port))

      (relevantMembers -- upUnreachable(clusterState)).size < quorumSize
    }
  }

  class KeepMajorityDecider(role: Option[String]) extends SurvivalDecider {
    override def isInMinority (clusterState: CurrentClusterState, selfAddress: Address) = {
      role match {
        case Some(r) =>
          val all = upMembers(clusterState).filter(_.roles contains r)
          val unreachable = upUnreachable(clusterState).filter(_.roles contains r)
          all.size > 2*unreachable.size
        case None =>
          upMembers(clusterState).size > 2*upUnreachable(clusterState).size
      }
    }
  }

  class KeepOldestDecider(downIfAlone: Boolean, role: Option[String]) extends SurvivalDecider {
    override def isInMinority (clusterState: CurrentClusterState, selfAddress: Address) = {
      val allRelevant = role match {
        case Some(r) => upMembers(clusterState).filter(_.roles contains r)
        case None    => upMembers(clusterState)
      }
      val oldestRelevant = allRelevant.foldLeft(allRelevant.iterator.next())((a, b) => if (a.isOlderThan(b)) a else b)

      (clusterState.unreachable contains oldestRelevant) || (downIfAlone && upMembers(clusterState).size == upUnreachable(clusterState).size + 1)
    }
  }
}