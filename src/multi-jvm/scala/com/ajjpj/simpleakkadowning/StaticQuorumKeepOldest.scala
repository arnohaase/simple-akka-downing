package com.ajjpj.simpleakkadowning

import com.ajjpj.simpleakkadowning.StaticQuorumKeepOldest.NoRolesConfig
import com.ajjpj.simpleakkadowning.util.{AbstractDowningSpec, SimpleDowningConfig}


object StaticQuorumKeepOldest {
  object NoRolesConfig extends SimpleDowningConfig("static-quorum", "quorum-size" -> "3")
}

class StaticQuorumKeepOldestMultiJvm0 extends AbstractDowningSpec(NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm1 extends AbstractDowningSpec(NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm2 extends AbstractDowningSpec(NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm3 extends AbstractDowningSpec(NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm4 extends AbstractDowningSpec(NoRolesConfig, 1,2,3)
class StaticQuorumKeepOldestMultiJvm5 extends AbstractDowningSpec(NoRolesConfig, 1,2,3)

class StaticQuorumLoseOldestMultiJvm0 extends AbstractDowningSpec(NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm1 extends AbstractDowningSpec(NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm2 extends AbstractDowningSpec(NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm3 extends AbstractDowningSpec(NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm4 extends AbstractDowningSpec(NoRolesConfig, 3,4,5)
class StaticQuorumLoseOldestMultiJvm5 extends AbstractDowningSpec(NoRolesConfig, 3,4,5)
