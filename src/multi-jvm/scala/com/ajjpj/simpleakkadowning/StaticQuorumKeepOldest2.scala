package com.ajjpj.simpleakkadowning

import akka.remote.transport.ThrottlerTransportAdapter.Direction
import com.ajjpj.simpleakkadowning.util.{SimpleDowningConfig, SimpleDowningSpec}


object StaticQuorumKeepOldest2 {
  object Config extends SimpleDowningConfig("static-quorum", "quorum-size" -> "2") {
    val conductor = role("conductor")
    val node1 = role("node1")
    val node2 = role("node2")
    val node3 = role("node3")
  }

  abstract class Spec extends SimpleDowningSpec(Config) {
    import Config._

    "An Akka Cluster" should {
      "mark nodes as unreachable between partitions, and heal the partition" in {
        runOn(conductor) {
          testConductor.abort(node1, node2)
          testConductor.abort(node1, node3)
          testConductor.abort(node1, conductor)
        }

        enterBarrier("+++")

        init()

        runOn(conductor) {
          println ("+++ " + unreachableNodesFor(node1))

          testConductor.blackhole(node1, node2, Direction.Both)
          testConductor.blackhole(node1, node3, Direction.Both)
          testConductor.blackhole(node1, conductor, Direction.Both)

//          createPartition(node1, node2)
          Thread.sleep(3000)

          println ("*** " + unreachableNodesFor(node1))

          upNodesFor(node1) shouldBe Set(node1, node2, node3)
          upNodesFor(node2) shouldBe Set(node1, node2, node3)
          upNodesFor(node3) shouldBe Set(node1, node2, node3)

          unreachableNodesFor(node1) shouldBe Set(node3)
          unreachableNodesFor(node2) shouldBe Set(node3)
          unreachableNodesFor(node3) shouldBe Set(node1, node2)

          healPartition()
          Thread.sleep(3000)

          unreachableNodesFor(node1) shouldBe Set(node1, node2, node3)
          unreachableNodesFor(node2) shouldBe Set(node1, node2, node3)
          unreachableNodesFor(node3) shouldBe Set(node1, node2, node3)
        }

//        println ("yo: " + upNodesFor(node1))
        enterBarrier("---")
      }
    }
  }
}

//class StaticQuorumKeepOldestMultiJvmConductor extends StaticQuorumKeepOldest.Spec
//class StaticQuorumKeepOldestMultiJvmNode1 extends StaticQuorumKeepOldest.Spec
//class StaticQuorumKeepOldestMultiJvmNode2 extends StaticQuorumKeepOldest.Spec
//class StaticQuorumKeepOldestMultiJvmNode3 extends StaticQuorumKeepOldest.Spec



