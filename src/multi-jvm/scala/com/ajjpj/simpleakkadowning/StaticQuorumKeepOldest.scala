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

    "An cluster of three nodes" should {
      "reach initial convergence" in {
        awaitClusterUp(node1, node2, node3)
        enterBarrier("after-1")

        muteMarkingAsUnreachable(system)
        muteDeadLetters(classOf[AnyRef])(system)
      }

      "mark nodes as unreachable between partitions, and heal the partition" in {
        enterBarrier("before-split")

        val side1 = Vector(node1, node2)
        val side2 = Vector(node3)

        runOn(conductor) {
          // split the cluster in two parts (first, second) / (third, fourth, fifth)
          for (role1 ← side1; role2 ← side2) {
            testConductor.blackhole (role1, role2, Direction.Both).await
          }
        }
        enterBarrier("after-split")

        println("after-split")

        runOn(conductor) {
          for (i <- 1 to 20) {
            for (r <- side1 ++ side2) {
              println (s"${r.name}: up-nodes = ${upNodesFor(r).map(_.name).mkString(" ")}")
            }
            Thread.sleep(1000)
          }
        }

        runOn(side1 ++ side2 :_*) {
          Thread.sleep(20000)
        }

        println ("yo")

        enterBarrier("after-2")
      }
    }
  }
}

class StaticQuorumKeepOldestMultiJvmConductor extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode1 extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode2 extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode3 extends StaticQuorumKeepOldest.Spec
