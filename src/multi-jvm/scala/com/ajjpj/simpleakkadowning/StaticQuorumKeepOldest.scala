package com.ajjpj.simpleakkadowning

import akka.remote.transport.ThrottlerTransportAdapter.Direction
import com.ajjpj.simpleakkadowning.util.{MultiNodeClusterSpec, SimpleDowningConfig}


object StaticQuorumKeepOldest {
  object Config extends SimpleDowningConfig("static-quorum", "quorum-size" -> "2") {
    val conductor = role("conductor")
    val node1 = role("node1")
    val node2 = role("node2")
    val node3 = role("node3")
  }

  abstract class Spec extends MultiNodeClusterSpec(Config) {
    import Config._

    val side1 = Vector (node1, node2)
    val side2 = Vector (node3)

    "An cluster of three nodes" should {
      "reach initial convergence" in {
        muteLog()
        muteMarkingAsUnreachable()
        muteMarkingAsReachable()

        awaitClusterUp(node1, node2, node3)
        enterBarrier("after-1")
      }

      "mark nodes as unreachable between partitions, and heal the partition" in {
        enterBarrier ("before-split")

        runOn (conductor) {
          for (role1 <- side1; role2 <- side2) {
            testConductor.blackhole (role1, role2, Direction.Both).await
          }
        }
        enterBarrier ("after-split")

        Thread.sleep (5000)

        runOn (conductor) {
          for (r <- side1) {
            upNodesFor (r) shouldBe (side1 ++ side2).toSet
            unreachableNodesFor (r) shouldBe side2.toSet
          }
          for (r <- side2) {
            upNodesFor (r) shouldBe (side1 ++ side2).toSet
            unreachableNodesFor (r) shouldBe side1.toSet
          }

          for (role1 <- side1; role2 <- side2) {
            testConductor.passThrough (role1, role2, Direction.Both).await
          }
        }
        enterBarrier ("after-network-heal")

        Thread.sleep (5000)

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

        runOn (conductor) {
          for (role1 <- side1; role2 <- side2) {
            testConductor.blackhole (role1, role2, Direction.Both).await
          }
          side2.foreach (testConductor.removeNode)

          Thread.sleep(10000)
        }

        runOn (side1 :_*) {
          enterBarrier("after-durable-partition")
        }

        runOn (conductor) {
          for (r <- side1) upNodesFor(r) shouldBe side1.toSet
          for (r <- side2) upNodesFor(r) shouldBe empty
        }

        runOn (side1 :_*) {
          enterBarrier("finished")
        }
      }
    }
  }
}

class StaticQuorumKeepOldestMultiJvmConductor extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode1 extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode2 extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode3 extends StaticQuorumKeepOldest.Spec
