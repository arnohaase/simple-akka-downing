package com.ajjpj.simpleakkadowning.util

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory


abstract class SimpleDowningConfig(strategy: String, strategyConfig: (String,String)*) extends MultiNodeConfig {
  commonConfig(ConfigFactory.parseResources("application.conf"))

  private var isFirstNode = true
  override def role (name: String) = {
    val roleName = super.role (name)

    if (isFirstNode)
      isFirstNode = false
    else {
      val configString =
        s"""akka.actor.provider=cluster
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
