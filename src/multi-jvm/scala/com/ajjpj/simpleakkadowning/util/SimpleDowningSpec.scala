package com.ajjpj.simpleakkadowning.util

import akka.actor.Props
import akka.cluster.{Cluster, MemberStatus}
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec


abstract class SimpleDowningSpec(config: SimpleDowningConfig) extends MultiNodeSpec(config) {
  def init(): Unit = {
    if (roles.headOption contains myself) {
      enterBarrier("initialized")
    }
    else {
      val cluster = Cluster(system)
      cluster.joinSeedNodes(seedAddresses)
      system.actorOf(Props(new ClusterHttpInspector(httpPort(myself))), "http-server")

      while (cluster.state.members.count(_.status == MemberStatus.Up) < roles.tail.size) Thread.sleep(100)
      enterBarrier("initialized")
    }
  }

  def httpPort (node: RoleName) = {
    val nodeNo = roles.indexOf(myself)
    require(nodeNo > 0)
    8080 + nodeNo
  }

  def seedAddresses = roles.tail.map(node(_).root.address)


}
