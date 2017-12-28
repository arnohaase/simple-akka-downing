package com.ajjpj.simpleakkadowning.util

import akka.actor.Actor
import akka.cluster.{Cluster, MemberStatus}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer

import scala.concurrent.Await

/**
  * This class exposes some cluster state via HTTP to facilitate testing
  */
class ClusterHttpInspector(httpPort: Int) extends Actor {
  val cluster = Cluster.get(context.system)
  val routes = {
    import Directives._

    pathPrefix("cluster-members") {
      path("up") { complete {
        cluster.state.members.filter(_.status == MemberStatus.Up).map(_.address.port.get).mkString(" ")
      }} ~
      path("unreachable") { complete {
        println ("members: " + cluster.state.members)
        println ("unreachable: " + cluster.state.unreachable)
        cluster.state.unreachable.map(_.address.port.get).mkString(" ")
      }}
    }
  }

  import context.dispatcher
  implicit val mat = ActorMaterializer()

  val fServerBinding =
    Http(context.system)
      .bindAndHandle(routes, "localhost", httpPort)


  override def postStop () = {
    import scala.concurrent.duration._
    super.postStop ()
    fServerBinding.foreach(sb => Await.ready (sb.unbind(), 5.seconds))
  }

  override def receive = Actor.emptyBehavior

}
