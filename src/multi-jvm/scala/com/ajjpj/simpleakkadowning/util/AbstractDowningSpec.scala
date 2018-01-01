package com.ajjpj.simpleakkadowning.util

import akka.remote.testconductor.RoleName

abstract class AbstractDowningSpec(config: SimpleDowningConfig, survivors: Int*) extends MultiNodeClusterSpec(config) {
  val side1 = survivors.map(s => RoleName(s"$s")).toVector //  Vector (node1, node2, node3)
  val side2 = roles.tail.filterNot (side1.contains) //Vector (node4, node5)

  "A cluster of five nodes" should {
    "reach initial convergence" in {
      muteLog()
      muteMarkingAsUnreachable()
      muteMarkingAsReachable()

      awaitClusterUp(side1 ++ side2 :_*)
      enterBarrier("after-1")
    }

    "mark nodes as unreachable between partitions, and heal the partition" in {
      enterBarrier ("before-split")
      // mark nodes across the partition as mutually unreachable, and wait until that is reflected in all nodes' local cluster state
      createNetworkPartition(side1, side2)
      enterBarrier ("after-split")

      // mark nodes across the partition as mutually unreachable, and wait until that is reflected in all nodes' local cluster state
      healNetworkPartition()
      enterBarrier ("after-network-heal")

      runOn (config.conductor) {
        for (r <- side1 ++ side2) {
          upNodesFor (r) shouldBe (side1 ++ side2).toSet
          unreachableNodesFor (r) shouldBe empty
        }
      }

      enterBarrier ("after-cluster-heal")
    }

    "detect a network partition and shut down one partition after a timeout" in {
      enterBarrier("before-durable-partition")

      // mark nodes across the partition as mutually unreachable, and wait until that is reflected in all nodes' local cluster state
      createNetworkPartition (side1, side2)
      enterBarrier("durable-partition")

      // five second timeout until our downing strategy kicks in - plus some additional delay to be on the safe side
      Thread.sleep(7000)

      runOn (config.conductor) {
        for (r <- side1) upNodesFor(r) shouldBe side1.toSet
        for (r <- side2) upNodesFor(r) shouldBe empty
      }

      // some additional time to ensure cluster node JVMs stay alive during the previous checks
      Thread.sleep(2000)
    }
  }
}
