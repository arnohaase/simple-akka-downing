import sbt._

object Dependencies {
  final val akkaVersion = "2.5.8"
  final val akkaHttpVersion = "10.0.11"

  lazy val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion

  lazy val akkaLogging = Seq(
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "org.slf4j" % "jcl-over-slf4j" % "1.7.21",
    "ch.qos.logback" % "logback-core" % "1.1.10",
    "ch.qos.logback" % "logback-classic" % "1.1.10"
  )

  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"
  lazy val testKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  lazy val multiNodeTestKit = "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
}
