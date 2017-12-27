package com.ajjpj.simpleakkadowning.util

import akka.remote.testkit.{MultiNodeSpec, MultiNodeSpecCallbacks}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


trait STMultiNodeSpec extends MultiNodeSpecCallbacks
  with WordSpecLike with Matchers with BeforeAndAfterAll { self: MultiNodeSpec =>

  override def beforeAll() = multiNodeSpecBeforeAll()
  override def afterAll() = multiNodeSpecAfterAll()
}

