package com.ajjpj.simpleakkadowning

import akka.testkit.ImplicitSender
import com.ajjpj.simpleakkadowning.util.{STMultiNodeSpec, SimpleDowningConfig, SimpleDowningSpec}


object StaticQuorumKeepOldest {
  object Config extends SimpleDowningConfig("static-quorum", "quorum-size" -> "2") {
    val conductor = role("conductor")
    val node1 = role("node1")

  }

  abstract class Spec extends SimpleDowningSpec(Config) with STMultiNodeSpec with ImplicitSender {
    def initialParticipants = roles.size

    "A MultiNodeSample" should {
      "initialize a cluster" in {
        init()
        println ("yo")
      }
    }
  }
}

class StaticQuorumKeepOldestMultiJvmNode1 extends StaticQuorumKeepOldest.Spec
class StaticQuorumKeepOldestMultiJvmNode2 extends StaticQuorumKeepOldest.Spec



