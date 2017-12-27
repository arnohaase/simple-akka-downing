package com.ajjpj.simpleakkadowning.util

import akka.remote.testkit.MultiNodeSpec


abstract class SimpleDowningSpec(config: SimpleDowningConfig) extends MultiNodeSpec(config) {

}
