package com.ajjpj.simpleakkadowning.akka

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import language.postfixOps
import com.typesafe.config.{Config, ConfigFactory}
import akka.remote.testkit.MultiNodeConfig
import akka.remote.testkit.MultiNodeSpec
import akka.testkit._

import scala.concurrent.duration._
import akka.actor.Address
import akka.cluster.Member.addressOrdering
import akka.cluster.{Cluster, ClusterReadView, Member, MemberStatus}
import akka.remote.testconductor.RoleName

import scala.concurrent.duration._
import scala.collection.immutable
import akka.remote.transport.ThrottlerTransportAdapter.Direction
import com.ajjpj.simpleakkadowning.util.STMultiNodeSpec

final case class SplitBrainMultiNodeConfig(failureDetectorPuppet: Boolean) extends MultiNodeConfig {
  val first = role("first")
  val second = role("second")
  val third = role("third")
  val fourth = role("fourth")
  val fifth = role("fifth")

  commonConfig(debugConfig(on = false).
    withFallback(ConfigFactory.parseString("""
        akka.remote.retry-gate-closed-for = 3 s
        akka.cluster {
          auto-down-unreachable-after = 1s
          failure-detector.threshold = 4
        }""")).
    withFallback(clusterConfig(failureDetectorPuppet)))

  testTransport(on = true)

  def clusterConfig(withPuppet: Boolean): Config = ConfigFactory.parseString(s"""
    akka.actor.provider = cluster
    akka.actor.warn-about-java-serializer-usage = off
    akka.cluster {
      jmx.enabled                         = off
      gossip-interval                     = 200 ms
      leader-actions-interval             = 200 ms
      unreachable-nodes-reaper-interval   = 500 ms
      periodic-tasks-initial-delay        = 300 ms
      publish-stats-interval              = 0 s # always, when it happens
      failure-detector.heartbeat-interval = 500 ms

      run-coordinated-shutdown-when-down = off
    }
    akka.loglevel = INFO
    akka.log-dead-letters = off
    akka.log-dead-letters-during-shutdown = off
    akka.remote {
      log-remote-lifecycle-events = off
      artery.advanced.flight-recorder {
        enabled=on
        destination=target/flight-recorder-${UUID.randomUUID().toString}.afr
      }
    }
    akka.loggers = ["akka.testkit.TestEventListener"]
    akka.test {
      single-expect-default = 5 s
    }

    """)

}

//class SplitBrainWithFailureDetectorPuppetMultiJvmNode1 extends SplitBrainSpec(failureDetectorPuppet = true)
//class SplitBrainWithFailureDetectorPuppetMultiJvmNode2 extends SplitBrainSpec(failureDetectorPuppet = true)
//class SplitBrainWithFailureDetectorPuppetMultiJvmNode3 extends SplitBrainSpec(failureDetectorPuppet = true)
//class SplitBrainWithFailureDetectorPuppetMultiJvmNode4 extends SplitBrainSpec(failureDetectorPuppet = true)
//class SplitBrainWithFailureDetectorPuppetMultiJvmNode5 extends SplitBrainSpec(failureDetectorPuppet = true)

class SplitBrainWithAccrualFailureDetectorMultiJvmNode1 extends SplitBrainSpec(failureDetectorPuppet = false)
class SplitBrainWithAccrualFailureDetectorMultiJvmNode2 extends SplitBrainSpec(failureDetectorPuppet = false)
class SplitBrainWithAccrualFailureDetectorMultiJvmNode3 extends SplitBrainSpec(failureDetectorPuppet = false)
class SplitBrainWithAccrualFailureDetectorMultiJvmNode4 extends SplitBrainSpec(failureDetectorPuppet = false)
class SplitBrainWithAccrualFailureDetectorMultiJvmNode5 extends SplitBrainSpec(failureDetectorPuppet = false)

abstract class SplitBrainSpec(multiNodeConfig: SplitBrainMultiNodeConfig)
  extends MultiNodeSpec(multiNodeConfig)
    with STMultiNodeSpec  {

  def this(failureDetectorPuppet: Boolean) = this(SplitBrainMultiNodeConfig(failureDetectorPuppet))

  override def initialParticipants = roles.size

  import multiNodeConfig._

//  muteMarkingAsUnreachable()

  val side1 = Vector(first, second)
  val side2 = Vector(third, fourth, fifth)

  "A cluster of 5 members" must {

    "reach initial convergence" in {
      awaitClusterUp(first, second, third, fourth, fifth)

      enterBarrier("after-1")
    }

    "detect network partition and mark nodes on other side as unreachable and form new cluster" in within(30 seconds) {
      enterBarrier("before-split")

      runOn(first) {
        // split the cluster in two parts (first, second) / (third, fourth, fifth)
        for (role1 ← side1; role2 ← side2) {
          testConductor.blackhole(role1, role2, Direction.Both).await
        }
      }
      enterBarrier("after-split")

      runOn(side1: _*) {
//        for (role ← side2) markNodeAsUnavailable(role)
        // auto-down
        awaitMembersUp(side1.size, side2.toSet map address)
        assertLeader(side1: _*)
      }

      runOn(side2: _*) {
//        for (role ← side1) markNodeAsUnavailable(role)
        // auto-down
        awaitMembersUp(side2.size, side1.toSet map address)
        assertLeader(side2: _*)
      }

      enterBarrier("after-2")
    }

  }

  //----------------------------------------

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
      awaitMembersUp(numberOfMembers = roles.length)
    }
    enterBarrier(roles.map(_.name).mkString("-") + "-joined")
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
}
