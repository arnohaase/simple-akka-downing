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

crossScalaVersions := Seq("2.11.8", "2.12.4")
scalaVersion := "2.12.4"
organization := "com.ajjpj.simple-akka-downing"

version      := "0.9.2"


lazy val `simple-akka-downing` = (project in file("."))
  .enablePlugins(MultiJvmPlugin)
  .configs(MultiJvm)
  .settings(
    parallelExecution in Test := false,
    name := "simple-akka-downing",

    libraryDependencies += akkaCluster,

    libraryDependencies += akkaHttp % Test,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += testKit % Test,
    libraryDependencies += multiNodeTestKit % Test,
  )

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)