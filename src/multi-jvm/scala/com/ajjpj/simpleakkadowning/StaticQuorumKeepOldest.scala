package com.ajjpj.simpleakkadowning

import akka.remote.testconductor.RoleName
import com.ajjpj.simpleakkadowning.util.{MultiNodeClusterSpec, SimpleDowningConfig}


object StaticQuorumKeepOldest {
  object Config extends SimpleDowningConfig("static-quorum", "quorum-size" -> "3") {

    val conductor = role("0")
    val node1 = role("1")
    val node2 = role("2")
    val node3 = role("3")
    val node4 = role("4")
    val node5 = role("5")

  }

  abstract class Spec(survivors: Int*) extends MultiNodeClusterSpec(Config) {
    val conductor = RoleName("0")

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

        runOn (conductor) {
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
        enterBarrier("after-network-split")

        // five second timeout until our downing strategy kicks in - plus some additional delay to be on the safe side
        Thread.sleep(7000)

        runOn (conductor) {
          for (r <- side1) upNodesFor(r) shouldBe side1.toSet
          for (r <- side2) upNodesFor(r) shouldBe empty
        }

        // some additional time to ensure cluster node JVMs stay alive during the previous checks
        Thread.sleep(2000)
      }
    }
  }
}

class StaticQuorumKeepOldestMultiJvm0 extends StaticQuorumKeepOldest.Spec(1,2,3)
class StaticQuorumKeepOldestMultiJvm1 extends StaticQuorumKeepOldest.Spec(1,2,3)
class StaticQuorumKeepOldestMultiJvm2 extends StaticQuorumKeepOldest.Spec(1,2,3)
class StaticQuorumKeepOldestMultiJvm3 extends StaticQuorumKeepOldest.Spec(1,2,3)
class StaticQuorumKeepOldestMultiJvm4 extends StaticQuorumKeepOldest.Spec(1,2,3)
class StaticQuorumKeepOldestMultiJvm5 extends StaticQuorumKeepOldest.Spec(1,2,3)

class StaticQuorumLoseOldestMultiJvm0 extends StaticQuorumKeepOldest.Spec(3,4,5)
class StaticQuorumLoseOldestMultiJvm1 extends StaticQuorumKeepOldest.Spec(3,4,5)
class StaticQuorumLoseOldestMultiJvm2 extends StaticQuorumKeepOldest.Spec(3,4,5)
class StaticQuorumLoseOldestMultiJvm3 extends StaticQuorumKeepOldest.Spec(3,4,5)
class StaticQuorumLoseOldestMultiJvm4 extends StaticQuorumKeepOldest.Spec(3,4,5)
class StaticQuorumLoseOldestMultiJvm5 extends StaticQuorumKeepOldest.Spec(3,4,5)
