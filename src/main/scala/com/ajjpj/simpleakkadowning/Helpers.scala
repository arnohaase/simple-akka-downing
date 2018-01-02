package com.ajjpj.simpleakkadowning

import java.util.Locale
import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

import scala.concurrent.duration.{Duration, FiniteDuration}


private[simpleakkadowning] object Helpers {
  def toRootLowerCase(s: String) = s.toLowerCase(Locale.ROOT)


  /**
    * Implicit class providing `requiring` methods. This class is based on
    * `Predef.ensuring` in the Scala standard library. The difference is that
    * this class's methods throw `IllegalArgumentException`s rather than
    * `AssertionError`s.
    *
    * @param value The value to check.
    */
  @inline final implicit class Requiring[A](val value: A) extends AnyVal {
    /**
      * Check that a condition is true. If true, return `value`, otherwise throw
      * an `IllegalArgumentException` with the given message.
      *
      * @param cond The condition to check.
      * @param msg The message to report if the condition isn't met.
      */
    @inline def requiring(cond: Boolean, msg: ⇒ Any): A = {
      require(cond, msg)
      value
    }

    /**
      * Check that a condition is true for the `value`. If true, return `value`,
      * otherwise throw an `IllegalArgumentException` with the given message.
      *
      * @param cond The function used to check the `value`.
      * @param msg The message to report if the condition isn't met.
      */
    @inline def requiring(cond: A ⇒ Boolean, msg: ⇒ Any): A = {
      require(cond(value), msg)
      value
    }
  }

  final implicit class ConfigOps(val config: Config) extends AnyVal {
    def getMillisDuration(path: String): FiniteDuration = getDuration(path, TimeUnit.MILLISECONDS)

    def getNanosDuration(path: String): FiniteDuration = getDuration(path, TimeUnit.NANOSECONDS)

    private def getDuration(path: String, unit: TimeUnit): FiniteDuration =
      Duration(config.getDuration(path, unit), unit)
  }

}
