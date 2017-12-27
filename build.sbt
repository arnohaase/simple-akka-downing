import Dependencies._

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-encoding", "UTF-8",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-language:higherKinds"
)

lazy val `simple-akka-downing` = (project in file("."))
  .enablePlugins(MultiJvmPlugin)
  .configs(MultiJvm)
  .settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "simple-akka-downing",

    libraryDependencies += akkaCluster,

    libraryDependencies += akkaHttp % Test,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += testKit % Test,
    libraryDependencies += multiNodeTestKit % Test,
    libraryDependencies ++= akkaLogging.map(_ % Test)
  )
