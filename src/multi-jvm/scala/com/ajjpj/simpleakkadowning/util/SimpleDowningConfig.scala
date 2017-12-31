package com.ajjpj.simpleakkadowning.util

import java.util.UUID

import akka.remote.testkit.MultiNodeConfig
import com.ajjpj.simpleakkadowning.StaticQuorumKeepOldest.Config.role
import com.typesafe.config.ConfigFactory


abstract class SimpleDowningConfig(strategy: String, strategyConfig: (String,String)*) extends MultiNodeConfig {
  commonConfig(ConfigFactory.parseResources("application.conf"))

  final val CLUSTER_SIZE = 5
  final val ROLE_SIZE = 3

  private var numRoles = 0
  override def role (name: String) = {
    val roleName = super.role (name)
    numRoles += 1

    if (numRoles > 1) {
      var clusterRoles = Vector.empty[String]

      if (numRoles <= ROLE_SIZE) clusterRoles :+= "with-oldest"
      if (numRoles > CLUSTER_SIZE - ROLE_SIZE) clusterRoles :+= "without-oldest"

      val configString =
        s"""akka.actor.provider=cluster
           |akka.actor.warn-about-java-serializer-usage = off
           |akka.cluster {
           |  jmx.enabled                         = off
           |  gossip-interval                     = 100 ms
           |  leader-actions-interval             = 100 ms
           |  unreachable-nodes-reaper-interval   = 100 ms
           |  periodic-tasks-initial-delay        = 100 ms
           |  publish-stats-interval              = 0 s # always, when it happens
           |  failure-detector.heartbeat-interval = 100 ms
           |  failure-detector.implementation-class = com.ajjpj.simpleakkadowning.util.FailureDetectorPuppet
           |
           |  roles = ${clusterRoles.mkString("[", ",", "]")}
           |
           |  run-coordinated-shutdown-when-down = off
           |}
           |akka.loglevel = INFO
           |akka.log-dead-letters = off
           |akka.log-dead-letters-during-shutdown = off
           |akka.remote {
           |  log-remote-lifecycle-events = off
           |  artery.advanced.flight-recorder {
           |    enabled=on
           |     destination=target/flight-recorder-${UUID.randomUUID ().toString}.afr
           |  }
           |}
           |akka.loggers = ["akka.testkit.TestEventListener"]
           |akka.test {
           |  single-expect-default = 5 s
           |}
           |
           |akka.remote.retry-gate-closed-for = 3 s
           |akka.cluster {
           |  failure-detector.threshold = 3
           |}
           |
           |
           |akka.cluster.downing-provider-class = com.ajjpj.simpleakkadowning.SimpleAkkaDowningProvider
           |
           |simple-akka-downing.stable-after=5s
           |simple-akka-downing.active-strategy=$strategy
           |""".stripMargin +
          strategyConfig.map(c => s"simple-akka-downing.$strategy.${c._1}=${c._2}").mkString("\n")

      nodeConfig (roleName)(ConfigFactory.parseString (configString))
    }

    roleName
  }

  //  commonConfig(debugConfig(true))
  testTransport(true)
}
