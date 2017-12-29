package com.ajjpj.simpleakkadowning

import akka.remote.transport.ThrottlerTransportAdapter.Direction
import com.ajjpj.simpleakkadowning.util.{MultiNodeClusterSpec, SimpleDowningConfig}


object StaticQuorumKeepOldest {
  object Config extends SimpleDowningConfig("static-quorum", "quorum-size" -> "3") {

    val conductor = role("conductor")
    val node1 = role("node1")
    val node2 = role("node2")
    val node3 = role("node3")
    val node4 = role("node4")
    val node5 = role("node5")

  }

  abstract class Spec extends MultiNodeClusterSpec(Config) {
    import Config._

    val side1 = Vector (node1, node2, node3)
    val side2 = Vector (node4, node5)

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

        createNetworkPartition(side1, side2)
        enterBarrier ("after-split")
//        runOn (conductor) {
//          for (role1 <- side1; role2 <- side2) {
//            testConductor.blackhole (role1, role2, Direction.Both).await
//          }
//        }

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
        }

        enterBarrier("after-split-check")

        healNetworkPartition()
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

        createNetworkPartition (side1, side2)


//        runOn (conductor) {
//          for (r <- side1 ++ side2) {
//            println (r.name + ": " + address(r).port.get)
//          }
//
//          side2.foreach (testConductor.removeNode(_))
//          for (role1 <- side1; role2 <- side2) {
//            println("blackholing " + role1.name + " <-> " + role2.name)
//            testConductor.blackhole (role1, role2, Direction.Both)
//          }

//          Thread.sleep(10000)
//          enterBarrier("after-durable-partition")
//        }

//        runOn (side1 :_*) {
//          enterBarrier("after-durable-partition")
//        }

        Thread.sleep(15000)

        runOn (conductor) {
          println ("--8<----8<----8<----8<----8<----8<----8<----8<----8<----8<----8<----8<----8<----8<----8<----8<----8<----8<--")

          for (r <- side1) upNodesFor(r) shouldBe side1.toSet
          for (r <- side2) upNodesFor(r) shouldBe empty
        }

        Thread.sleep(5000)

//        runOn(conductor) {
//          enterBarrier("finished")
//        }
//        runOn (side1 :_*) {
//          enterBarrier("finished")
//        }
      }
    }
  }
}

class StaticQuorumKeepOldestMultiJvmCondu extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode1 extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode2 extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode3 extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode4 extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode5 extends StaticQuorumKeepOldest.Spec

//class _StaticQuorumKeepOldestMultiJvmCondu extends StaticQuorumKeepOldest.Spec
//class _StaticQuorumKeepOldestMultiJvmNode1 extends StaticQuorumKeepOldest.Spec
//class _StaticQuorumKeepOldestMultiJvmNode2 extends StaticQuorumKeepOldest.Spec
//class _StaticQuorumKeepOldestMultiJvmNode3 extends StaticQuorumKeepOldest.Spec
//class _StaticQuorumKeepOldestMultiJvmNode4 extends StaticQuorumKeepOldest.Spec
//class _StaticQuorumKeepOldestMultiJvmNode5 extends StaticQuorumKeepOldest.Spec
