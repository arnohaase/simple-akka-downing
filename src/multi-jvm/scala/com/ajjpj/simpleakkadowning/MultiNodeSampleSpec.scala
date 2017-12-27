package com.ajjpj.simpleakkadowning

import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import com.ajjpj.simpleakkadowning.util.{ClusterHttpInspector, STMultiNodeSpec, SimpleDowningConfig, SimpleDowningSpec}
import com.typesafe.config.ConfigFactory


object MultiNodeSampleConfig extends SimpleDowningConfig {
  val node1 = role("node1")
  val node2 = role("node2")

  commonConfig(ConfigFactory.parseResources("application.conf"))

  nodeConfig(node2)(ConfigFactory.parseString(
    """akka.actor.provider=cluster
      |
    """.stripMargin))

//  commonConfig(debugConfig(true))
//  testTransport(true)
}

class MultiNodeSampleMultiJvmNode1 extends MultiNodeSample
class MultiNodeSampleMultiJvmNode2 extends MultiNodeSample

object MultiNodeSample {
  class Ponger extends Actor {
    def receive = {
      case "ping" ⇒ sender() ! "pong"
    }
  }
}

private[simpleakkadowning] abstract class MultiNodeSample extends SimpleDowningSpec(MultiNodeSampleConfig)
  with STMultiNodeSpec with ImplicitSender {

  import MultiNodeSample._
  import MultiNodeSampleConfig._

  def initialParticipants = roles.size

  "A MultiNodeSample" should {
    "send to and receive from a remote node" in {
      runOn(node1) {

        enterBarrier("deployed")
        val ponger = system.actorSelection(node(node2) / "user" / "ponger")
        ponger ! "ping"
        import scala.concurrent.duration._
        expectMsg(10.seconds, "pong")

        val response = Http(system).singleRequest(HttpRequest(uri=Uri("http://localhost:12345/cluster-members"))).await
        println (response)
      }

      runOn(node2) {
        Cluster(system).join(myAddress)

        system.actorOf(Props[Ponger], "ponger")
        system.actorOf(Props(new ClusterHttpInspector(12345)), "http-server")

        enterBarrier("deployed")
      }

      enterBarrier("finished")
    }
  }
}
