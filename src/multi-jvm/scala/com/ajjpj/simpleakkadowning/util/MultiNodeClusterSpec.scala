package com.ajjpj.simpleakkadowning.util

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorSystem, Address, Props}
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.cluster.Member.addressOrdering
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import akka.stream.ActorMaterializer
import akka.testkit.TestEvent.Mute
import akka.testkit.{EventFilter, ImplicitSender}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.util.control.NonFatal


abstract class MultiNodeClusterSpec(config: SimpleDowningConfig) extends MultiNodeSpec(config) with STMultiNodeSpec with ImplicitSender {
  def initialParticipants = roles.size

  def cluster: Cluster = Cluster(system)

  private val cachedAddresses = new ConcurrentHashMap[RoleName, Address]
  implicit def address(role: RoleName): Address = {
    cachedAddresses.get(role) match {
      case null ⇒
        val address = node(role).address
        cachedAddresses.put(role, address)
        address
      case address ⇒ address
    }
  }

  implicit val clusterOrdering: Ordering[RoleName] = new Ordering[RoleName] {
    import Member.addressOrdering
    def compare(x: RoleName, y: RoleName) = addressOrdering.compare(address(x), address(y))
  }

  def assertLeader(nodesInCluster: RoleName*): Unit =
    if (nodesInCluster.contains(myself)) assertLeaderIn(nodesInCluster.to[immutable.Seq])

  def assertLeaderIn(nodesInCluster: immutable.Seq[RoleName]): Unit =
    if (nodesInCluster.contains(myself)) {
      nodesInCluster.length should not be (0)
      val expectedLeader = roleOfLeader(nodesInCluster)
      val leader = cluster.state.leader
      val isLeader = leader == Some(cluster.selfAddress)
      assert(
        isLeader == isNode(expectedLeader),
        "expectedLeader [%s], got leader [%s], members [%s]".format(expectedLeader, leader, cluster.state.members))
      cluster.selfMember.status should (be(MemberStatus.Up) or be(MemberStatus.Leaving))
    }

  def roleOfLeader(nodesInCluster: immutable.Seq[RoleName] = roles): RoleName = {
    nodesInCluster.length should not be (0)
    nodesInCluster.sorted.head
  }


  def awaitClusterUp(roles: RoleName*): Unit = {
    runOn(roles.head) {
      // make sure that the node-to-join is started before other join
      startClusterNode()
    }
    enterBarrier(roles.head.name + "-started")
    if (roles.tail.contains(myself)) {
      cluster.join(roles.head)
    }
    if (roles.contains(myself)) {
      system.actorOf(Props(new ClusterHttpInspector(httpPort(myself))), "http-server")
      awaitMembersUp(numberOfMembers = roles.length)
    }
    enterBarrier(roles.map(_.name).mkString("-") + "-joined")
  }

  def httpPort (node: RoleName) = {
    val nodeNo = roles.indexOf(node)
    require(nodeNo > 0)
    8080 + nodeNo
  }

  def awaitMembersUp(numberOfMembers:          Int,
                     canNotBePartOfMemberRing: Set[Address]   = Set.empty,
                     timeout:                  FiniteDuration = 25.seconds): Unit = {
    within(timeout) {
      if (!canNotBePartOfMemberRing.isEmpty) // don't run this on an empty set
        awaitAssert(canNotBePartOfMemberRing foreach (a ⇒ cluster.state.members.map(_.address) should not contain (a)))
      awaitAssert(cluster.state.members.size should ===(numberOfMembers))
      awaitAssert(cluster.state.members.map(_.status) should ===(Set(MemberStatus.Up)))
      // clusterView.leader is updated by LeaderChanged, await that to be updated also
      val expectedLeader = cluster.state.members.collectFirst {
        case m if m.dataCenter == cluster.settings.SelfDataCenter ⇒ m.address
      }
      awaitAssert(cluster.state.leader should ===(expectedLeader))
    }
  }

  def startClusterNode(): Unit = {
    if (cluster.state.members.isEmpty) {
      cluster join myself
      awaitAssert(cluster.state.members.map(_.address) should contain(address(myself)))
    } else
      cluster.selfMember
  }


  def muteMarkingAsUnreachable(sys: ActorSystem = system): Unit =
    if (!sys.log.isDebugEnabled)
      sys.eventStream.publish(Mute(EventFilter.error(pattern = ".*Marking.* as UNREACHABLE.*")))

  private def portToNode(port: Int) = roles.filter(address(_).port contains port).head

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
}
