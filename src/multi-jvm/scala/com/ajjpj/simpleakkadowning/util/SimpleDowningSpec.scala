package com.ajjpj.simpleakkadowning.util

import akka.actor.Props
import akka.cluster.{Cluster, MemberStatus}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import akka.remote.transport.ThrottlerTransportAdapter.Direction
import akka.stream.ActorMaterializer
import akka.testkit.ImplicitSender

import scala.concurrent.duration._
import scala.util.control.NonFatal

abstract class SimpleDowningSpec(config: SimpleDowningConfig) extends MultiNodeSpec(config) with STMultiNodeSpec with ImplicitSender{

  def initialParticipants = roles.size

  private var portToNode = Map.empty[Int,RoleName]

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

    portToNode = roles.map(r => node(r).address.port.get -> r).toMap
  }

  def httpPort (node: RoleName) = {
    val nodeNo = roles.indexOf(node)
    require(nodeNo > 0)
    8080 + nodeNo
  }

  def seedAddresses = roles.tail.map(node(_).root.address)

  private def httpGetNodes(node: RoleName, path: String): Set[RoleName] = {
    try {
      import system.dispatcher
      implicit val mat = ActorMaterializer()

      val uri = Uri (s"http://localhost:${httpPort (node)}$path")
      val response = Http (system).singleRequest (HttpRequest (uri = uri)).await
      val strict = response.entity.toStrict (10.seconds).await
      strict.data.decodeString ("utf-8") match {
        case s if s.isEmpty => Set.empty
        case s => s.split (' ').map (_.toInt).map (portToNode).toSet
      }
    }
    catch {
      case NonFatal(th) =>
        th.printStackTrace()
        Set.empty
    }
  }

  def upNodesFor(node: RoleName) = httpGetNodes(node, "/cluster-members/up")
  def unreachableNodesFor (node: RoleName) = httpGetNodes(node, "/cluster-members/unreachable")

  /**
    * Simulates a partitioning of the network into the given set of nodes as the first partition and all other
    *  nodes as the second partition, i.e. network traffic within one partition works unhindered, but traffic
    *  between partitions is cut off
    */
  def createPartition(nodes: RoleName*) = {
    val otherNodes = roles.tail.toSet -- nodes
    for (n1 <- nodes; n2 <- otherNodes) testConductor.blackhole(n1, n2, Direction.Both).await
  }

  def healPartition(): Unit = {
    for (n1 <- roles.tail; n2 <- roles.tail) testConductor.passThrough(n1, n2, Direction.Both).await
  }
}
