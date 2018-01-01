package com.ajjpj.simpleakkadowning.util

import java.util.concurrent.atomic.AtomicReference

import akka.remote.FailureDetector
import com.typesafe.config.Config
import akka.event.EventStream

/**
  * User controllable "puppet" failure detector.
  */
class FailureDetectorPuppet(config: Config, ev: EventStream) extends FailureDetector {

  trait Status
  object Up extends Status
  object Down extends Status
  object Unknown extends Status

  private val status: AtomicReference[Status] = new AtomicReference(Unknown)

  def markNodeAsUnavailable(): Unit = {
    println ("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< markAsUnavailable")
    status.set(Down)
  }

  def markNodeAsAvailable(): Unit = {
    println (">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> markAsAvailable")
    status.set(Up)
  }

  override def isAvailable: Boolean = status.get match {
    case Unknown | Up ⇒ true
    case Down         ⇒ false
  }

  override def isMonitoring: Boolean = status.get != Unknown

  override def heartbeat(): Unit = status.compareAndSet(Unknown, Up)

}